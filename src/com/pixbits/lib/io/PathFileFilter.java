package com.pixbits.lib.io;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.PathMatcher;

public class PathFileFilter implements FileFilter
{
  private final PathMatcher matcher;

  public PathFileFilter(PathMatcher matcher)
  {
    this.matcher = matcher;
  }
  
  @Override
  public boolean accept(File pathname)
  {
    return matcher.matches(pathname.toPath());
  }

}
