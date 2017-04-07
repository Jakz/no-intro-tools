package com.github.jakz.nit.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.Language;
import com.github.jakz.romlib.data.game.Location;
import com.github.jakz.romlib.data.game.Version;
import com.github.jakz.romlib.parsers.GameCataloguer;

public class NoIntroCataloguer implements GameCataloguer
{
  private static final Map<String,Location> locations = new HashMap<>();
  private static final Map<String,Language> languages = new HashMap<>();

  
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

    
    languages.put("It", Language.ITALIAN);
    languages.put("Fr", Language.FRENCH);
    languages.put("De", Language.GERMAN);
    languages.put("Es", Language.SPANISH);
  }
  
  
  public void catalogue(Game game)
  {
    String name = game.getTitle();
    
    locations.entrySet().forEach(e -> {
      if (name.contains(e.getKey()))
        game.getLocation().add(e.getValue());
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
        game.getLanguages().add(e.getValue());
    });
    
    if (name.contains("(Rev A)"))
      game.setVersion(new Version.Revision("Rev A"));
    else if (name.contains("(Rev B)"))
      game.setVersion(new Version.Revision("Rev B"));
    else if (name.contains("(Rev 1)"))
      game.setVersion(new Version.Revision("Rev 1"));
    else if (name.contains("(Rev 2)"))
      game.setVersion(new Version.Revision("Rev 2"));
    else if (name.contains("(Demo)"))
      game.setVersion(Version.DEMO);
    else if (name.contains("(Sample)"))
      game.setVersion(Version.SAMPLE);
    else if (name.contains("(Beta)"))
      game.setVersion(Version.BETA);
    else if (name.contains("(Proto)"))
      game.setVersion(Version.PROTO);

    if (name.contains("(Unl)"))
      game.setLicensed(false);
       
        
  }
}
