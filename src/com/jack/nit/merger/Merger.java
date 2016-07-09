package com.jack.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.jack.nit.data.GameSetStatus;
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
