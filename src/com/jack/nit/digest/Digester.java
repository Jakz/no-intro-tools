package com.jack.nit.digest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import com.jack.nit.Settings;
import com.jack.nit.handles.RomHandle;

public class Digester
{
  private final DigestOptions options;
  private final byte[] buffer = new byte[Settings.DIGEST_BUFFER_SIZE];

  public Digester(DigestOptions options)
  {
    this.options = options;
  }
  
  public DigestInfo digestOnlyCRC(RomHandle handle)
  {
    return new DigestInfo(handle.crc(), null, null);
  }
  
  public DigestInfo digest(RomHandle handle, InputStream is, boolean realCRC) throws IOException, NoSuchAlgorithmException
  {
    final byte[] buffer = options.multiThreaded ? new byte[8192] : this.buffer;
    
    InputStream fis = new BufferedInputStream(is);
    CheckedInputStream crc = null;
    MessageDigest md5 = null;
    MessageDigest sha1 = null;
    boolean realRead = false;

    if (realCRC)
    {
      crc = new CheckedInputStream(is, new CRC32());
      fis = crc;
      realRead = true;
    }
    
    if (options.computeMD5)
    {
      md5 = MessageDigest.getInstance("MD5");
      fis = new DigestInputStream(fis, md5);
      realRead = true;
    }
    
    if (options.computeSHA1)
    {
      sha1 = MessageDigest.getInstance("SHA-1");
      fis = new DigestInputStream(fis, sha1);
      realRead = true;
    }

    if (realRead)
      for (; fis.read(buffer) > 0; );
    else
      is.close();
    
    if (crc != null)
      crc.close();
    
    //TODO: maybe there are multple roms with same CRC32

    return new DigestInfo(
       realCRC ? crc.getChecksum().getValue() : handle.crc(),
       options.computeMD5 ? md5.digest() : null,
       options.computeSHA1 ? sha1.digest() : null
    );
  }
}
