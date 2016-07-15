package com.jack.nit.data;

import java.util.Arrays;

public enum Location
{
  ITALY(0x1000L),
  FRANCE(0x2000L),
  GERMANY(0x4000L),
  SPAIN(0x8000L),
  
  JAPAN(0x1L),
  USA(0x2L),  
  CANADA(0x4L),
  KOREA(0x8L),
  
  EUROPE(ITALY, FRANCE),
  USA_JAPAN(USA, JAPAN),
  USA_EUROPE(USA, EUROPE),
  WORLD(USA, EUROPE, JAPAN)
  
  ;
  
  private Location(Location... locations) { this.mask = Arrays.stream(locations).reduce(0L, (m,l) -> m | l.mask, (u,v) -> u | v); }
  private Location(long mask) { this.mask = mask; }
  
  public static Location locationForMask(long mask)
  {
    return Arrays.stream(values()).filter(l -> l.mask == mask).findFirst().orElse(null);
  }
  
  final long mask;
};
