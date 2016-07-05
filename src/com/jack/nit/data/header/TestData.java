package com.jack.nit.data.header;

import java.util.Arrays;
import java.util.function.Predicate;

import com.pixbits.io.BinaryBuffer;

public class TestData extends Test
{
  private final int offset;
  private final byte[] data;
  private boolean isInverted;
  
  TestData(int offset, byte[] data, boolean isInverted)
  {
    this.offset = offset;
    this.data = data;
    this.isInverted = isInverted;
  }
  
  @Override Predicate<BinaryBuffer> build()
  {
    return b -> {
      byte[] test = new byte[data.length];
      b.read(test, offset);      
      return Arrays.equals(data, test) ^ !isInverted;      
    };
  }
}
