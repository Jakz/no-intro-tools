package com.pixbits.lib.parser;

public class ParseException extends Exception
{
  public ParseException(String message)
  {
    super("Parse Exception: "+message);
  }
}
