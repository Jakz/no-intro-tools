package com.jack.nit.data.xmdb;

import java.util.HashMap;
import java.util.Map;

public enum Zone
{
  JAPAN("J"),
  USA("U"),
  EUROPE("E"),
  SPAIN("S"),
  GERMANY("G"),
  FRANCE("F"),
  ITALY("I")
  
  ;
  
  private static final Map<String, Zone> tinyCodeMap = new HashMap<>();
  
  public final String tinyCode;
  
  private Zone(String tinyCode)
  {
    this.tinyCode = tinyCode;
  }
  
  static
  {
    for (Zone zone : Zone.values())
      tinyCodeMap.put(zone.tinyCode, zone);
  }
}
