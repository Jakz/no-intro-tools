package com.github.jakz.nit.data.header;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class SkippingStream extends InputStream
{
  
  private final byte[] values;
  private final int bytesToSkip;
  
  private int offset;
  private final byte[] buffer;
  
  private InputStream is;
  
  private Status status;
  private int read;
  
  private enum Status
  {
    JUST_OPENED,
    READY,
    INVALID,
  };
  
  public SkippingStream(InputStream is, byte[] values, int bytesToSkip)
  {
    offset = 0;
    this.values = values;
    this.bytesToSkip = bytesToSkip;
    this.is = is;
    
    this.buffer = new byte[values.length];
    
    this.status = Status.JUST_OPENED;
    this.read = 0;
  }
  
  public int read(byte[] buffer) throws IOException
  {
    return super.read(buffer);
  }
  
  public int read(byte[] buffer, int o, int l) throws IOException
  {
    return super.read(buffer, o, l);
  }
  
  public int read() throws IOException
  {
    switch (this.status)
    {
      case READY:
        return is.read();
        
      case JUST_OPENED:
      {
        /* read enough bytes to be able to check magic number */
        while ((read += is.read(buffer, read, values.length-read)) < values.length);
        
        /* magic number is correct, then we apply skip */
        boolean valid = Arrays.equals(values, buffer); 
        
        /* if check is valid then we should 
         * skip all the remaining bytes from header 
         * and return the next one
         */
        if (valid)
        {
          while (read < bytesToSkip)
          {
            is.read();
            ++read;
          }
          
          status = Status.READY;
          return is.read();
        }
        else
        {
          /* otherwise we should return the 
           * buffer and then keep with the stream 
           */
          status = Status.INVALID;
          byte value = buffer[values.length - read];
          --read;
          return value;
        }
      }
      case INVALID:
      {
        byte value = buffer[values.length - read];
        --read;
        if (read == 0)
          status = Status.READY;
        return value;
      }
      default: return -1;
    }
    
    
  }
}
