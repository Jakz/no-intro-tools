package com.github.jakz.nit.handles;

import java.io.IOException;
import java.io.InputStream;

import com.pixbits.lib.io.PipedOutputStream;

import net.sf.sevenzipjbinding.IInArchive;

public class ArchiveExtractPipedStream implements ArchiveExtractStream
{
  private ArchivePipedInputStream pis;
  private PipedOutputStream pos;

  public ArchiveExtractPipedStream(IInArchive archive, int indexInArchive) throws IOException
  {
    pis = new ArchivePipedInputStream(archive, indexInArchive);
    pos = new PipedOutputStream(pis);
  }
  
  @Override public int write(byte[] data)
  { 
    try
    {
      //System.out.println("PipedOutput::write "+data.length+" "+Thread.currentThread().getName());
      pos.write(data);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return data.length;
  }
  
  public InputStream getInputStream()
  {
    return pis;
  }
  
  @Override
  public void close() throws IOException { 
    pos.close();
  }
}