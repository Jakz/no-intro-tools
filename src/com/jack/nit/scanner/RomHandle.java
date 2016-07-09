package com.jack.nit.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public abstract class RomHandle
{
  @Override
  public abstract String toString();
  
  public abstract Path file();
  public abstract String fileName();
  
  public abstract String plainName();
  public abstract String plainInternalName();
  public abstract RomHandle relocate(Path file);
  public abstract RomHandle relocateInternal(String internalName);
  public abstract boolean isArchive();
  
  public String getExtension() {
    String filename = file().getFileName().toString();
    int lastdot = filename.lastIndexOf('.');
    return lastdot != -1 ? filename.substring(lastdot+1) : "";
  }
  
  public abstract String getInternalExtension();
  public abstract InputStream getInputStream() throws IOException;
  
  public abstract long crc();
  public abstract long size();
  public abstract long compressedSize();
}
