package com.github.jakz.nit.data.header;

import java.util.Arrays;
import java.util.function.Predicate;

import com.pixbits.lib.io.BinaryBuffer;

public class TestData extends Test
{
  private final long offset;
  private final byte[] data;
  private boolean isPositive;
  
  public TestData(long offset, byte[] data, boolean isPositive)
  {
    this.offset = offset;
    this.data = data;
    this.isPositive = isPositive;
  }
  
  @Override Predicate<BinaryBuffer> build()
  {
    return b -> {
      byte[] test = new byte[data.length];
      b.read(test, (int)offset);      
      return Arrays.equals(data, test) ^ isPositive;      
    };
  }
}
