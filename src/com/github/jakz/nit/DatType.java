package com.github.jakz.nit;

import java.util.Arrays;

public enum DatType
{
  CLR_MAME_PRO("clrmamepro"),
  LOGIQX("logiqx"),
  UNSPECIFIED("unknown")
  
  ;
  
  public final String name;
  
  private DatType(String name)
  {
    this.name = name;
  }
  
  public static DatType forName(String name)
  {
    return Arrays.stream(values()).filter(f -> f.name.equals(name)).findAny().orElse(DatType.UNSPECIFIED);
  }
}
