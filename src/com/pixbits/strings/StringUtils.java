package com.pixbits.strings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class StringUtils
{
  private final HexBinaryAdapter hexConverter = new HexBinaryAdapter();

  
  public static String humanReadableByteCount(long bytes)
  {
    return humanReadableByteCount(bytes, true);
  }
  
  public static String humanReadableByteCount(long bytes, boolean si)
  {
      int unit = si ? 1000 : 1024;
      if (bytes < unit) return bytes + " B";
      int exp = (int) (Math.log(bytes) / Math.log(unit));
      String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
      return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }
  
  public String toHexString(byte[] bytes)
  {
    return hexConverter.marshal(bytes);
  }
  
  public String toHexString(int value)
  {
    byte[] buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    return hexConverter.marshal(buffer);
  }
  
  public byte[] fromHexString(String value)
  {
    return hexConverter.unmarshal(value);
  }
}
