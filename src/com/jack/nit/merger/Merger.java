package com.jack.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.jack.nit.data.GameSetStatus;
import com.jack.nit.scanner.Options;
import com.jack.nit.scanner.RomHandle;

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
      default: break;
    }
  }
  
  private void mergeToSingleArchive(Path dest) throws FileNotFoundException, SevenZipException
  {
    RomHandle[] handles = Arrays.stream(set.found).map(rfr -> rfr.handle).toArray(i -> new RomHandle[i]);
    Compressor compressor = new Compressor(options);
    compressor.createArchive(dest, handles);
  }
}
