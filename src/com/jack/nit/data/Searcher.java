package com.jack.nit.data;

import java.util.Arrays;
import java.util.function.Predicate;

import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.data.xmdb.Zone;

public class Searcher
{
  public Predicate<Game> buildPredicate(String text)
  {    
    Predicate<Game> filter = g -> true;
    
    String[] tokens = text.split(" ");
    for (String tokenz : tokens)
    {
      if (tokenz.length() == 0)
        continue;
      
      Predicate<Game> predicate = null;
      String token = tokenz.charAt(0) == '!' ? tokenz.substring(1) : tokenz;
      boolean negated = tokenz.charAt(0) == '!';
      
      if (token.equals("is:proper"))
        predicate = (g -> g.info().version == Version.PROPER);
      else if (token.equals("is:licensed"))
        predicate = (g -> g.info().licensed);
      else
        predicate = (g -> g.name.toLowerCase().contains(token.toLowerCase()));
      
      if (predicate != null)
        filter = filter.and(negated ? predicate.negate() : predicate);
    }
        
    return filter;
  }
  
  public Predicate<Game> buildExportByRegionPredicate(Location... zones)
  {
    return game -> {
      GameClone clone = game.getClone();
      
      if (clone == null)
        return Arrays.stream(zones).anyMatch(zone -> game.info().location.isJust(zone));
      else
      {
        for (Location location : location)
          
      }
    };
  }
}
