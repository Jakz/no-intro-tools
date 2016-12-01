package com.jack.nit.scanner;

import java.util.List;

import com.jack.nit.handles.ArchiveHandle;
import com.jack.nit.handles.BinaryHandle;
import com.jack.nit.handles.NestedArchiveHandle;

public class RomHandlesSet
{
  final List<BinaryHandle> binaries;
  final List<ArchiveHandle> archives;
  final List<List<NestedArchiveHandle>> nestedArchives;
  
  RomHandlesSet(List<BinaryHandle> binaries, List<ArchiveHandle> archives, List<List<NestedArchiveHandle>> nestedArchives)
  {
    this.binaries = binaries;
    this.archives = archives;
    this.nestedArchives = nestedArchives;
  }
}
