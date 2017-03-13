package com.github.jakz.nit.scanner;

import java.nio.file.Path;
import java.util.Set;

public class FileEnumerator
{
  private final boolean multithreaded;
  private final boolean recursive;
  
  public FileEnumerator(boolean multithreaded, boolean recursive)
  {
    this.multithreaded = multithreaded;
    this.recursive = recursive;
  }
  
  Set<Path> enumerate(Set<Path> folders)
  {
    return null;
  }
}
