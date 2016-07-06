package com.jack.nit.scanner;

import java.util.List;

public class RomHandlesSet
{
  final List<BinaryHandle> binaries;
  final List<ArchiveHandle> archives;
  
  RomHandlesSet(List<BinaryHandle> binaries, List<ArchiveHandle> archives)
  {
    this.binaries = binaries;
    this.archives = archives;
  }
}
