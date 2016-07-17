package com.jack.nit.data;

public interface Version 
{    
  public final static Version PROPER = new Version() {
    @Override public boolean equals(Object obj) { return obj == this; }
  };
  
  public final static Version SAMPLE = new Version() {
    @Override public boolean equals(Object obj) { return obj == this; }
  };
  
  public final static Version DEMO = new Version() {
    @Override public boolean equals(Object obj) { return obj == this; }
  };
  
  public final static Version BETA = new Version() {
    @Override public boolean equals(Object obj) { return obj == this; }
  };
  
  public final static Version PROTO = new Version() {
    @Override public boolean equals(Object obj) { return obj == this; }
  };
  
  public final class Revision implements Version
  {
    private final String type;
    public Revision(String type)
    { 
      this.type = type;
    }
    
    @Override public boolean equals(Object obj)
    { 
      return (obj instanceof Revision) && ((Revision)obj).type.equals(type);
    }
  }
}
