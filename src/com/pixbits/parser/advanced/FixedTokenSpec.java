package com.pixbits.parser.advanced;

public class FixedTokenSpec extends TokenSpec
{
  String value;
  
  FixedTokenSpec(TokenSpec.Type type, String value)
  {
    super(type);
    this.value = value;
  }
  
  public char getStart() { return value.charAt(0); }
  
  public boolean match(String value) { return this.value.equals(value); }
  public boolean partialMatch(String value) { return this.value.startsWith(value); }
}
