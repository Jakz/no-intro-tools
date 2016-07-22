package com.jack.nit.handles;

import java.util.Arrays;

import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISeekableStream;

public class MemoryArchive implements IInStream
{
  private final byte[] data;
  private final int size;
  
  private int offset;
  
  public MemoryArchive(int size)
  {
    data = new byte[size];
    this.size = size;
    this.offset = 0;
  }
  
  public void write(byte[] data)
  {
    System.arraycopy(data, 0, this.data, offset, data.length);
    offset += data.length;
  }
  
  public void close()
  {
    offset = 0;
  }
  
  public int read(byte[] data)
  {
    System.arraycopy(this.data, offset, data, 0, data.length);
    offset += data.length;
    return data.length;
  }
  
  public long seek(long offset, int seekOrigin)
  {
    if (seekOrigin == ISeekableStream.SEEK_SET)
      this.offset = (int)offset;
    else if (seekOrigin == ISeekableStream.SEEK_CUR)
      this.offset += offset;
    else if (seekOrigin == ISeekableStream.SEEK_END)
      this.offset = (int)(size - offset);
      
    return this.offset;
  }

}
