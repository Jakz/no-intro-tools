package com.jack.nit.digest;

public class DigestInfo
{
  public final long crc;
  public final byte[] md5;
  public final byte[] sha1;
  
  public DigestInfo(long crc, byte[] md5, byte[] sha1)
  {
    this.crc = crc;
    this.md5 = md5;
    this.sha1 = sha1;
  }
}
