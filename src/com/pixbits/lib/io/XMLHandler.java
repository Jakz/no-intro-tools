package com.pixbits.lib.io;

import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class XMLHandler<T> extends DefaultHandler
{
  private final CharArrayWriter buffer = new CharArrayWriter();
  private final Stack<Map<String,Object>> stack = new Stack<>();
  
  private final HexBinaryAdapter hexConverter = new HexBinaryAdapter();
  
  private Attributes currentAttributes = null;

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
  
  @Override public final void characters(char[] ch, int start, int length)
  {
    buffer.write(ch,start,length);
  }
  
  @Override public final void endElement(String namespaceURI, String name, String qName) throws SAXException
  {
    end(name);
    stack.pop();
  }
  
  @Override public final void startElement(String namespaceURI, String name, String qName, Attributes attr) throws SAXException
  {
    currentAttributes = attr;
    clearBuffer();
    stack.push(new HashMap<>());
    start(name, attr);
  }
  
  protected String attrString(String key) { return currentAttributes.getValue(key); }
  
  protected long longAttributeOrDefault(String key, long value)
  {
    String o = currentAttributes.getValue(key);
    
    if (o == null)
      return value;
    else if (o.equals("EOF"))
      return Long.MIN_VALUE;
    else
      return Long.valueOf(o);
  }
  
  protected byte[] hexByteArray(String key)
  {
    return hexConverter.unmarshal(currentAttributes.getValue(key));
  }
  
  protected boolean boolOrDefault(String key, boolean value)
  {
    String o = currentAttributes.getValue(key);
    return o != null ? Boolean.valueOf(key) : value; 
  }
  
  protected long longHexAttributeOrDefault(String key, long value)
  {
    String o = currentAttributes.getValue(key);
    
    if (o == null)
      return value;
    else if (o.equals("EOF"))
      return Long.MIN_VALUE;
    else
      return Long.valueOf(o, 16);
  }
  
  protected String stringAttributeOrDefault(String key, String value)
  {
    String o = currentAttributes.getValue(key);
    return o != null ? o : value;
  }
  
  @SuppressWarnings("unchecked")
  protected <U> U value(String key) { return (U)stack.peek().get(key); }
  @SuppressWarnings("unchecked")
  protected <U> U valueOrDefault(String key, U value) { return (U)stack.peek().getOrDefault(key, value); }

  protected void map(String key, Object o) { stack.peek().put(key, o); }
  protected void mapOuter(String key, Object o) { stack.get(stack.size()-2).put(key, o); }
  
  protected final void clearBuffer() { buffer.reset(); }
  
  protected abstract void start(String name, Attributes attr);
  protected abstract void end(String name);
  
  abstract public T get();
}
