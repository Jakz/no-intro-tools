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
import java.util.stream.Collectors;

import com.jack.nit.Options;
import com.jack.nit.data.GameSetStatus;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.scanner.RomHandle;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Merger
{
  GameSetStatus set;
  Options options;
  Compressor compressor;
  
  public Merger(GameSetStatus set, Options options)
  {
    this.set = set;
    this.options = options;
    this.compressor = new Compressor(options);
  }
  
  public void merge(Path dest) throws FileNotFoundException, SevenZipException
  {
    try
    {
      Files.createDirectories(dest);
    }
    catch (IOException e)
    {
      throw new FileNotFoundException("unable to create destination path for merging at "+dest.toString());
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
  
  private void mergeToSingleArchive(Path dest) throws FileNotFoundException, SevenZipException
  {
    RomHandle[] handles = set.found.stream().map(rfr -> rfr.handle).toArray(i -> new RomHandle[i]);
    
    String archiveName = options.datPath.getFileName().toString();
    archiveName = archiveName.substring(0, archiveName.indexOf('.')) + options.archiveFormat.extension;
    
    compressor.createArchive(dest.resolve(archiveName), handles);
  }
  
  private void mergeToOneArchivePerGame(Path dest) throws FileNotFoundException, SevenZipException
  {
    set.found.forEach(StreamException.rethrowConsumer(rfr -> {
      createArchive(dest.resolve(rfr.rom.game.name+options.archiveFormat.extension), new ArchiveInfo(rfr.rom.game.name, rfr.handle));
    }));    
  }
  
  private class ArchiveInfo
  {
    public final String name;
    public final List<RomHandle> handles;
    
    ArchiveInfo(String name)
    {
      this.name = name;
      this.handles = new ArrayList<>();
    }
    
    ArchiveInfo(String name, RomHandle... handles)
    {
      this(name);
      this.handles.addAll(Arrays.asList(handles));
    }
  }
  
  private void mergeToCloneArchives(Path dest) throws FileNotFoundException, SevenZipException
  {
    Map<GameClone, ArchiveInfo> clones = new HashMap<>();
    List<ArchiveInfo> handles = new ArrayList<>();
    
    for (RomFoundReference rfr : set.found)
    {
      GameClone clone = set.clones.get(rfr.rom.game);
      
      if (clone != null)
      {
        clones.compute(clone, (k,v) -> {
          if (v == null)
            return new ArchiveInfo(k.getTitleForBias(options.zonePriority), rfr.handle);
          else
          {
            v.handles.add(rfr.handle);
            return v;
          }
        });
      }
      else
        handles.add(new ArchiveInfo(rfr.rom.game.normalizedTitle(), rfr.handle)); 
    }
    
    Logger.log(Log.INFO1, "Merger is going to create %d archives.", clones.size()+handles.size());
        
    clones.values().forEach(
      StreamException.rethrowConsumer(a -> 
        createArchive(dest.resolve(a.name+options.archiveFormat.extension), a)
      )
    );
    
    handles.forEach(
      StreamException.rethrowConsumer(a -> 
        createArchive(dest.resolve(a.name+options.archiveFormat.extension), a)
      )
    );
  }
    
  private void mergeUncompressed(Path dest)
  {
    set.found.forEach(StreamException.rethrowConsumer(rfr -> {
      RomHandle handle = rfr.handle;
      
      // TODO: manage src dest as same path
      // just copy the file
      if (!handle.isArchive())
      {
        Files.copy(handle.file(), dest.resolve(handle.file().getFileName()));
      }
      else
      {
        // extract from archive to dest
      }
    }));
  }
  
  private void createArchive(Path dest, ArchiveInfo info) throws FileNotFoundException, SevenZipException
  {
    ArchiveStatus status = checkExistingArchiveStatus(dest, info);
    
    switch (status)
    {
      case UP_TO_DATE:
        Logger.log(Log.INFO2, "Skipping creation of %s, already up to date.", dest.getFileName().toString());
        break;
      case CREATE:
        compressor.createArchive(dest, info.handles.toArray(new RomHandle[info.handles.size()]));
        break;
      case UPDATE:
      {
        
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
                long crc = (long)(int)archive.getProperty(i, PropID.CRC);
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
              crcs.put((long)(int)archive.getProperty(i, PropID.CRC), (String)archive.getProperty(i, PropID.PATH));
            
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
    
    set.found.forEach(rfr -> {
      if (rfr.handle.isArchive())
      {
        archives.compute(rfr.handle.file(), (path,archive) -> {
          if (archive == null)
            archive = new ExistingArchive(path);
          else
            archive.handles.add(rfr.handle);
          
          return archive;
        });
      }
    });
    
    return archives;
  }
}
