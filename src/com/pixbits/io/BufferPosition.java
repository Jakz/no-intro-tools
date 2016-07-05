package com.pixbits.io;

public class BufferPosition
{
  private final int position;
  
  BufferPosition(int position)
  {
    this.position = position;
  }
  
  public int get()
  {
    return position;
  }
  
  public String toString() { return Integer.toString(position); }
}
