package com.github.jakz.nit.config;

import java.nio.file.Path;

public class ScannerEntry
{
  public static enum Type
  {
    BINARY,
    ARCHIVE,
    NESTED_ARCHIVE
  }
  
  
  public final Path path;
  public final String fileName;
  public final long size;
  
  ScannerEntry(Path path, String fileName, long size)
  {
    this.path = path;
    this.fileName = fileName;
    this.size = size;
  }
}
