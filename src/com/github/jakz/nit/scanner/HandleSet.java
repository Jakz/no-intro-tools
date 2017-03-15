package com.github.jakz.nit.scanner;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import com.pixbits.lib.io.archive.handles.ArchiveHandle;
import com.pixbits.lib.io.archive.handles.BinaryHandle;
import com.pixbits.lib.io.archive.handles.NestedArchiveHandle;

public class HandleSet
{
  final List<BinaryHandle> binaries;
  final List<ArchiveHandle> archives;
  final List<List<NestedArchiveHandle>> nestedArchives;
  final Set<Path> faultyArchives;
  
  HandleSet(List<BinaryHandle> binaries, List<ArchiveHandle> archives, List<List<NestedArchiveHandle>> nestedArchives, Set<Path> faultyArchives)
  {
    this.binaries = binaries;
    this.archives = archives;
    this.nestedArchives = nestedArchives;
    this.faultyArchives = faultyArchives;
  }
}
