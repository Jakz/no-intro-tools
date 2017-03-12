package com.github.jakz.nit.data.header;

public class Rule
{
  public static long EOF = Long.MIN_VALUE;
  
  public static enum Type
  {
    none,
    bitswap,
    byteswap,
    wordswap,
    wordbyteswap
  };
  
  public final long startOffset;
  public final long endOffset;
  public final Type type;
  
  private final Test[] tests;

  public Rule(Type type, long startOffset, long endOffset, Test[] tests)
  {
    this.type = type;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.tests = tests;
  }
}
