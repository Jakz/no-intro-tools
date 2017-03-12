package com.github.jakz.nit.scanner;

import java.util.List;

import com.github.jakz.nit.handles.ArchiveHandle;
import com.github.jakz.nit.handles.BinaryHandle;
import com.github.jakz.nit.handles.NestedArchiveHandle;

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
