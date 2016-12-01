package com.jack.nit.handles;

import java.io.IOException;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;

public class ArchiveExtractCallback implements IArchiveExtractCallback
{
  private ArchiveExtractStream stream;
  
  public ArchiveExtractCallback(ArchiveExtractStream stream)
  {
    this.stream = stream;
  }
  
  public ArchiveExtractStream stream() { return stream; }
  
  public void close() throws IOException { stream.close(); }
  
  public ISequentialOutStream getStream(int index, ExtractAskMode mode)
  {
    if (mode != ExtractAskMode.EXTRACT) return null;
    return stream;
  }
  
  public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException
  {
    
  }
  
  public void setOperationResult(ExtractOperationResult result) throws SevenZipException
  {
     //System.out.println("Extract Stream finished");
  }
  
  public void setCompleted(long completeValue) throws SevenZipException
  {
    
  }

  public void setTotal(long total) throws SevenZipException
  {

  }
}