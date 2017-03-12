package com.github.jakz.nit.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.jakz.nit.data.xmdb.GameClone;

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
  
  private Game findFirstClone(GameClone clone, Location... zones)
  {
    for (Location zone : zones)
    {
      Optional<Game> cgame = clone.stream().filter(g -> g.info().location.isJust(zone)).findFirst();
      
      if (cgame.isPresent())
        return cgame.get();
      
      cgame = clone.stream().filter(g -> g.info().location.is(zone)).findFirst();
      
      if (cgame.isPresent())
        return cgame.get();
    }
    
    return null;
  }
  
  public Predicate<Game> buildExportByRegionPredicate(Location... zones)
  {
    return game -> {
      GameClone clone = game.getClone();
      List<Location> lzones = Arrays.asList(zones);
 
      /* if game doesn't have any clone then should be exported if it has correct zone */
      if (clone == null)
        return lzones.stream().anyMatch(zone -> game.info().location.isJust(zone));
      else
      {
        Game cgame = findFirstClone(clone, zones);
        return cgame == game;
      }
    };
  }
}
