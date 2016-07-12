package com.jack.nit.data;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.jack.nit.digest.DigestInfo;
import com.jack.nit.handles.RomHandle;

public class Rom
{
  public final String name;
  
  public final byte[] md5;
  public final byte[] sha1;
  public final long crc32;
  public final long size;
  
  private Game game;
  private RomHandle handle;
  
  public Rom(String name, long size, long crc32, byte[] md5, byte[] sha1)
  {
    this.name = name;
    this.size = size;
    this.crc32 = crc32;
    this.md5 = md5;
    this.sha1 = sha1;
    this.handle = null;
  }
  
  public Rom(String name, long size, DigestInfo info)
  {
    this(name, size, info.crc, info.md5, info.sha1);
  }
  
  public void setHandle(RomHandle handle) { this.handle = handle; }
  public RomHandle handle() { return handle; }
  
  void setGame(Game game) { this.game = game; }
  public Game game() { return game; }
  
  
  
  private static HexBinaryAdapter hexConverter = null;
  private static StringBuilder builder = null;
  
  @Override public String toString()
  { 
    if (hexConverter == null)
    {
      hexConverter = new HexBinaryAdapter();
      builder = new StringBuilder();
    }
    
    builder.delete(0, builder.length());
    builder.append("[").append(name).append(", size: ").append(size).append(", crc: ").append(Long.toHexString(crc32));
    if (md5 != null)
      builder.append(", md5: ").append(hexConverter.marshal(md5));
    if (sha1 != null)
      builder.append(", sha1: ").append(hexConverter.marshal(sha1));
    builder.append("]");
    
    return builder.toString();
  }
 
  public boolean isEquivalent(Rom rom)
  {
    return size == rom.size && crc32 == rom.crc32 && md5 == rom.md5 && sha1 == rom.sha1;
  }
  
}
