package com.jack.nit.data.xmdb;

public class BiasSet
{
  private final Zone[] zones;
  
  public BiasSet(Zone... zones)
  {
    this.zones = zones;
  }
  
  public Zone[] getZones() { return zones; }
}
