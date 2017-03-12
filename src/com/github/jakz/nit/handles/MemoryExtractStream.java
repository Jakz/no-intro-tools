package com.github.jakz.nit.handles;

import java.io.IOException;
import java.io.InputStream;

import com.pixbits.lib.io.PipedOutputStream;

import net.sf.sevenzipjbinding.IInArchive;

class MemoryExtractStream implements ArchiveExtractStream
{  
  private MemoryArchive archive;

  MemoryExtractStream(int size) throws IOException
  {
    this.archive = new MemoryArchive(size);
  }
  
  @Override public int write(byte[] data)
  { 
    archive.write(data);
    return data.length;
  }
  
  @Override
  public void close() throws IOException { 
    
  }
}