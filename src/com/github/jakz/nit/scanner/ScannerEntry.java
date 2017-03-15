package com.github.jakz.nit.scanner;

public class ScannerEntry
{
  public final String fileName;
  public final long compressedSize;
  public final long size;
  public final long crc;
  
  ScannerEntry(String fileName, long size, long compressedSize, long crc)
  {
    this.fileName = fileName;
    this.size = size;
    this.compressedSize = compressedSize;
    this.crc = crc;
  }
}