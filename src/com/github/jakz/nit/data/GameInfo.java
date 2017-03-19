package com.github.jakz.nit.data;

import com.github.jakz.romlib.data.game.LocationSet;

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
