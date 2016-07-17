package com.jack.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.jack.nit.Options;
import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.Rom;
import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.exceptions.FatalErrorException;
import com.jack.nit.handles.RomHandle;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

//TODO: relocation after move/compress doesn't apply to internal name for now but it should

public class Merger
{
  GameSet set;
  Options options;
  Compressor compressor;
  List<Rom> found;
  
  TitleNormalizer normalizer;
  
  public Merger(GameSet set, Predicate<Game> filter, Options options)
  {
    this.set = set;
    this.options = options;
    this.compressor = new Compressor(options);
    this.found = set.foundRoms().filter(r -> filter.test(r.game())).collect(Collectors.toList());
    this.normalizer = new TitleNormalizer();
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

    switch (options.mergeMode)
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
    RomHandle[] handles = found.stream().map(rom -> rom.handle()).toArray(i -> new RomHandle[i]);
    
    String archiveName = options.datPath.getFileName().toString();
    archiveName = archiveName.substring(0, archiveName.lastIndexOf('.')) + options.archiveFormat.extension;
    
    if (!options.doesMergeInPlace())
      Files.delete(dest.resolve(archiveName)); 
    else
    {
      //TODO: if merge is in place we should probably just update existing archive
    }
    
    compressor.createArchive(dest.resolve(archiveName), handles);
    Arrays.stream(handles).forEach(h -> h.relocate(dest));
  }
  
  private void mergeToOneArchivePerGame(Path dest) throws FileNotFoundException, SevenZipException
  {
    found.forEach(StreamException.rethrowConsumer(rom -> {
      //TODO: manage games with multiple roms per game
      final Path finalPath = dest.resolve(rom.game().name+options.archiveFormat.extension);
      final ArchiveInfo archive = new ArchiveInfo(rom.game().name, rom.handle());
      createArchive(finalPath, archive);
      archive.relocate(finalPath);
    }));    
  }
  
  private void mergeToCloneArchives(Path dest) throws FileNotFoundException, SevenZipException
  {
    if (set.clones() == null || set.clones().size() == 0)
      throw new FatalErrorException(String.format("can't merge '%s' by using game clones since there is no clone info", set.info.name));
    
    Map<GameClone, ArchiveInfo> clones = new HashMap<>();
    List<ArchiveInfo> handles = new ArrayList<>();
    Map<String, GameClone> cloneMapping = new HashMap<>();
    
    for (Rom rom : found)
    {
      GameClone clone = set.clones().get(rom.game());
      
      if (clone != null)
      {
        clones.compute(clone, (k,v) -> {
          String archiveName = normalizer.normalize(k.getTitleForBias(options.zonePriority));

          if (v == null)
          {
            cloneMapping.putIfAbsent(archiveName, clone);
            return new ArchiveInfo(archiveName, rom.handle());
          }
          else
          {
            if (!clone.equals(cloneMapping.get(archiveName)))
              throw new FatalErrorException(String.format("can't merge '%s' correctly: clone data contains two entries that resolve to same name: %s", set.info.name, archiveName));
            
            v.handles.add(rom.handle());
            return v;
          }
        });
      }
      else
        handles.add(new ArchiveInfo(normalizer.normalize(rom.game().name), rom.handle())); 
    }
    
    Logger.log(Log.INFO1, "Merger is going to create %d archives.", clones.size()+handles.size());
        
    Consumer<ArchiveInfo> compress = StreamException.rethrowConsumer(a -> { 
        final Path path = dest.resolve(a.name+options.archiveFormat.extension);
        createArchive(path, a);
        a.relocate(path);
    });
    
    // TODO: parallel?
    
    clones.values().forEach(compress);
    handles.forEach(compress);
  }
    
  private void mergeUncompressed(Path dest)
  {
    found.forEach(StreamException.rethrowConsumer(rom -> {
      RomHandle handle = rom.handle();
      
      // TODO: manage src dest as same path
      // just copy the file
      if (!handle.isArchive())
      {
        Path path = dest.resolve(handle.file().getFileName());
        Files.copy(handle.file(), path);
        handle.relocate(path);
      }
      else
      {
        // extract from archive to dest
      }
    }));
  }
  
  private void createArchive(Path dest, ArchiveInfo info) throws FileNotFoundException, SevenZipException, IOException
  {
    ArchiveStatus status = checkExistingArchiveStatus(dest, info);
    
    switch (status)
    {
      case UP_TO_DATE:
        Logger.log(Log.INFO2, "Skipping creation of %s, already up to date.", dest.getFileName().toString());
        break;
      case CREATE:
        Files.deleteIfExists(dest); 
        compressor.createArchive(dest, info.handles.toArray(new RomHandle[info.handles.size()]));
        break;
      case UPDATE:
      {
        Logger.log(Log.DEBUG, "Archive %s must be updated", dest.getFileName());
        
        /* if archive needs to be updated it means it alrady exists and already present roms should be merged with new roms to the archive
         * so we first rename the existing archive to a temporary name
         */
        Path tempArchive = Files.createTempFile(dest.getParent(), "", "." + options.archiveFormat.extension);
        
        /* then we update all references to old archive to new name */
        info.handles.stream().filter(rh -> rh.file().equals(dest)).forEach(rh -> rh.relocate(tempArchive));
        
        /* now we can create the new archive by merging items from old archive and the new files */
        compressor.createArchive(dest, info.handles.toArray(new RomHandle[info.handles.size()]));
        
        /* now it's safe to delete temporary file because otherwise checkExistingArchiveStatus would have returned ArchiveStatus.ERROR */
        Files.delete(tempArchive);       
        
        break;
      }
      case ERROR:
        Logger.log(Log.ERROR, "Error on checking status of existing archive %s", dest.getFileName().toString());
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
          if (archive.getNumberOfItems() != info.handles.size() && !options.keepUnrecognizedFilesInArchives)
          {
            if (options.doesMergeInPlace())
            {
              // if merge is in place and archive exists then all files inside the archive should be present also in ArchiveInfo
              Map<Long, String> crcs = new HashMap<>();
              info.handles.stream().forEach(handle -> crcs.put(handle.crc(), handle.fileName()));

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
            
            return info.handles.stream().allMatch(h -> h.fileName().equals(crcs.get(h.crc()))) ? ArchiveStatus.UP_TO_DATE : ArchiveStatus.CREATE;
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
    final Path file;
    final Set<RomHandle> handles;
    
    ExistingArchive(Path file)
    {
      this.file = file;
      this.handles = new HashSet<>();
    }
  };
  
  public Map<Path,ExistingArchive> computeInPlaceStatus()
  {
    Map<Path, ExistingArchive> archives = new HashMap<>();
    
    found.forEach(rom -> {
      if (rom.handle().isArchive())
      {
        archives.compute(rom.handle().file(), (path,archive) -> {
          if (archive == null)
            archive = new ExistingArchive(path);
          else
            archive.handles.add(rom.handle());
          
          return archive;
        });
      }
    });
    
    return archives;
  }
}
