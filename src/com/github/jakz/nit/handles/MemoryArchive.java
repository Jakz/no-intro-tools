package com.github.jakz.nit.handles;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.github.jakz.nit.Settings;
import com.github.jakz.nit.scanner.ExtractionCanceledException;
import com.github.jakz.nit.scanner.FormatUnrecognizedException;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISeekableStream;
import net.sf.sevenzipjbinding.SevenZip;

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
  
  public void readFromInputStream(InputStream is) throws IOException
  {
    while (offset < size)
    {
      int stillToRead = Math.min(8192, size - offset);
      offset += is.read(data, offset, stillToRead);
    }
  }
  
  public IInArchive open(ArchiveFormat format) throws IOException, FormatUnrecognizedException
  {
    try
    {
      IInArchive archive = SevenZip.openInArchive(format, this);
      
      if (archive.getArchiveFormat() == null)
        throw new FormatUnrecognizedException(null, "Nested archive format unrecognized");
      
      return archive;
    }
    catch (IOException e)
    {
      throw new FormatUnrecognizedException(null, "Nested Archive format unrecognized");
    }
  }
  
  public static MemoryArchive load(IInArchive archive, int index, int size) throws IOException
  {
    MemoryArchive memoryArchive = new MemoryArchive(size);

    final ArchiveExtractPipedStream stream = new ArchiveExtractPipedStream(archive, index);
    final ArchiveExtractCallback callback = new ArchiveExtractCallback(stream); 

    Runnable r = () -> {
      try
      {
        archive.extract(new int[] { index }, false, callback);
        callback.close();
      }
      catch (ExtractionCanceledException e) { }
      catch (IOException e) { e.printStackTrace(); }
    };
    
    new Thread(r).start();

    memoryArchive.readFromInputStream(stream.getInputStream());    
    memoryArchive.close();
    return memoryArchive;
  }

}
