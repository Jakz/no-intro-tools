package com.github.jakz.nit.config;

import com.github.jakz.nit.scanner.ScannerEntry;
import java.util.function.Predicate;

public class ScannerOptions
{
  public boolean multithreaded;
  public boolean includeSubfolders;
  public boolean discardUnknownSizes;
  
  public boolean scanArchives = true;
  public boolean scanNestedArchives = true;
  public boolean assumeCRCisCorrect = true;
  
  public Predicate<ScannerEntry> shouldSkip;
}
