package com.pixbits.io;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;

import com.pixbits.stream.StreamException;

import java.util.HashSet;

public class FolderScanner
{
  private final Set<Path> files;
  private final Set<Path> excluded;
  private final PathMatcher filter;
  private final boolean includeSubfolders;
  
  public FolderScanner(PathMatcher filter, boolean includeSubfolders, Set<Path> excluded)
  {
    files = new HashSet<Path>();
    this.filter = filter;
    this.includeSubfolders = includeSubfolders;
    this.excluded = excluded;
  }
  
  public FolderScanner(PathMatcher filter, boolean includeSubfolders)
  {
    this(filter, includeSubfolders, null);
  }
  
  public FolderScanner(boolean includeSubfolders)
  {
    this(FileSystems.getDefault().getPathMatcher("glob:*.*"), includeSubfolders, null);
  }

  
  public Set<Path> scan(Path root) throws IOException
  {
    if (Files.isDirectory(root))
      innerScan(root);
    
    return files;
  }
  
  private void innerScan(Path folder) throws IOException
  {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder))
    {
      stream.forEach(StreamException.rethrowConsumer(e ->
      {                
        if (excluded == null || !excluded.stream().anyMatch(path -> e.startsWith(path)))
        {
          if (Files.isDirectory(e) && includeSubfolders)
            innerScan(e);
          else if (filter.matches(e.getFileName()))
            files.add(e);
        }
      }));
    }
    catch (AccessDeniedException e)
    {
      // silently kill
    }
  }
}
