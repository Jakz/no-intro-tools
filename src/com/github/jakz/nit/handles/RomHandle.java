package com.github.jakz.nit.handles;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.pixbits.lib.io.digest.DigestedCRC;

public abstract class RomHandle implements DigestedCRC
{
  @Override
  public abstract String toString();
  
  public abstract Path file();
  public abstract String fileName();
  
  public abstract String plainName();
  public abstract String plainInternalName();
  public abstract void relocate(Path file);
  public abstract RomHandle relocateInternal(String internalName);
  public abstract boolean isArchive();
  
  public String getExtension() {
    String filename = file().getFileName().toString();
    int lastdot = filename.lastIndexOf('.');
    return lastdot != -1 ? filename.substring(lastdot+1) : "";
  }
  
  public abstract String getInternalExtension();
  public abstract InputStream getInputStream() throws IOException;
  
  /*
   * @return crc returns crc32 for handle, this operation caches the value
   */
  public abstract long crc();
  
  /**
   * @return size in bytes of the handle
   */
  public abstract long size();
  /**
   * @return compressed size in bytes of the handle, corresponds to <code>size()</code> for binary handles
   */
  public abstract long compressedSize();
  
  @Override public long getCRC32() { return crc(); }
}
