package com.jack.nit.scanner;

import java.util.List;

public class RomHandlesSet
{
  final List<RomHandle> binaries;
  final List<RomHandle> archives;
  
  RomHandlesSet(List<RomHandle> binaries, List<RomHandle> archives)
  {
    this.binaries = binaries;
    this.archives = archives;
  }
}
