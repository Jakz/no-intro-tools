package com.jack.nit.scanner;

public class ScannerOptions
{
  public final boolean matchSize;
  public final boolean matchSHA1;
  public final boolean matchMD5;
  public final boolean multiThreaded;
  
  public ScannerOptions()
  {
    matchSize = true;
    matchSHA1 = true;
    matchMD5 = true;
    multiThreaded = true;
  }
}
