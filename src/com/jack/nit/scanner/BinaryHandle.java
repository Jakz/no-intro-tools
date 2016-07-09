package com.jack.nit.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.pixbits.io.FileUtils;

public class BinaryHandle extends RomHandle
{
  public final Path file;
  private long crc;

  public BinaryHandle(Path file)
  {
    this.file = file.normalize();
    this.crc = -1;
  }
  
  @Override public Path file() { return file; }
  @Override public String fileName() { return file().toString(); }
  
  @Override
  public String toString() { return file.getFileName().toString(); }
  @Override
  public String plainName() { return file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf('.')); }
  @Override
  public String plainInternalName() { return plainName(); }
  
  @Override public long size() {
    try
    {
      return Files.size(file);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return 0;
    }
  }
  
  @Override public long compressedSize() { return size(); }
  
  @Override public long crc()
  {
    if (crc != -1)
      return crc;
    else
      try
      { 
        crc = FileUtils.calculateCRCFast(file()); 
        return crc;
      } catch (IOException e) 
      { 
        e.printStackTrace(); 
        return -1;
      }
  }
  
  
  @Override public boolean isArchive() { return false; }

  @Override public String getInternalExtension() { return getExtension(); }
  
  @Override
  public RomHandle relocate(Path file)
  {
    return new BinaryHandle(file);
  }
  
  @Override
  public RomHandle relocateInternal(String internalName)
  {
    throw new UnsupportedOperationException("a binary rompath doesn't have an internal filename");
  }

  @Override
  public InputStream getInputStream() throws IOException
  {
    return Files.newInputStream(file);
  }
}