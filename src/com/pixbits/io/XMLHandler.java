package com.pixbits.io;

import java.io.CharArrayWriter;

import org.xml.sax.helpers.DefaultHandler;

public abstract class XMLHandler<T> extends DefaultHandler
{
  CharArrayWriter buffer = new CharArrayWriter();

  protected String asString() { return buffer.toString().replaceAll("[\r\n]"," ").trim(); }
  
  protected int asInt()
  {
    String value = asString(); 
    return !value.isEmpty() ? Integer.parseInt(asString()) : 0;
  }
  
  protected long asLong()
  {
    String value = asString();
    return !value.isEmpty() ? Long.parseLong(asString()) : 0;
  }
  
  protected long asHexLong()
  {
    String value = asString();
    return !value.isEmpty() ? Long.parseLong(asString(), 16) : 0;
  }
  
  @Override
  public final void characters(char[] ch, int start, int length)
  {
    buffer.write(ch,start,length);
  }
  
  protected final void clear() { buffer.reset(); }
  
  abstract public T get();
}
