package com.github.jakz.nit.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.jakz.nit.data.Game;
import com.github.jakz.romlib.data.game.Language;
import com.github.jakz.romlib.data.game.Location;
import com.github.jakz.romlib.data.game.Version;

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
    String name = game.name;
    
    locations.entrySet().forEach(e -> {
      if (name.contains(e.getKey()))
        game.info().getLocation().add(e.getValue());
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
        game.info().getLanguages().add(e.getValue());
    });
    
    if (name.contains("(Rev A)"))
      game.info().setVersion(new Version.Revision("Rev A"));
    else if (name.contains("(Rev B)"))
      game.info().setVersion(new Version.Revision("Rev B"));
    else if (name.contains("(Rev 1)"))
      game.info().setVersion(new Version.Revision("Rev 1"));
    else if (name.contains("(Rev 2)"))
      game.info().setVersion(new Version.Revision("Rev 2"));
    else if (name.contains("(Demo)"))
      game.info().setVersion(Version.DEMO);
    else if (name.contains("(Sample)"))
      game.info().setVersion(Version.SAMPLE);
    else if (name.contains("(Beta)"))
      game.info().setVersion(Version.BETA);
    else if (name.contains("(Proto)"))
      game.info().setVersion(Version.PROTO);

    if (name.contains("(Unl)"))
      game.info().setLicensed(false);
       
        
  }
}
