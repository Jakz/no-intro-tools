package com.jack.nit.archives;

import java.nio.file.Path;

public class Archive
{
  private final Path path;
  
  Archive(Path path)
  {
    this.path = path;
  }
  
  boolean isCached() { return false; }
}
