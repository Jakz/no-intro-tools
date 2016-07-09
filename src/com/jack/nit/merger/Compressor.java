package com.jack.nit.merger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Arrays;

import com.jack.nit.Options;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.scanner.RomHandle;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IOutCreateArchive;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.IOutItemBase;
import net.sf.sevenzipjbinding.IOutItemZip;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.InputStreamSequentialInStream;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;

public class Compressor
{ 
  Options options;
  
  public Compressor(Options options)
  {
    this.options = options;
  }
  
  
  
  private static interface ItemDecorator<T>
  {
    public void decorate(T item, RomHandle handle);
  }
  
  private static class ItemDecorator7z implements ItemDecorator<IOutItem7z>
  {
    public void decorate(IOutItem7z item, RomHandle handle)
    {
      item.setDataSize(handle.size());
      item.setPropertyPath(handle.fileName());
    }
  }
  
  private static class ItemDecoratorZip implements ItemDecorator<IOutItemZip>
  {
    public void decorate(IOutItemZip item, RomHandle handle)
    {
      item.setDataSize(handle.size());
      item.setPropertyPath(handle.fileName());
    }
  }
  
  private static final class CreateCallback<T extends IOutItemBase> implements IOutCreateCallback<T>
  {
    private final RomHandle[] handles;
    private final long totalSize;
    private final ItemDecorator<T> decorator;
    
    CreateCallback(RomHandle[] handles, ItemDecorator<T> decorator)
    {
      this.handles = handles;
      totalSize = Arrays.stream(handles).mapToLong(RomHandle::size).sum();
      this.decorator = decorator;
    }
    
    @Override public void setTotal(long total) throws SevenZipException
    {
      
    }

    @Override public void setCompleted(long complete) throws SevenZipException
    {
      Logger.logger.updateProgress(complete/(float)totalSize, "");
    }

    @Override
    public void setOperationResult(boolean operationResultOk) throws SevenZipException
    {
      // progress callback 
    }

    @Override
    public T getItemInformation(int index, OutItemFactory<T> factory) throws SevenZipException
    {
      T item = factory.createOutItem();
      
      RomHandle handle = handles[index];
      
      decorator.decorate(item, handle);

      Logger.log(Log.DEBUG, " [MERGER] Preparing item %s (%d bytes) to be archived", handle.fileName(), handle.size());
      
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
  
  public void createArchive(Path dest, RomHandle... handles) throws FileNotFoundException, SevenZipException
  {
    RandomAccessFile raf = new RandomAccessFile(dest.toFile(), "rw");
        
    Logger.logger.startProgress("Creating archive "+dest.toString());

    switch (options.archiveFormat)
    {
      case _7ZIP:
      {
        IOutCreateArchive7z archive = SevenZip.openOutArchive7z();
        archive.setThreadCount(0);
        archive.setLevel(options.compressionLevel);
        boolean solid = options.useSolidArchives;
        archive.setSolid(solid);
        if (solid)
        {
          archive.setSolidFiles(handles.length);
          archive.setSolidSize(Arrays.stream(handles).mapToLong(RomHandle::size).sum());
        }
        
        CreateCallback<IOutItem7z> callback = new CreateCallback<IOutItem7z>(handles, new ItemDecorator7z());
        archive.createArchive(new RandomAccessFileOutStream(raf), handles.length, callback);
        
        break;
      }
    }
    
    Logger.logger.endProgress();
  }
}
