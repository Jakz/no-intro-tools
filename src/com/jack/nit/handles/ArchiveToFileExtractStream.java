package com.jack.nit.handles;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.jack.nit.Settings;
import com.pixbits.io.PipedOutputStream;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;

class ArchiveToFileExtractStream implements ArchiveExtractStream
{
  private BufferedOutputStream bos;

  ArchiveToFileExtractStream(Path dest) throws IOException
  {
    bos = new BufferedOutputStream(Files.newOutputStream(dest), Settings.PIPED_BUFFER_SIZE);
  }
  
  @Override public synchronized int write(byte[] data)
  { 
    try
    {
      bos.write(data);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return data.length;
  }

  @Override public void close() throws IOException
  { 
    bos.close();
  }
}