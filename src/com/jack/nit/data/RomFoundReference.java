package com.jack.nit.data;

import com.jack.nit.scanner.RomHandle;

public class RomFoundReference
{
  public final RomReference rom;
  public final RomHandle handle;
  
  public RomFoundReference(RomReference rom, RomHandle handle)
  {
    this.rom = rom;
    this.handle = handle;
  }
}
