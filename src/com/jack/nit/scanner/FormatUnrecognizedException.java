package com.jack.nit.scanner;

import java.nio.file.Path;

public class FormatUnrecognizedException extends Exception
{
  public final Path path;
  
  FormatUnrecognizedException(Path path, String message)
  {
    super(message);
    this.path = path;
  }
}
