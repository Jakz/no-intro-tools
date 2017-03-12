package com.github.jakz.nit.digest;

public class DigestOptions
{
  final boolean multiThreaded;
  final boolean computeCRC;
  final boolean computeMD5;
  final boolean computeSHA1;
  
  public DigestOptions(boolean crc, boolean md5, boolean sha1, boolean multiThreaded)
  {
    this.computeCRC = crc;
    this.computeMD5 = md5;
    this.computeSHA1 = sha1;
    this.multiThreaded = multiThreaded;
  }
}
