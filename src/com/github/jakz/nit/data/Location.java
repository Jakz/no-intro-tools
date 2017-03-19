package com.github.jakz.nit.data;

import java.util.Arrays;

import com.github.jakz.romlib.ui.Icon;

public enum Location
{
  ITALY(0x1000L, "Italy", "Italian"),
  FRANCE(0x2000L, "France", "French"),
  GERMANY(0x4000L, "Germany", "German"),
  SPAIN(0x8000L, "Spain", "Spanish"),
  SWEDEN(0x10000L, "Sweden", "Swedish"),
  CHINA(0x20000L, "China", "Chinese"),
  
  JAPAN(0x1L, "Japan", "Japanese"),
  USA(0x2L, "USA", "English"),  
  CANADA(0x4L, "Canada", "Canadian"),
  KOREA(0x8L, "Korea", "Korean"),
  
  EUROPE(ITALY, FRANCE, GERMANY, SPAIN, SWEDEN),
  USA_JAPAN(USA, JAPAN),
  USA_EUROPE(USA, EUROPE),
  WORLD(USA, EUROPE, JAPAN)
  
  ;
  
  private Location(Location... locations)
  { 
    this.mask = Arrays.stream(locations).reduce(0L, (m,l) -> m | l.mask, (u,v) -> u | v); 
    this.language = null;
    this.name = null;
  }
  
  private Location(long mask, String name, String language)
  { 
    this.mask = mask;
    this.language = language;
    this.name = name;
  }
  
  public boolean isComposite() { return name == null; }
  
  public static Location locationForMask(long mask)
  {
    return Arrays.stream(values()).filter(l -> l.mask == mask).findFirst().orElse(null);
  }
  
  public final String name;
  public final String language;
  
  final long mask;
  
  
  public static Icon iconForLocation(Location location) { return iconForLocation(new LocationSet(location)); }
  public static Icon iconForLocation(LocationSet location)
  {
    if (location.isJust(Location.ITALY))
      return Icon.FLAG_ITALY;
    else if (location.isJust(Location.FRANCE))
      return Icon.FLAG_FRANCE;
    else if (location.isJust(Location.GERMANY))
      return Icon.FLAG_GERMANY;
    else if (location.isJust(Location.SPAIN))
      return Icon.FLAG_SPAIN;
    else if (location.isJust(Location.GERMANY))
      return Icon.FLAG_GERMANY;
    else if (location.isJust(Location.SWEDEN))
      return Icon.FLAG_SWEDEN;
    
    
    else if (location.isJust(Location.EUROPE))
      return Icon.FLAG_EUROPE;

    else if (location.isJust(Location.USA))
      return Icon.FLAG_USA;
    
    else if (location.isJust(Location.KOREA))
      return Icon.FLAG_KOREA;
    else if (location.isJust(Location.CANADA))
      return Icon.FLAG_CANADA;
    
    else if (location.isJust(Location.JAPAN))
      return Icon.FLAG_JAPAN;
    else if (location.isJust(Location.CHINA))
      return Icon.FLAG_CHINA;
    
    else if (location.isJust(Location.USA_EUROPE))
      return Icon.FLAG_USA_EUROPE;
    
    else if (location.isJust(Location.USA_JAPAN))
      return Icon.FLAG_JAPAN_USA;
    
    else if (location.isJust(Location.WORLD))
      return Icon.FLAG_WORLD;
    
    else return null;
  }
};
