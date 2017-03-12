package com.github.jakz.nit.handles;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;

import com.github.jakz.nit.Settings;
import com.github.jakz.nit.scanner.ExtractionCanceledException;
import com.github.jakz.nit.scanner.FormatUnrecognizedException;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class NestedArchiveHandle extends RomHandle
{
  private static final NestedArchiveCache cache = new NestedArchiveCache();
  
  private Path file;
  public final int indexInArchive;
  public final String internalName;
  public final ArchiveFormat format;
  
  public final int nestedIndexInArchive;
  public final String nestedInternalName;
  public final ArchiveFormat nestedFormat;
  
  public final long size;
  public final long compressedSize;
  public final long crc;
  
  private MemoryArchive innerArchive;
  private IInArchive mappedArchive;
  
  public NestedArchiveHandle(Path file, ArchiveFormat format, String internalName, Integer indexInArchive, ArchiveFormat nestedFormat, String nestedInternalName, int nestedIndexInArchive, long size, long compressedSize, long crc)
  {
    this.file = file.normalize();
    
    this.internalName = internalName;
    this.indexInArchive = indexInArchive;
    this.format = format;
    
    this.nestedIndexInArchive = nestedIndexInArchive;
    this.nestedInternalName = nestedInternalName;
    this.nestedFormat = nestedFormat;
    
    this.size = size;
    this.compressedSize = compressedSize;
    this.innerArchive = null;
    this.mappedArchive = null;
    this.crc = crc;
  }
  
  public MemoryArchive getMemoryArchive() { return innerArchive; }
  public void setMemoryArchive(MemoryArchive archive) { this.innerArchive = archive; } 
  
  public IInArchive getMappedArchive() { return mappedArchive; }
  public void setMappedArchive(IInArchive archive) { this.mappedArchive = archive; }
    
  public void loadArchiveInMemory()
  {
    try
    {      
      RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(file.toFile(), "r"));
      IInArchive archive = SevenZip.openInArchive(Settings.guessFormatForFilename(file.getFileName().toString()), rfile);
      int outerSize = (int)(long)archive.getProperty(indexInArchive, PropID.SIZE);
      innerArchive = MemoryArchive.load(archive, indexInArchive, outerSize);
      ArchiveFormat format = Settings.guessFormatForFilename(internalName);
      mappedArchive = innerArchive.open(format);
      archive.close();
      innerArchive.close();
    }
    catch (IOException|FormatUnrecognizedException e)
    {
      e.printStackTrace();
    }
  }
  
  protected IInArchive open()
  {
    if (innerArchive == null || mappedArchive == null)
      loadArchiveInMemory();
    
    return mappedArchive;
  }
  
  @Override public final boolean isArchive() { return true; }
  
  @Override public Path file() { return file; }
  @Override public String fileName() { return internalName; }
  
  @Override public String toString() { return file.getFileName().toString() + " ("+internalName+")"; }
  @Override public String plainName() { return file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf('.')); }
  @Override public String plainInternalName() { return internalName.substring(0, internalName.toString().lastIndexOf('.')); }
  @Override public String getInternalExtension() { return internalName.substring(internalName.toString().lastIndexOf('.')+1); }
  
  @Override public long size() { return size; }
  @Override public long compressedSize() { return compressedSize; }
  @Override public long crc() { return crc; }
  
  

  public boolean renameInternalFile(String newName)
  {       
    return false;
  }
  
  @Override
  public void relocate(Path file)
  {
    this.file = file;
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
    final ArchiveExtractPipedStream stream = new ArchiveExtractPipedStream(archive, nestedIndexInArchive);
    final ArchiveExtractCallback callback = new ArchiveExtractCallback(stream); 
    
    Runnable r = () -> {
      //System.out.println("Extract Thread Started");
      try
      {
        archive.extract(new int[] { nestedIndexInArchive }, false, callback);
        callback.close();
      }
      catch (ExtractionCanceledException e)
      {
        
      }
      catch (IOException e)
      {
        System.err.println(String.format("Exception while extracting file %s from nested archive %s (index: %d) contained in %s (index: %d)", 
            nestedInternalName, internalName, nestedIndexInArchive, file.getFileName().toString(), indexInArchive)); 
        
        e.printStackTrace();
      }
      //System.out.println("Extract Thread Stopped");
    };
    
    new Thread(r).start();
 
    return stream.getInputStream();
  }

  
}