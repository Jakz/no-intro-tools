package com.pixbits.algorithm;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BoyerMooreByte
{
  private final int R;
  private int[] right;
  
  private byte[] pattern;
  
  public BoyerMooreByte(byte[] pattern)
  {
    this.R = 256;
    this.pattern = pattern;
    
    right = new int[R];
    
    Arrays.fill(right, -1);
    for (int i = 0; i < pattern.length; ++i)
      right[pattern[i] & 0xFF] = i;
  }
  
  public int search(ByteBuffer buffer, int startPosition)
  {
    int M = pattern.length;
    int N = buffer.limit();
    int skip = 0;
    
    for (int i = startPosition; i <= N - M; i += skip)
    {
      skip = 0;
      
      for (int j = M - 1; j >= 0; j--)
      {
        if (pattern[j] != buffer.get(i+j))
        {
          skip = Math.max(1, j - right[buffer.get(i+j) & 0xFF]);
          break;
        }
      }
      
      if (skip == 0)
        return i;
    }
    
    return -1;  
  }
}
