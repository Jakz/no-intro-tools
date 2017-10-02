package com.github.jakz.nit.merger;

import java.io.IOException;
import java.io.InputStream;

import com.pixbits.lib.io.archive.Compressible;
import com.pixbits.lib.io.archive.handles.Handle;

public class ArchiveEntry implements Compressible
{
  private final Handle handle;
  private final String fileName;
  
  public ArchiveEntry(Handle handle)
  {
    this(handle, handle.plainInternalName());
  }
  
  public ArchiveEntry(Handle handle, String fileName)
  {
    this.handle = handle;
    this.fileName = fileName;
  }
  
  public Handle handle() { return handle; }
   
  @Override public String fileName() { return fileName; }
  @Override public long size() { return handle.size(); }
  @Override public InputStream getInputStream() throws IOException { return handle.getInputStream(); }

}
