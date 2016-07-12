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
import com.jack.nit.digest.DigestInfo;
import com.jack.nit.digest.DigestOptions;
import com.jack.nit.digest.Digester;
import com.jack.nit.handles.RomHandle;
import com.jack.nit.log.Logger;
import com.pixbits.stream.StreamException;

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
    DigestInfo info = digester.digest(handle, is);
    
    Rom rom = cache.romForCrc(info.crc);
    
    return rom != null && (info.md5 == null || Arrays.equals(info.md5, rom.md5)) && (info.sha1 == null || Arrays.equals(info.sha1, rom.sha1)) ? rom : null;
  }
  
  private Rom verifyJustCRC(RomHandle handle)
  {
    return cache.romForCrc(digester.digestOnlyCRC(handle).crc);
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
