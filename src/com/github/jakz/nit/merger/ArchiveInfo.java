package com.github.jakz.nit.merger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jakz.nit.handles.RomHandle;

class ArchiveInfo
{
  public final String name;
  public final List<RomHandle> handles;
  
  ArchiveInfo(String name)
  {
    this.name = name;
    this.handles = new ArrayList<>();
  }
  
  ArchiveInfo(String name, RomHandle... handles)
  {
    this(name);
    this.handles.addAll(Arrays.asList(handles));
  }
  
  void relocate(Path dest)
  {
    handles.forEach(h -> h.relocate(dest));
    //TODO: should take care of internal name too
  }
}