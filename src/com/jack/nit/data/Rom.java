package com.jack.nit.data;

import com.jack.nit.scanner.RomHandle;

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
  
  public void setHandle(RomHandle handle) { this.handle = handle; }
  public RomHandle handle() { return handle; }
  
  void setGame(Game game) { this.game = game; }
  public Game game() { return game; }
  
}
