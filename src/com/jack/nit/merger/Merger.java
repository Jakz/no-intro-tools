package com.jack.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jack.nit.data.GameSetStatus;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.scanner.Options;
import com.jack.nit.scanner.RomHandle;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.SevenZipException;

public class Merger
{
  GameSetStatus set;
  Options options;
  
  public Merger(GameSetStatus set, Options options)
  {
    this.set = set;
    this.options = options;
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
    RomHandle[] handles = Arrays.stream(set.found).map(rfr -> rfr.handle).toArray(i -> new RomHandle[i]);
    Compressor compressor = new Compressor(options);
    compressor.createArchive(dest, handles);
  }
  
  private void mergeToOneArchivePerGame(Path dest) throws FileNotFoundException, SevenZipException
  {
    RomHandle[] handles = Arrays.stream(set.found).map(rfr -> rfr.handle).toArray(i -> new RomHandle[i]);
    Compressor compressor = new Compressor(options);
    
    Arrays.stream(handles).forEach(StreamException.rethrowConsumer(h -> compressor.createArchive(dest, h)));
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
    
    Compressor compressor = new Compressor(options);
    
    clones.values().forEach(
      StreamException.rethrowConsumer(a -> 
        compressor.createArchive(dest.resolve(a.name+options.archiveFormat.extension), a.handles.toArray(new RomHandle[a.handles.size()]))
      )
    );
    
    
  }
  
  private void mergeUncompressed(Path dest)
  {
    Arrays.stream(set.found).forEach(StreamException.rethrowConsumer(rfr -> {
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
}
