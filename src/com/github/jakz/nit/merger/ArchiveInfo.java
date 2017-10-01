package com.github.jakz.nit.merger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.pixbits.lib.io.archive.handles.Handle;
import com.pixbits.lib.lang.Pair;

class ArchiveInfo
{
  public final String name;
  public final List<ArchiveEntry> entries;
  
  ArchiveInfo(String name)
  {
    this.name = name;
    this.entries = new ArrayList<>();
  }
  
  ArchiveInfo(String name, ArchiveEntry... entries)
  {
    this(name);
    this.entries.addAll(Arrays.asList(entries));
  }

  void add(ArchiveEntry entries)
  {
    this.entries.add(entries);
  }
  
  void add(Collection<ArchiveEntry> entries)
  {
    this.entries.addAll(entries);
  }
  
  public int size() { return entries.size(); }
  List<ArchiveEntry> entries() { return entries; }
  Stream<ArchiveEntry> stream() { return entries.stream(); }
  
  void relocate(Path dest)
  {
    entries.forEach(h -> h.handle().relocate(dest));
    //TODO: should take care of internal name too
  }
}