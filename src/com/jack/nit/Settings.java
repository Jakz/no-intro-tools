package com.jack.nit;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.jack.nit.data.GameSet;

public class Settings
{
  public static Path DATS_PATH = Paths.get("dats/");
  public static Path HEADERS_PATH = DATS_PATH.resolve("headers/");

  public static Path[] resolveRomPathsForSet(GameSet set)
  {
    return new Path[] { Paths.get("/Volumes/RAMDisk") };
  }
}
