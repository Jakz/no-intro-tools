package com.github.jakz.nit.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.github.jakz.nit.Options;
import com.github.jakz.nit.Settings;
import com.github.jakz.nit.config.VerifierOptions;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.nit.data.HashCache;
import com.github.jakz.nit.data.Rom;
import com.github.jakz.nit.data.header.SkippingStream;
import com.github.jakz.nit.handles.MemoryArchive;
import com.github.jakz.nit.handles.NestedArchiveHandle;
import com.github.jakz.nit.handles.Handle;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.digest.DigestInfo;
import com.pixbits.lib.io.digest.DigestOptions;
import com.pixbits.lib.io.digest.Digester;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;

import net.sf.sevenzipjbinding.IInArchive;

public class Verifier
{
  private static final Logger logger = Log.getLogger(Verifier.class);
  
  
  private final VerifierOptions voptions;
  private final HashCache cache;
  private final GameSet set;
  private final boolean multiThreaded;
  
  private float total;
  private AtomicInteger current = new AtomicInteger();
  
  private final Digester digester;
  
  
  public Verifier(Options options, GameSet set)
  {
    this.voptions = options.verifier;
    this.set = set;
    this.cache = set.cache();
    this.multiThreaded = options.multiThreaded;
    this.digester = new Digester(new DigestOptions(Settings.DIGEST_BUFFER_SIZE, true, voptions.matchMD5, voptions.matchSHA1, options.multiThreaded));
  }
  
  public int verify(RomHandleSet handles) throws IOException
  {
    logger.startProgress(Log.INFO1, "Verifying roms...");
    current.set(0);
    total = handles.binaries.size() + handles.archives.size() 
      + handles.nestedArchives.stream().mapToInt(List::size).sum();
    
    int found = 0;

    found = verify(handles.binaries) + verify(handles.archives);
    found += verifyNested(handles.nestedArchives);
      
    logger.endProgress();
    
    return found;
  }
  
  private int verifyNested(List<List<NestedArchiveHandle>> archives) throws IOException
  {
    Stream<List<NestedArchiveHandle>> stream = archives.stream();
    AtomicInteger count = new AtomicInteger();
    
    final boolean onlyCRC = voptions.verifyJustCRC() && set.header == null;

    if (multiThreaded)
      stream = stream.parallel();
    
    stream.forEach(StreamException.rethrowConsumer(batch -> {
      MemoryArchive archive = null;
      IInArchive iarchive = null;
      for (NestedArchiveHandle handle : batch)
      {
        logger.updateProgress(current.getAndIncrement() / total, handle.nestedInternalName);

        
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
  
  private Rom verifyRawInputStream(Handle handle, InputStream is) throws IOException, NoSuchAlgorithmException
  {
    DigestInfo info = digester.digest(set.header != null ? handle : null, is);
    
    Rom rom = cache.romForCrc(info.crc);
    
    return rom != null && (info.md5 == null || Arrays.equals(info.md5, rom.md5)) && (info.sha1 == null || Arrays.equals(info.sha1, rom.sha1)) ? rom : null;
  }
  
  private Rom verifyJustCRC(Handle handle)
  {
    return cache.romForCrc(digester.digestOnlyCRC(handle).crc);
  }
  
  private Rom verify(Handle handle) throws IOException, NoSuchAlgorithmException
  {       
    Rom rom = null;
    
    if (voptions.verifyJustCRC() && set.header == null)
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
  
  private int verify(List<? extends Handle> handles) throws IOException
  {
    Stream<? extends Handle> stream = handles.stream();
    
    AtomicInteger count = new AtomicInteger();
    
    if (multiThreaded)
      stream = stream.parallel();
        
    stream.forEach(StreamException.rethrowConsumer(path -> {      
      logger.updateProgress(current.getAndIncrement() / total, path.file().getFileName().toString());
      Rom rom = verify(path);
      
      if (rom != null)
      {
        if (rom.handle() != null)
        {
          logger.w("Duplicate ROM found for %s: %s", rom.name, path.toString());
        }
        else
        {
          rom.setHandle(path);
          count.incrementAndGet();
        }
      }
    }));
    
    return count.get();
  }
}
