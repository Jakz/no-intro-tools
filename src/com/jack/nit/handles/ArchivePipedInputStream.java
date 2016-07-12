package com.jack.nit.handles;

import java.io.IOException;

import com.jack.nit.Settings;
import com.pixbits.io.PipedInputStream;

import net.sf.sevenzipjbinding.IInArchive;

public class ArchivePipedInputStream extends PipedInputStream
{
  private final IInArchive archive;
  private final int indexInArchive;
  
  public ArchivePipedInputStream(IInArchive archive, int indexInArchive)
  {
    super(Settings.PIPED_BUFFER_SIZE);
    this.archive = archive;
    this.indexInArchive = indexInArchive;
  }
  
  @Override public synchronized int read(byte[] data) throws IOException
  {
    int i = super.read(data);
    //System.out.println("PipedInput::read "+i+" "+Thread.currentThread().getName());
    return i;
  }

  public int getIndexInArchive() { return indexInArchive; }
  public IInArchive getArchive() { return archive; }
}