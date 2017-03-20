package com.github.jakz.nit.data.xmdb;

import com.github.jakz.romlib.data.game.Location;

public class BiasSet
{
  private final Location[] locations;
  
  public BiasSet(Location... zones)
  {
    this.locations = zones;
  }
  
  public Location[] getLocations() { return locations; }
}
