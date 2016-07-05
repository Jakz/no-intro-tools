package com.jack.nit.scanner;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.jack.nit.Settings;
import com.jack.nit.data.GameSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.pixbits.io.FolderScanner;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Scanner
{
  final private GameSet set;
  final private PathMatcher archiveMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{zip,rar,7z}");
  
  public Scanner(GameSet set)
  {
    this.set = set;
  }
  
  private ArchiveFormat checkArchiveValidity(Path path) throws FormatUnrecognizedException
  {
    try (RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r")))
    {
      try (IInArchive archive = SevenZip.openInArchive(null, rfile))
      {
        if (archive.getArchiveFormat() == null)
          throw new FormatUnrecognizedException(path, "Archive format unrecognized");
        
        return archive.getArchiveFormat();
      }
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
    
    Logger.log(Log.INFO, "found %d files to scan", files.size());
    
    return files;
  }
  
  public Set<RomHandle> computeHandles() throws IOException
  {
    Set<Path> paths = computeFileList();
    
    paths.forEach(path -> {
      boolean shouldBeArchive = archiveMatcher.matches(path);
      
      if (shouldBeArchive)
      {      
        try
        {
          ArchiveFormat format = checkArchiveValidity(path);
        }
        catch (FormatUnrecognizedException e)
        {
          Logger.log(Log.ERROR, "File "+e.path.getFileName()+" doesn't look like a valid archive");
        }
      }
      
    });
    
    return null;
  }
}
