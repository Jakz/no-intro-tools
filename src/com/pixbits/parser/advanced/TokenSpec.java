package com.pixbits.parser.advanced;

public abstract class TokenSpec
{
  public static enum Type
  {
    QUOTE,
    WHITESPACE,
    FIXED,
  };

  public final Type type;

  protected TokenSpec(Type type)
  {
    this.type = type;
  }
  
  public abstract boolean match(String value);
  public abstract boolean partialMatch(String value);
  
  public String transform(String token)
  {
    if (type == Type.WHITESPACE)
      return null;
    else return token;
  }
}
