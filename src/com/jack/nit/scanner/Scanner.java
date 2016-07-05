package com.jack.nit.scanner;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.jack.nit.Settings;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.HashCache;
import com.jack.nit.data.RomReference;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.pixbits.io.FileUtils;
import com.pixbits.io.FolderScanner;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Scanner
{
  final private GameSet set;
  final private HashCache cache;
  final private PathMatcher archiveMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{zip,rar,7z}");
  
  public Scanner(GameSet set)
  {
    this.set = set;
    this.cache = new HashCache(set);
  }
  
  private IInArchive openArchive(Path path) throws FormatUnrecognizedException
  {
    try (RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r")))
    {
      IInArchive archive = SevenZip.openInArchive(null, rfile);

      if (archive.getArchiveFormat() == null)
        throw new FormatUnrecognizedException(path, "Archive format unrecognized");
      
      return archive;
    }
    catch (IOException e)
    {
      throw new FormatUnrecognizedException(path, "Archive format unrecognized");
    }
  }
  
  
  public Set<Path> computeFileList() throws IOException
  {
    Set<Path> files = new TreeSet<>();
    Path[] paths = Settings.resolveRomPathsForSet(set);
    
    boolean includeSubfolders = true;
    final FolderScanner scanner = new FolderScanner(includeSubfolders);

    Arrays.stream(paths).map(StreamException.rethrowFunction(scanner::scan)).forEach(files::addAll);
    
    Logger.log(Log.INFO1, "found %d files to scan in %d paths", files.size(), paths.length);
    
    return files;
  }
  
  public RomReference isValidRom(long size, long crc)
  {
    return cache.isValidSize(size) ? cache.romForCrc(crc) : null;
  }
  
  public Set<RomHandle> computeHandles() throws IOException
  {
    Set<Path> paths = computeFileList();
    
    Set<Path> faultyArchives = new HashSet<>();
    Set<String> skipped = new HashSet<>();
    
    Logger.logger.startProgress("[INFO] Checking format of files...");
    final float count = paths.size();
    final AtomicInteger current = new AtomicInteger(0);
    
    Set<RomHandle> handles = new HashSet<>();
    
    int[] counters = new int[2];
        
    paths.forEach(StreamException.rethrowConsumer(path -> {
      Logger.logger.updateProgress(current.getAndIncrement() / count);
      
      boolean shouldBeArchive = archiveMatcher.matches(path.getFileName());
      
      if (shouldBeArchive)
      {      
        try (IInArchive archive = openArchive(path))
        {
          int itemCount = archive.getNumberOfItems();
          
          for (int i = 0; i < itemCount; ++i)
          {
            long uncompressedSize = (long)archive.getProperty(i, PropID.SIZE);
            String fileName = (String)archive.getProperty(i, PropID.PATH);
            
            if (cache.isValidSize(uncompressedSize))
            {
              handles.add(new ArchiveHandle(path, archive.getArchiveFormat(), fileName, i));
              ++counters[1];
            }
            else
              skipped.add(fileName+" in "+path.getFileName());
          }
        }
        catch (FormatUnrecognizedException e)
        {
          faultyArchives.add(path);
        }
      }
      else
      {
        /* if size of the file is compatible with the romset add it to potential roms */
        if (cache.isValidSize(Files.size(path)))
        {
          handles.add(new BinaryHandle(path));
          ++counters[0];
        }
        else
          skipped.add(path.getFileName().toString());
          
      }
      
    }));
    
    Logger.logger.endProgress();
    
    faultyArchives.forEach(p -> Logger.log(Log.WARNING, "File "+p.getFileName()+" is not a valid archive."));

    Logger.log(Log.INFO1, "Found %d potential matches (%d binary, %d inside archives).", counters[0]+counters[1], counters[0], counters[1]);
    Logger.log(Log.INFO3, "Skipped %d entries:", skipped.size());
    skipped.forEach(s -> Logger.log(Log.INFO3, "> %s", s));

    return handles;
  }
}
