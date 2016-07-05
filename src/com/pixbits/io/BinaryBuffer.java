package com.pixbits.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import com.pixbits.algorithm.BoyerMooreByte;

public class BinaryBuffer implements AutoCloseable
{
  private ByteBuffer buffer;
  private final RandomAccessFile file;
  private final MapMode mapMode;
  private ByteOrder order;
  
  public BinaryBuffer(String fileName, Mode mode, ByteOrder order) throws FileNotFoundException, IOException
  {
    this(Paths.get(fileName), mode, order);
  }
  
  public BinaryBuffer(Path fileName, Mode mode, ByteOrder order) throws FileNotFoundException, IOException
  {    
    String smode = null;    
    
    switch (mode)
    {
      case READ:
      {
        smode = "r";
        mapMode = MapMode.READ_ONLY;
        break;
      }
      case WRITE:
      {
        smode = "rw"; 
        mapMode = MapMode.READ_WRITE;
        break;
      }
      default:
        mapMode = MapMode.READ_ONLY;
    }
    
    file = new RandomAccessFile(fileName.toFile(), smode);
    buffer = file.getChannel().map(mapMode, 0, file.length());
    
    this.order = order;
  }
  
  void setByteOrder(ByteOrder order)
  {
    this.order = order;
  }
  
  private void reverse(byte[] data)
  {
    for (int i = 0; i < data.length/2; ++i)
    {
      byte tmp = data[i];
      data[i] = data[data.length - i - 1];
      data[data.length - i - 1] = tmp;
    }
  }
  
  private void readOrdered(byte[] data)
  {
    buffer.get(data);
    if (order == ByteOrder.BIG_ENDIAN)
      reverse(data);
  }
  
  public void writeOrdered(byte[] data)
  {
    if (order == ByteOrder.BIG_ENDIAN)
      reverse(data);
    buffer.put(data);
  }
  
  public void writeOrdered(byte[] data, int position)
  {
    if (order == ByteOrder.BIG_ENDIAN)
      reverse(data);
    for (byte b : data)
      buffer.put(position++, b);
  }
  
  public boolean didReachEnd()
  {
    return buffer.remaining() == 0;
  }
  
  public void skip(int length)
  {
    buffer.position(buffer.position()+length);
  }
  
  public long length() throws IOException
  {
    return file.length();
  }
  
  public int position()
  {
    return buffer.position();
  }
  
  public void position(int position)
  {
    buffer.position(position);
  }
  
  public void advance(int amount)
  {
    int value = buffer.position();
    buffer.position(value+amount);
  }
  
  public void resize(long size) throws IOException
  {
    file.setLength(size);
    buffer = file.getChannel().map(mapMode, 0, file.length());
  }
  
  public void replace(byte[] bytes, int position)
  {
    buffer.position(position);
    buffer.put(bytes, 0, bytes.length);
  }
  
  public Optional<BufferPosition> replace(byte[] from, byte[] to) throws IOException
  {
    Optional<BufferPosition> position = this.scanForData(from);
    
    if (position.isPresent())
    {
      this.insert(to, position.get().get(), from.length);
    }
    
    return position;
  }
  
  public void insert(byte[] bytes, int position, int length) throws IOException
  {
    int adjust = bytes.length - length;
    
    if (adjust == 0)
      replace(bytes, position);
    /* must shift remaining to right */
    else if (adjust > 0)
    {
      resize(file.length() + adjust);
      shift(position, adjust, buffer.limit() - position - adjust);
      write(bytes, position);
    }
    /* must shift remaining to left */
    else
    {
      shift(position + length, adjust, buffer.limit() - position - length);
      resize(file.length() + adjust);
      write(bytes, position);
    }
  }
  
  public void shift(int position, int amount, int length)
  {
    byte[] tmp = new byte[length];
    
    buffer.position(position);
    buffer.get(tmp);
    buffer.position(position + amount);
    buffer.put(tmp);
  }
  
  public byte read(long position)
  {
    return buffer.get((int)position);
  }
  
  public void read(byte[] bytes)
  {
    buffer.get(bytes);
  }
  
  public void read(byte[] bytes, int position)
  {
    int backup = position();
    buffer.position(position);
    buffer.get(bytes);
    buffer.position(backup);
  }
  
  public byte readByte()
  {
    return buffer.get();
  }
  
  public int readU8()
  {
    return buffer.get() & 0xFF;
  }

  public int readU24()
  {
    byte[] data = new byte[3];
    readOrdered(data);
    return (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16);
  }
  
  public int readU32()
  {
    byte[] data = new byte[4];
    readOrdered(data);
    return (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 24);
  }
  
  public int readU32(int position)
  {
    int backup = position();
    position(position);
    int value = readU32();
    buffer.position(backup);
    return value;
  }
  
  public int readU16()
  {
    byte[] data = new byte[2];
    readOrdered(data);
    return (data[0] & 0xFF) | ((data[1] & 0xFF) << 8);
  }

  public void writeU24(int value, int position)
  {
    writeOrdered(new byte[] {(byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF)}, position);
  }
  
  public void writeU32(int value)
  {
    writeOrdered(new byte[] {(byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 24) & 0xFF)});
  }
  
  public void writeU32(int value, int position)
  {
    writeOrdered(new byte[] {(byte)(value & 0xFF), (byte)((value >> 8) & 0xFF), (byte)((value >> 16) & 0xFF), (byte)((value >> 24) & 0xFF)}, position);
  }
  
  public void writeU16(int value, int position)
  {
    writeOrdered(new byte[] {(byte)(value & 0xFF), (byte)((value >> 8) & 0xFF)}, position);
  }
    
  public String readString(int length)
  {
    byte[] bbuffer = new byte[length];
    buffer.get(bbuffer);

    return new String(bbuffer);
  }
  
  public void write(byte value, int position)
  {
    buffer.put(position, value);
  }
  
  public void write(byte[] bytes)
  {
    buffer.put(bytes);
  }
  
  public void write(byte[] bytes, int position)
  {
    buffer.position(position);
    buffer.put(bytes);
  }
  

  
  private Optional<BufferPosition> scanForDataWithCRC(byte[] data, int start)
  {
    Checksum crc = new Adler32();
    crc.update(data, 0, data.length);
    
    final long destCRC = crc.getValue();
    final byte[] tempBuffer = new byte[data.length];
    
    for (int i = start; i < buffer.limit() - data.length; ++i)
    {
      if (buffer.get(i) == data[0])
      {
        buffer.position(i);
        buffer.get(tempBuffer);
        
        crc.reset();
        crc.update(tempBuffer, 0, tempBuffer.length);
        
        if (crc.getValue() == destCRC && Arrays.equals(data, tempBuffer))
          return Optional.of(new BufferPosition(i));
            
      }
    }
    
    return Optional.empty();
  }
  
  private Optional<BufferPosition> scanForDataBayerMoore(byte[] data, int startPosition)
  {
    BoyerMooreByte boyerMore = new BoyerMooreByte(data); 
    
    int index = boyerMore.search(buffer, startPosition);
    
    return index != -1 ? Optional.of(new BufferPosition(index)) : Optional.empty();
  }
  
  public Optional<BufferPosition> scanForData(byte[] data)
  {
    return scanForDataBayerMoore(data, 0);
  }
  
  public void close()
  {
    if (buffer.isDirect())
    {
      try 
      {
        Method cleaner = buffer.getClass().getMethod("cleaner");
        cleaner.setAccessible(true);
        Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
        clean.setAccessible(true);
        clean.invoke(cleaner.invoke(buffer));
      } 
      catch(Exception ex)
      { 
        ex.printStackTrace();
      }
    }
  }
  
  public static enum Mode
  {
    READ,
    WRITE
  };
}
