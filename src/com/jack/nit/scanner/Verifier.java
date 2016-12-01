package com.jack.nit.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import com.jack.nit.Options;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.HashCache;
import com.jack.nit.data.Rom;
import com.jack.nit.data.header.SkippingStream;
import com.jack.nit.digest.DigestInfo;
import com.jack.nit.digest.DigestOptions;
import com.jack.nit.digest.Digester;
import com.jack.nit.handles.MemoryArchive;
import com.jack.nit.handles.NestedArchiveHandle;
import com.jack.nit.handles.RomHandle;
import com.jack.nit.log.Log;
import com.pixbits.lib.stream.StreamException;

import net.sf.sevenzipjbinding.IInArchive;

public class Verifier
{
  private final Options options;
  private final HashCache cache;
  private final GameSet set;
  private final boolean multiThreaded;
  
  private float total;
  private AtomicInteger current = new AtomicInteger();
  
  private final Digester digester;
  
  
  public Verifier(Options options, GameSet set)
  {
    this.options = options;
    this.set = set;
    this.cache = set.cache();
    this.multiThreaded = options.multiThreaded;
    this.digester = new Digester(new DigestOptions(true, options.matchMD5, options.matchSHA1, options.multiThreaded));
  }
  
  public int verify(RomHandlesSet handles) throws IOException
  {
    Log.logger.startProgress("[INFO] Verifying roms...");
    current.set(0);
    total = handles.binaries.size() + handles.archives.size() + handles.nestedArchives.size();
    
    int found = 0;

    found = verify(handles.binaries) + verify(handles.archives);
    found += verifyNested(handles.nestedArchives);
      
    Log.logger.endProgress();
    
    return found;
  }
  
  private int verifyNested(List<List<NestedArchiveHandle>> archives) throws IOException
  {
    Stream<List<NestedArchiveHandle>> stream = archives.stream();
    AtomicInteger count = new AtomicInteger();
    
    final boolean onlyCRC = options.verifyJustCRC() && set.header == null;

    if (multiThreaded)
      stream = stream.parallel();
    
    stream.forEach(StreamException.rethrowConsumer(batch -> {
      MemoryArchive archive = null;
      IInArchive iarchive = null;
      for (NestedArchiveHandle handle : batch)
      {
        if (!onlyCRC)
        {
          if (archive == null)
          {
            handle.loadArchiveInMemory();
            archive = handle.getMemoryArchive();
            iarchive = handle.getMappedArchive();
          }
          else
          {
            archive.close();
            handle.setMemoryArchive(archive);
            handle.setMappedArchive(iarchive);
          }
        }
        
        Rom rom = verify(handle);
        
        if (rom != null)
        {
          rom.setHandle(handle);
          count.incrementAndGet();
        }
      }
      
      batch.stream().forEach(handle -> { handle.setMemoryArchive(null); handle.setMappedArchive(null); });
    }));
    
    return count.get();
  }
  
  private Rom verifyRawInputStream(RomHandle handle, InputStream is) throws IOException, NoSuchAlgorithmException
  {
    DigestInfo info = digester.digest(handle, is, set.header != null);
    
    Rom rom = cache.romForCrc(info.crc);
    
    return rom != null && (info.md5 == null || Arrays.equals(info.md5, rom.md5)) && (info.sha1 == null || Arrays.equals(info.sha1, rom.sha1)) ? rom : null;
  }
  
  private Rom verifyJustCRC(RomHandle handle)
  {
    return cache.romForCrc(digester.digestOnlyCRC(handle).crc);
  }
  
  private Rom verify(RomHandle handle) throws IOException, NoSuchAlgorithmException
  {       
    Rom rom = null;
    
    if (options.verifyJustCRC() && set.header == null)
    {
      rom = verifyJustCRC(handle);
    }
    else
    {
      try (InputStream is = handle.getInputStream())
      {
        if (set.header != null)
          rom = verifyRawInputStream(handle, new SkippingStream(is, new byte[] { 0x46, 0x44, 0x53 }, 16));
        else
          rom = verifyRawInputStream(handle, is);
      }
    }
    
    return rom;
  }
  
  private int verify(List<? extends RomHandle> handles) throws IOException
  {
    Stream<? extends RomHandle> stream = handles.stream();
    
    AtomicInteger count = new AtomicInteger();
    
    if (multiThreaded)
      stream = stream.parallel();
        
    stream.forEach(StreamException.rethrowConsumer(path -> {      
      Log.logger.updateProgress(current.getAndIncrement() / total, path.file().getFileName().toString());
      Rom rom = verify(path);

      if (rom != null)
      {
        rom.setHandle(path);
        count.incrementAndGet();
      }
    }));
    
    return count.get();
  }
}
