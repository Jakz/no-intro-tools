package com.jack.nit.scanner;

import java.util.List;

import com.jack.nit.handles.ArchiveHandle;
import com.jack.nit.handles.BinaryHandle;

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
