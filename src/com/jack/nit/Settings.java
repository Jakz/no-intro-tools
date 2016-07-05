package com.jack.nit;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings
{
  public static Path DATS_PATH = Paths.get("dats/");
  public static Path HEADERS_PATH = DATS_PATH.resolve("headers/");

}
