package com.jack.nit.scanner;

public class ScannerOptions
{
  public final boolean matchSize;
  public final boolean matchCRC;
  public final boolean matchSHA1;
  public final boolean matchMD5;
  
  public ScannerOptions()
  {
    matchSize = true;
    matchCRC = true;
    matchSHA1 = true;
    matchMD5 = true;
  }
}
