package com.pixbits.io;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

public class FileUtils
{
  public static long calculateCRCFast(Path filename) throws IOException
  {
    final int SIZE = 16 * 1024;
    try (FileChannel channel = (FileChannel)Files.newByteChannel(filename))
    {
      CRC32 crc = new CRC32();
      int length = (int) channel.size();
      MappedByteBuffer mb = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
      byte[] bytes = new byte[SIZE];
      int nGet;
      
      while (mb.hasRemaining())
      {
         nGet = Math.min(mb.remaining(), SIZE);
         mb.get(bytes, 0, nGet);
         crc.update(bytes, 0, nGet);
      }
      
      return crc.getValue();
    }
  }
}
