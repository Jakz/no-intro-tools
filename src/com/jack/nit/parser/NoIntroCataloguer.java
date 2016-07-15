package com.jack.nit.parser;

import java.util.HashMap;
import java.util.Map;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameCataloguer;
import com.jack.nit.data.Location;

public class NoIntroCataloguer implements GameCataloguer
{
  private static final Map<String,Location> locations = new HashMap<>();
  
  static
  {
    locations.put("(World)", Location.WORLD);
    locations.put("(Japan)", Location.JAPAN);
    locations.put("(Europe)", Location.EUROPE);
    locations.put("(Italy)", Location.ITALY);
    locations.put("(France)", Location.FRANCE);
    locations.put("(Germany)", Location.GERMANY);
    locations.put("(Spain)", Location.SPAIN);
    locations.put("(Canada)", Location.CANADA);
    locations.put("(Korea)", Location.KOREA);

    
    locations.put("(Japan, USA)", Location.USA_JAPAN);
    locations.put("(USA, Europe)", Location.USA_EUROPE);
    locations.put("(USA)", Location.USA);

  }
  
  
  public void catalogue(Game game)
  {
    locations.entrySet().forEach(e -> {
      if (game.name.contains(e.getKey()))
        game.info().location.add(e.getValue());
    });
        
  }
}
