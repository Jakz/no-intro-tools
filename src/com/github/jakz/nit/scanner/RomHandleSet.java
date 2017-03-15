package com.github.jakz.nit.scanner;

import java.util.List;

import com.pixbits.lib.io.archive.handles.ArchiveHandle;
import com.pixbits.lib.io.archive.handles.BinaryHandle;
import com.pixbits.lib.io.archive.handles.NestedArchiveHandle;

public class RomHandleSet
{
  final List<BinaryHandle> binaries;
  final List<ArchiveHandle> archives;
  final List<List<NestedArchiveHandle>> nestedArchives;
  
  RomHandleSet(List<BinaryHandle> binaries, List<ArchiveHandle> archives, List<List<NestedArchiveHandle>> nestedArchives)
  {
    this.binaries = binaries;
    this.archives = archives;
    this.nestedArchives = nestedArchives;
  }
}
