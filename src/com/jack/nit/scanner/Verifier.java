package com.jack.nit.scanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.HashCache;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.RomReference;
import com.pixbits.stream.StreamException;

public class Verifier
{
  private final ScannerOptions options;
  private final HashCache cache;
  private final GameSet set;
  
  
  public Verifier(ScannerOptions options, GameSet set)
  {
    this.options = options;
    this.set = set;
    this.cache = set.cache();
  }
  
  public List<RomFoundReference> verify(RomHandlesSet handles) throws IOException
  {
    if (set.header != null)
      throw new UnsupportedOperationException("romsets with headers are not supported yed");
    
    return verifyBinaryFilesNoHeader(handles.binaries);
  }

  private List<RomFoundReference> verifyBinaryFilesNoHeader(List<RomHandle> handles) throws IOException
  {
    List<RomFoundReference> found = new ArrayList<>();
    
    byte[] buffer = new byte[8192];
    
    handles.stream().forEach(StreamException.rethrowConsumer(rpath -> {
      Path path = rpath.file();
      try (InputStream is = new BufferedInputStream(Files.newInputStream(path)))
      {
        InputStream fis = is;
        CheckedInputStream crc = null;
        MessageDigest md5 = null;
        MessageDigest sha1 = null;
        
        RomReference rom = null;
        
        if (options.matchCRC)
        {
          crc = new CheckedInputStream(is, new CRC32());
          fis = crc;
        }
        
        if (options.matchMD5)
        {
          md5 = MessageDigest.getInstance("MD5");
          fis = new DigestInputStream(fis, md5);
        }
        
        if (options.matchSHA1)
        {
          sha1 = MessageDigest.getInstance("SHA-1");
          fis = new DigestInputStream(fis, sha1);
        }
        
        for (; fis.read(buffer) > 0; );

        //TODO: maybe there are multple roms with same CRC32
        if (options.matchCRC)
          rom = cache.romForCrc(crc.getChecksum().getValue());
        
        if (options.matchMD5 && rom != null)
        {
          byte[] computedMD5 = md5.digest();
          
          if (!Arrays.equals(computedMD5, rom.rom.md5))
            rom = null;
        }
        
        if (options.matchSHA1 && rom != null)
        {
          byte[] computedSHA1 = sha1.digest();
          
          if (!Arrays.equals(computedSHA1, rom.rom.sha1))
            rom = null;
        }

        if (rom != null)
          found.add(new RomFoundReference(rom, rpath));
      }
    }));
    
    return found;
  }
  
  
}
