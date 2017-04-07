package com.github.jakz.nit.data;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.GameClone;
import com.github.jakz.romlib.data.game.Location;
import com.github.jakz.romlib.data.game.Version;
import com.pixbits.lib.searcher.BasicPredicate;
import com.pixbits.lib.searcher.SearchPredicate;
import com.pixbits.lib.searcher.BasicSearchParser;
import com.pixbits.lib.searcher.LambdaPredicate;

public class Searcher
{
  com.pixbits.lib.searcher.Searcher<Game> searcher;
  
  public Searcher()
  {
    final LambdaPredicate<Game> freeSearch = new LambdaPredicate<Game>(token -> (g -> g.getTitle().toLowerCase().contains(token.toLowerCase())));
    
    final SearchPredicate<Game> isProper = new BasicPredicate<Game>()
    {
      @Override public Predicate<Game> buildPredicate(String token)
      {
        if (isSearchArg(splitWithDelimiter(token, ":"), "is", "proper"))
          return g -> g.getVersion() == Version.PROPER;
        else
          return null;
      }
    };
    
    final SearchPredicate<Game> isLicensed = new BasicPredicate<Game>()
    {
      @Override public Predicate<Game> buildPredicate(String token)
      {
        if (isSearchArg(splitWithDelimiter(token, ":"), "is", "licensed"))
          return g -> g.getLicensed();
        else
          return null;
      }
    };
    
    BasicSearchParser<Game> parser = new BasicSearchParser<>(freeSearch);
    
    searcher = new com.pixbits.lib.searcher.Searcher<>(parser, Arrays.asList(isProper, isLicensed));
  }
  
  
  public Predicate<Game> buildPredicate(String text)
  {    
    return searcher.search(text);
  }
  
  private Game findFirstClone(GameClone clone, Location... zones)
  {
    for (Location zone : zones)
    {
      Optional<Game> cgame = clone.stream().filter(g -> g.getLocation().isJust(zone)).findFirst();
      
      if (cgame.isPresent())
        return cgame.get();
      
      cgame = clone.stream().filter(g -> g.getLocation().is(zone)).findFirst();
      
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
        return lzones.stream().anyMatch(zone -> game.getLocation().isJust(zone));
      else
      {
        Game cgame = findFirstClone(clone, zones);
        return cgame == game;
      }
    };
  }
}
