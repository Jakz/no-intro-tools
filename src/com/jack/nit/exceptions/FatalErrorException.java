package com.jack.nit.exceptions;

public class FatalErrorException extends RuntimeException
{
  public FatalErrorException(String message)
  {
    super(message);
  }
}
