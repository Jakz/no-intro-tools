package com.github.jakz.nit.data;

public class GameInfo
{
  public final LocationSet location;
  public final LocationSet languages;
  public Version version;
  public boolean licensed;
  
  
  GameInfo()
  {
    location = new LocationSet();
    languages = new LocationSet();
    version = Version.PROPER;
    licensed = true;
  }
}
