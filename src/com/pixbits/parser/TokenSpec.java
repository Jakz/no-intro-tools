package com.pixbits.parser;

public class TokenSpec
{
  public static enum Type
  {
    QUOTE,
    WHITESPACE,
    SINGLE,
    NORMAL
  };
  
  public final char value;
  public final Type type;
  
  public TokenSpec(Type type, char value)
  {
    this.type = type;
    this.value = value;
  }
  
}
