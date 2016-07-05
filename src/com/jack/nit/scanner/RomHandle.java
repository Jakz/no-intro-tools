package com.jack.nit.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.stream.Stream;
import java.util.zip.*;

public abstract class RomHandle
{
  @Override
  public abstract String toString();
  
  public abstract Path file();
  public abstract String plainName();
  public abstract String plainInternalName();
  public abstract RomHandle relocate(Path file);
  public abstract RomHandle relocateInternal(String internalName);
  public abstract boolean isArchive();
  public abstract String getExtension();
  public abstract String getInternalExtension();
  public abstract InputStream getInputStream() throws IOException;
  public abstract long size();
  public abstract long uncompressedSize();
}
