package com.github.jakz.nit.data;

import java.util.Arrays;

public class LocationSet
{
  private long mask;
  
  public LocationSet(Location... locations)
  {
    mask = Arrays.stream(locations).reduce(0L, (m,l) -> m | l.mask, (u,v) -> u | v);
  }
  
  public LocationSet(long mask)
  {
    this.mask = mask;
  }
  
  public void add(Location location) { mask |= location.mask; }
  public boolean is(Location location) { return (mask & location.mask) != 0; }
  public boolean isJust(Location location) { return mask == location.mask; }
  public boolean isLocalized() { return mask != 0; }
  
  public long getMask() { return mask; }
}
