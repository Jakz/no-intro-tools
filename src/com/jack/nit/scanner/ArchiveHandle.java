package com.jack.nit.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class ArchiveHandle extends RomHandle
{
  public final Path file;
  public final int indexInArchive;
  public final String internalName;
  public final ArchiveFormat format;
  public final long size;
  public final long compressedSize;
  
  private IInArchive archive;
  
  public ArchiveHandle(Path file, ArchiveFormat format, String internalName, Integer indexInArchive, long size, long compressedSize)
  {
    this.file = file.normalize();
    this.internalName = internalName;
    this.indexInArchive = indexInArchive;
    this.format = format;  
    this.size = size;
    this.compressedSize = compressedSize;
    this.archive = null;
  }
    
  protected IInArchive open()
  {
    if (archive != null)
      return archive;
    
    try
    {
      RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(file.toFile(), "r"));
      return SevenZip.openInArchive(null, rfile);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return null;
  }
  
  public final void forceOpen()
  {
    if (archive == null)
      archive = open();
  }
  
  public final void forceClose()
  {
    try
    {
      archive.close();
      archive = null;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    finally
    {
      archive = null;
    }
  }
  
  @Override public final boolean isArchive() { return true; }
  
  @Override public Path file() { return file; }
  @Override public String fileName() { return internalName; }
  
  @Override public String toString() { return file.getFileName().toString() + " ("+internalName+")"; }
  @Override public String plainName() { return file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf('.')); }
  @Override public String plainInternalName() { return internalName.substring(0, internalName.toString().lastIndexOf('.')); }
  @Override public String getInternalExtension() { return internalName.substring(internalName.toString().lastIndexOf('.')+1); }
  
  @Override public long size()
  {
    return size;
  }
  
  @Override public long compressedSize()
  {
    return compressedSize;
  }

  public boolean renameInternalFile(String newName)
  {       
    return false;
  }
  
  @Override
  public RomHandle relocate(Path file)
  {
    return new ArchiveHandle(file, format, this.internalName, this.indexInArchive, size, compressedSize);
  }
  
  @Override
  public RomHandle relocateInternal(String internalName)
  {
    return null;//new Zip7Handle(file, internalName);
  }
  
  @Override
  public InputStream getInputStream() throws IOException
  {
    final IInArchive archive = open();    
    final ExtractCallback callback = new ExtractCallback(archive, indexInArchive); 
    
    Runnable r = () -> {
      try
      {
        archive.extract(new int[] { indexInArchive }, false, callback);
        callback.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    };
    
    new Thread(r).start();
    
    return callback.stream.getInputStream();
  }
  
  public static class ArchivePipedInputStream extends PipedInputStream
  {
    private final IInArchive archive;
    private final int indexInArchive;
    
    public ArchivePipedInputStream(IInArchive archive, int indexInArchive)
    {
      super(1024*8);
      this.archive = archive;
      this.indexInArchive = indexInArchive;
    }

    public int getIndexInArchive() { return indexInArchive; }
    public IInArchive getArchive() { return archive; }
  }
  
  private class ExtractStream implements ISequentialOutStream
  {
    private ArchivePipedInputStream pis;
    private PipedOutputStream pos;
    

            
    ExtractStream(IInArchive archive, int indexInArchive) throws IOException
    {
      pis = new ArchivePipedInputStream(archive, indexInArchive);
      pos = new PipedOutputStream(pis);
    }
    
    @Override public int write(byte[] data)
    { 
      try
      {
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
    
    public void close() throws IOException { pos.close(); }
  }
  
  private class ExtractCallback implements IArchiveExtractCallback
  {
    private final IInArchive archive;
    private int index;
    private ExtractStream stream;
    
    ExtractCallback(IInArchive archive, int index)
    {
      this.archive = archive;
      this.index = index;
      try
      {
        this.stream = new ExtractStream(archive, index);
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    
    public void close() throws IOException { stream.close(); }
    
    public ISequentialOutStream getStream(int index, ExtractAskMode mode)
    {
      this.index = index;
      if (mode != ExtractAskMode.EXTRACT) return null;
      return stream;
    }
    
    public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException
    {
      
    }
    
    public void setOperationResult(ExtractOperationResult result) throws SevenZipException
    {

    }
    
    public void setCompleted(long completeValue) throws SevenZipException
    {
      
    }

    public void setTotal(long total) throws SevenZipException
    {
      
    }
  }
  
}