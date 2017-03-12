package com.github.jakz.nit.config;

import java.nio.file.Path;
import java.util.List;

public class ScannerOptions
{
  public List<Path> paths;
  public boolean multithreaded;
  public boolean includeSubfolders;
  public boolean discardUnknownSizes;
}
