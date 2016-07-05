package com.pixbits.parser;

public class ParseException extends Exception
{
  public ParseException(String message)
  {
    super("Parse Exception: "+message);
  }
}
