package com.github.jakz.nit.exceptions;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class RomPathNotFoundException extends FileNotFoundException
{
  private static final long serialVersionUID = 1L;
  public final Path path;
  
  public RomPathNotFoundException(Path path)
  {
    super("unable to find rom path: "+path);
    this.path = path;
  }
}
