package com.github.jakz.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jakz.nit.Options;
import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.GameClone;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.set.Feature;
import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.exceptions.FatalErrorException;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.archive.Compressor;
import com.pixbits.lib.io.archive.handles.Handle;
import com.pixbits.lib.io.archive.support.ArchivePipedInputStream;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.ProgressLogger;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

//TODO: relocation after move/compress doesn't apply to internal name for now but it should

public class Merger
{
  private final static Logger logger = Log.getLogger(Merger.class);
  private final static ProgressLogger progressLogger = Log.getProgressLogger(Merger.class);
  
  GameSet set;
  Options options;
  Compressor<ArchiveEntry> compressor;
  List<Game> found;
  
  TitleNormalizer normalizer;
  
  Function<Rom, String> fileNameBuilder;
  Function<Rom, String> folderNameBuilder;
  
  public Merger(GameSet set, Predicate<Game> filter, Options options)
  {
    this.set = set;
    this.options = options;
    this.compressor = new Compressor<>(options.getCompressorOptions());
    this.compressor.setCallbackOnAddingEntryToArchive(handle -> logger.d("Preparing item %s (%d bytes) to be archived", handle.fileName(), handle.size()));
    this.found = set.stream().filter(Game::hasAnyRom).filter(g -> filter.test(g)).collect(Collectors.toList());
    this.normalizer = new TitleNormalizer();
    
    fileNameBuilder = rom -> FileUtils.trimExtension(rom.name) + "." + rom.handle().getInternalExtension();
    folderNameBuilder = rom -> set.hasFeature(Feature.SINGLE_ROM_PER_GAME) && !options.merge.forceFolderPerGameStructure ? "" : rom.game().getTitle(); // TODO: normalizer?
  }

  public void merge(Path dest) throws IOException, SevenZipException
  {
    try
    {
      if (!options.doesMergeInPlace())
        Files.createDirectories(dest);
    }
    catch (IOException e)
    {
      throw new FatalErrorException("unable to create destination path for merging at "+dest.toString());
    }

    switch (options.merge.mode)
    {
      case SINGLE_ARCHIVE_PER_SET: mergeToSingleArchive(dest); break;
      case SINGLE_ARCHIVE_PER_GAME: mergeToOneArchivePerGame(dest); break;
      case SINGLE_ARCHIVE_PER_CLONE: mergeToCloneArchives(dest); break;
      case UNCOMPRESSED: mergeUncompressed(dest); break;
      default: break;
    }
  }
  
  private void mergeToSingleArchive(Path dest) throws SevenZipException, IOException
  {
    List<Handle> handles = found.stream().flatMap(Game::stream).map(rom -> rom.handle()).collect(Collectors.toList());
    
    String archiveName = options.datPath.getFileName().toString();
    archiveName = archiveName.substring(0, archiveName.lastIndexOf('.')) + options.merge.archiveFormat.dottedExtension();
    Path destArchive = dest.resolve(archiveName);
    
    if (!options.doesMergeInPlace() && Files.exists(destArchive))
      Files.delete(destArchive); 
    else
    {
      //TODO: if merge is in place we should probably just update existing archive
    }
    
    List<ArchiveEntry> entries = found.stream().flatMap(Game::stream).map(r -> {
      String folder = folderNameBuilder.apply(r);
      String fileName = fileNameBuilder.apply(r);
      String archivedName = !folder.isEmpty() ? (folder + '/' + fileName) : fileName;
      return new ArchiveEntry(r.handle(), archivedName);
    }).collect(Collectors.toList());
    
    // TODO: name mapping
    
    progressLogger.startProgress(Log.INFO2, "Creating single archive "+dest.toString());
    compressor.createArchive(dest.resolve(archiveName), entries);
    handles.forEach(h -> h.relocate(destArchive));
  }
  
  private void mergeToOneArchivePerGame(Path dest) throws FileNotFoundException, SevenZipException
  {
    //TODO: fix
    /*found.forEach(StreamException.rethrowConsumer(rom -> {
      //TODO: manage games with multiple roms per game
      final Path finalPath = dest.resolve(rom.game().getTitle()+options.merge.archiveFormat.dottedExtension());
      final ArchiveInfo archive = new ArchiveInfo(rom.game().getTitle(), rom.handle());
      createArchive(finalPath, archive);
      archive.relocate(finalPath);
    }));*/    
  }
  
  private void mergeToCloneArchives(Path dest) throws FileNotFoundException, SevenZipException
  {
    if (!set.hasFeature(Feature.CLONES))
      throw new FatalErrorException(String.format("can't merge '%s' by using game clones since there is no clone info", set.info().getName()));
    
    Map<String, ArchiveInfo> archives = new HashMap<>();
    Map<String, GameClone> cloneMapping = new HashMap<>();
    
    for (Game game : found)
    {    
      final GameClone existingClone = set.clones().get(game);
      final GameClone clone = existingClone != null ? existingClone : new GameClone(game);
      
      //TODO: folder builder insider archives?
      List<ArchiveEntry> entries = game.stream().filter(Rom::isPresent).map(r -> new ArchiveEntry(r.handle(), fileNameBuilder.apply(r))).collect(Collectors.toList());

      final String archiveName = normalizer.normalize(clone.getTitleForBias(options.zonePriority, true));
      
      archives.compute(archiveName, (k,v) -> {        
        /* if has clones and has multiple roms per clone we should use game name as internal folder to archive the game */
        
        if (v == null)
        {
          cloneMapping.putIfAbsent(archiveName, clone);
          return new ArchiveInfo(archiveName, entries);
        }
        else
        {
          GameClone alreadyGeneratedClone = cloneMapping.get(archiveName);
          
          if (!clone.equals(alreadyGeneratedClone) && !options.merge.automaticallyMergeClonesForSameNormalizedNames)
          {
            logger.d("Clone is resolved to same name %s as another existing clone", archiveName);
            logger.d(" existing: %s", alreadyGeneratedClone.stream().map(Game::getTitle).collect(Collectors.joining(", ")));
            logger.d(" current: %s ", clone.stream().map(Game::getTitle).collect(Collectors.joining(", ")));
            
            throw new FatalErrorException(String.format("can't merge '%s' correctly: clone data contains two entries that resolve to same name: %s", set.info().getName(), archiveName));
          }
          
          v.add(entries);
          return v;
        }
      });
    }
 
    Optional<ArchiveInfo> faultyArchive = archives.values().stream()
      .filter(archive -> new HashSet<>(archive.entries()).size() != archive.entries.size())
      .findAny();
    
    if (faultyArchive.isPresent())
      throw new FatalErrorException(String.format("can't merge '%s' correctly: clone %s contains two entries that resolve to same name: %s", set.info().getName(), faultyArchive.get().name));

    logger.i1("Merger is going to create %d archives.", archives.size());
        
    Consumer<ArchiveInfo> compress = StreamException.rethrowConsumer(a -> { 
        final Path path = dest.resolve(a.name+options.merge.archiveFormat.dottedExtension());
        createArchive(path, a);
        a.relocate(path);
    });
    
    // TODO: parallel?    
    archives.values().forEach(compress);
  }
    
  private void mergeUncompressed(Path base)
  {
    Stream<Rom> found = this.found.stream().flatMap(Game::stream);
    
    if (options.multiThreaded)
      found = found.parallel();
    
    found.forEach(StreamException.rethrowConsumer(rom -> {
      Handle handle = rom.handle();
      
      Path path = base.resolve(folderNameBuilder.apply(rom)).resolve(fileNameBuilder.apply(rom));
      Files.createDirectories(path.getParent());
      
      // TODO: manage src dest as same path
      // just copy the file
      if (!handle.isArchive())
      {
        Files.copy(handle.path(), path);
        handle.relocate(path);
      }
      else
      {        
        try (InputStream is = handle.getInputStream())
        {
          try (OutputStream os = Files.newOutputStream(path))
          {
            byte[] buffer = new byte[ArchivePipedInputStream.getBufferSize()];
            int i = -1;
            
            while ((i = is.read(buffer)) > 0)
            {
              os.write(buffer, 0, i);
            }
          }
        }
        
        handle.relocate(path);
      }
    }));
  }
  
  private void createArchive(Path dest, ArchiveInfo info) throws FileNotFoundException, SevenZipException, IOException
  {
    ArchiveStatus status = checkExistingArchiveStatus(dest, info);
    
    switch (status)
    {
      case UP_TO_DATE:
        logger.i2("Skipping creation of %s, already up to date.", dest.getFileName().toString());
        break;
      case CREATE:
        Files.deleteIfExists(dest); 
        progressLogger.startProgress(Log.INFO2, "Creating archive "+dest.toString());
        compressor.createArchive(dest, info.entries());
        break;
      case UPDATE:
      {
        logger.d("Archive %s must be updated", dest.getFileName());
        
        /* if archive needs to be updated it means it alrady exists and already present roms should be merged with new roms to the archive
         * so we first rename the existing archive to a temporary name
         */
        Path tempArchive = Files.createTempFile(dest.getParent(), "", options.merge.archiveFormat.dottedExtension());
        
        /* then we update all references to old archive to new name */
        info.stream().filter(e -> e.handle().path().equals(dest)).forEach(e -> e.handle().relocate(tempArchive));
        
        /* now we can create the new archive by merging items from old archive and the new files */
        progressLogger.startProgress(Log.INFO2, "Updating archive "+dest.toString());
        compressor.createArchive(dest, info.entries());
        
        /* now it's safe to delete temporary file because otherwise checkExistingArchiveStatus would have returned ArchiveStatus.ERROR */
        Files.delete(tempArchive);       
        
        break;
      }
      case ERROR:
        logger.e("Error on checking status of existing archive %s", dest.getFileName().toString());
        break;
    }
  }
  
  private enum ArchiveStatus
  {
    UP_TO_DATE,
    CREATE,
    UPDATE,
    ERROR
  };
  
  private ArchiveStatus checkExistingArchiveStatus(Path dest, ArchiveInfo info)
  {
    try
    {     
      if (options.alwaysRewriteArchives || !Files.exists(dest))
        return ArchiveStatus.CREATE;
    
      try (RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(dest.toFile(), "r")))
      {
        try (IInArchive archive =  SevenZip.openInArchive(null, rfile))
        {         
          if (archive.getNumberOfItems() != info.size() && !options.keepUnrecognizedFilesInArchives)
          {
            if (options.doesMergeInPlace())
            {
              // if merge is in place and archive exists then all files inside the archive should be present also in ArchiveInfo
              Map<Long, String> crcs = new HashMap<>();
              info.stream().forEach(entry -> crcs.put(entry.handle().crc(), entry.fileName()));

              int count = archive.getNumberOfItems();
              for (int i = 0; i < count; ++i)
              {
                long crc = Integer.toUnsignedLong((int)archive.getProperty(i, PropID.CRC));
                String filename = (String)archive.getProperty(i, PropID.PATH);
                if (!filename.equals(crcs.get(crc)))
                  return ArchiveStatus.ERROR;
              }
              
              return ArchiveStatus.UPDATE;
            }
            else
              return ArchiveStatus.CREATE;
          }
          else
          {
            Map<Long, String> crcs = new HashMap<>();
            
            int count = archive.getNumberOfItems();
            for (int i = 0; i < count; ++i)
              crcs.put(Integer.toUnsignedLong((int)archive.getProperty(i, PropID.CRC)), (String)archive.getProperty(i, PropID.PATH));
            
            return info.stream().allMatch(e -> e.fileName().equals(crcs.get(e.handle().crc()))) ? ArchiveStatus.UP_TO_DATE : ArchiveStatus.CREATE;
          }  
        }
      }
    }
    catch (IOException e)
    {
      return ArchiveStatus.ERROR;
    }
  }
  
  private class ExistingArchive
  {
    final Set<Handle> handles;
    
    ExistingArchive(Path file)
    {
      this.handles = new HashSet<>();
    }
  };
  
  /*public Map<Path,ExistingArchive> computeInPlaceStatus()
  {
    Map<Path, ExistingArchive> archives = new HashMap<>();
    
    found.forEach(rom -> {
      if (rom.handle().isArchive())
      {
        archives.compute(rom.handle().path(), (path,archive) -> {
          if (archive == null)
            archive = new ExistingArchive(path);
          else
            archive.handles.add(rom.handle());
          
          return archive;
        });
      }
    });
    
    return archives;
  }*/
}
