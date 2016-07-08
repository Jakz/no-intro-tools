package com.jack.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;

import com.jack.nit.scanner.RomHandle;
import com.jack.nit.scanner.ArchiveHandle.ArchivePipedInputStream;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.InputStreamSequentialInStream;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;

public class Compressor7Zip
{ 
  private static final class CreateCallback implements IOutCreateCallback<IOutItem7z>
  {
    private final RomHandle[] handles;
    
    CreateCallback(RomHandle[] handles)
    {
      this.handles = handles;
    }
    
    @Override public void setTotal(long total) throws SevenZipException
    {
      // progress callback
      
    }

    @Override public void setCompleted(long complete) throws SevenZipException
    {
      // progress callback
      
    }

    @Override
    public void setOperationResult(boolean operationResultOk) throws SevenZipException
    {
      // progress callback 
    }

    @Override
    public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> factory) throws SevenZipException
    {
      IOutItem7z item = factory.createOutItem();
      
      RomHandle handle = handles[index];
      
      item.setDataSize(handle.size());
      item.setPropertyPath(handle.file().getFileName().toString());
      
      return item;
    }

    @Override
    public ISequentialInStream getStream(int index) throws SevenZipException
    {
      try
      {
        return new InputStreamSequentialInStream(handles[index].getInputStream());
      }
      catch (IOException e)
      {
        e.printStackTrace();
        return null;
      }
    }
  }
  
  public static void createArchive(Path dest, RomHandle[] handles, int level, boolean solid) throws FileNotFoundException, SevenZipException
  {
    CreateCallback callback = new CreateCallback(handles);

    RandomAccessFile raf = null;
    IOutCreateArchive7z archive = null;
    
    raf = new RandomAccessFile(dest.toFile(), "rw");
    
    archive = SevenZip.openOutArchive7z();
    archive.setLevel(level);
    archive.setSolid(true);
    archive.setSolidFiles(handles.length);
    archive.setSolidSize(Arrays.stream(handles).mapToLong(RomHandle::size).sum());
    archive.setThreadCount(0);
    
    archive.createArchive(new RandomAccessFileOutStream(raf), handles.length, callback);
  }
}
