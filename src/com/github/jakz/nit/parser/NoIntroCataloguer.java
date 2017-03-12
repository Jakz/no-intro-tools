package com.github.jakz.nit.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameCataloguer;
import com.github.jakz.nit.data.Location;
import com.github.jakz.nit.data.Version;

public class NoIntroCataloguer implements GameCataloguer
{
  private static final Map<String,Location> locations = new HashMap<>();
  private static final Map<String,Location> languages = new HashMap<>();

  
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
    locations.put("(Sweden)", Location.SWEDEN);
    locations.put("(China)", Location.CHINA);

    
    locations.put("(Japan, USA)", Location.USA_JAPAN);
    locations.put("(USA, Europe)", Location.USA_EUROPE);
    locations.put("(USA)", Location.USA);

    
    languages.put("It", Location.ITALY);
    languages.put("Fr", Location.FRANCE);
    languages.put("De", Location.GERMANY);
    languages.put("Es", Location.SPAIN);
  }
  
  
  public void catalogue(Game game)
  {
    String name = game.name;
    
    locations.entrySet().forEach(e -> {
      if (name.contains(e.getKey()))
        game.info().location.add(e.getValue());
    });
      
    languages.entrySet().forEach(e -> {
      String key = e.getKey();
      String[] patterns = {
          "("+key+")",
          "("+key+",",
          ","+key+")",
          ","+key+","
      };
      
      if (Arrays.stream(patterns).anyMatch(s -> name.contains(s)))
        game.info().languages.add(e.getValue());
    });
    
    if (name.contains("(Rev A)"))
      game.info().version = new Version.Revision("Rev A");
    else if (name.contains("(Rev B)"))
      game.info().version = new Version.Revision("Rev B");
    else if (name.contains("(Rev 1)"))
      game.info().version = new Version.Revision("Rev 1");
    else if (name.contains("(Rev 2)"))
      game.info().version = new Version.Revision("Rev 2");
    else if (name.contains("(Demo)"))
      game.info().version = Version.DEMO;
    else if (name.contains("(Sample)"))
      game.info().version = Version.SAMPLE;
    else if (name.contains("(Beta)"))
      game.info().version = Version.BETA;
    else if (name.contains("(Proto)"))
      game.info().version = Version.PROTO;

    if (name.contains("(Unl)"))
      game.info().licensed = false;
       
        
  }
}
