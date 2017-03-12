package com.github.jakz.nit.data.header;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Headers
{
  private interface StreamBuilder
  {
    public InputStream build(InputStream is);
  }
  
  private static final Map<String, StreamBuilder> builders = new HashMap<>();
  
  static
  {
    builders.put("nes", new StreamBuilder(){
      @Override public InputStream build(InputStream is)
      {
        return new SkippingStream(is, new byte[] { 0x4e, 0x45, 0x53 }, 16);
      }
    });
    
    builders.put("fds", new StreamBuilder(){
      @Override public InputStream build(InputStream is)
      {
        return new SkippingStream(is, new byte[] { 0x46, 0x44, 0x53 }, 16);
      }
    });
  }
  
  
}
