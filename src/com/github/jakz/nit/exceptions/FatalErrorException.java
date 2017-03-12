package com.github.jakz.nit.exceptions;

public class FatalErrorException extends RuntimeException
{
  public FatalErrorException(Exception e)
  {
    super(e);
  }
  
  public FatalErrorException(String message)
  {
    super(message);
  }
}
