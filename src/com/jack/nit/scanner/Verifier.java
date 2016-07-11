package com.jack.nit.scanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import com.jack.nit.Options;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.HashCache;
import com.jack.nit.data.Rom;
import com.jack.nit.log.Logger;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.PropID;

public class Verifier
{
  private final Options options;
  private final HashCache cache;
  private final GameSet set;
  private final boolean multiThreaded;
  private final byte[] buffer = new byte[8192];
  
  private float total;
  private AtomicInteger current = new AtomicInteger();;
  
  
  public Verifier(Options options, GameSet set)
  {
    this.options = options;
    this.set = set;
    this.cache = set.cache();
    this.multiThreaded = options.multiThreaded;
  }
  
  public int verify(RomHandlesSet handles) throws IOException
  {
    if (set.header != null)
      throw new UnsupportedOperationException("romsets with headers are not supported yed");
    
    Logger.logger.startProgress("[INFO] Verifying roms...");
    current.set(0);
    total = handles.binaries.size() + handles.archives.size();
    
    int found = verifyNoHeader(handles.binaries) + verifyNoHeader(handles.archives);
    
    Logger.logger.endProgress();
    
    return found;
  }
  
  private Rom verifyRawInputStream(RomHandle handle, InputStream is) throws IOException, NoSuchAlgorithmException
  {
    final byte[] buffer = multiThreaded ? new byte[8192] : this.buffer;

    boolean computeRealCRC = !handle.isArchive();
    
    InputStream fis = new BufferedInputStream(is);
    CheckedInputStream crc = null;
    MessageDigest md5 = null;
    MessageDigest sha1 = null;
    boolean realRead = false;
    
    Rom rom = null;
    
    if (computeRealCRC)
    {
      crc = new CheckedInputStream(is, new CRC32());
      fis = crc;
      realRead = true;
    }
    
    if (options.matchMD5)
    {
      md5 = MessageDigest.getInstance("MD5");
      fis = new DigestInputStream(fis, md5);
      realRead = true;
    }
    
    if (options.matchSHA1)
    {
      sha1 = MessageDigest.getInstance("SHA-1");
      fis = new DigestInputStream(fis, sha1);
      realRead = true;
    }
    
    
    if (realRead)
      for (; fis.read(buffer) > 0; );
    else
      is.close();

    //TODO: maybe there are multple roms with same CRC32
    if (computeRealCRC)
      rom = cache.romForCrc(crc.getChecksum().getValue());
    else if (handle.isArchive())
    {
      ArchiveHandle.ArchivePipedInputStream pis = (ArchiveHandle.ArchivePipedInputStream)is;
      rom = cache.romForCrc((long)(int)pis.getArchive().getProperty(pis.getIndexInArchive(), PropID.CRC));
    }
      
    
    if (options.matchMD5 && rom != null)
    {
      byte[] computedMD5 = md5.digest();
      
      if (!Arrays.equals(computedMD5, rom.md5))
        rom = null;
    }
    
    if (options.matchSHA1 && rom != null)
    {
      byte[] computedSHA1 = sha1.digest();
      
      if (!Arrays.equals(computedSHA1, rom.sha1))
        rom = null;
    }
    
    if (crc != null)
      crc.close();
    
    return rom;
  }
  
  private Rom verifyJustCRC(RomHandle handle)
  {
    return cache.romForCrc(handle.crc());
  }
  
  private int verifyNoHeader(List<? extends RomHandle> handles) throws IOException
  {
    Stream<? extends RomHandle> stream = handles.stream();
    
    AtomicInteger count = new AtomicInteger();
    
    if (multiThreaded)
      stream = stream.parallel();
        
    stream.forEach(StreamException.rethrowConsumer(path -> {      
      Logger.logger.updateProgress(current.getAndIncrement() / total, path.file().getFileName().toString());
      
      Rom rom = null;
      
      if (options.verifyJustCRC())
      {
        rom = verifyJustCRC(path);
      }
      else
      {
        try (InputStream is = path.getInputStream())
        {
          rom = verifyRawInputStream(path, is);
        }
      }

      if (rom != null)
      {
        rom.setHandle(path);
        count.incrementAndGet();
      }
    }));
    
    return count.get();
  }
}
