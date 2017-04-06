package com.github.jakz.nit.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.github.jakz.nit.data.header.Header;
import com.github.jakz.nit.data.xmdb.CloneSet;
import com.github.jakz.nit.data.xmdb.GameClone;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.platforms.Platform;
import com.github.jakz.romlib.data.set.GameSetInfo;
import com.pixbits.lib.io.digest.HashCache;

public class GameSet implements Iterable<Game>
{
  public final GameSetInfo info;

  private final Game[] games;
  private final Map<String, Game> gameMap;
  
  final private HashCache<Rom> cache;
  
  private CloneSet clones;
  
  public GameSet(GameSetInfo info, Game[] games)
  {
    this.info = info;
    this.games = games;
    this.cache = new HashCache<Rom>(Arrays.stream(games).flatMap(g -> g.stream()));
    this.gameMap = new HashMap<>();
    
    Arrays.stream(games).forEach(g -> gameMap.put(g.name, g));
    
    info.computeStats(this);
  }
  
  public CloneSet clones() { return clones; }
  public void setClones(CloneSet clones) {  
    this.clones = clones;
    
    for (GameClone clone : clones)
      for (Game game : clone)
        game.setClone(clone);
    
    info.computeStats(this);
  }
  
  public HashCache<Rom> cache() { return cache; }

  public Game get(String name) { return gameMap.get(name); }
  public Game get(int index) { return games[index]; }
  
  public int gameCount() { return games.length; }
  public int filesCount() { return stream().mapToInt(Game::size).sum(); }
  
  
  public Stream<Game> stream() { return Arrays.stream(games); }
  public Iterator<Game> iterator() { return Arrays.asList(games).iterator(); }
  
  public Stream<Rom> foundRoms()
  { 
    return Arrays.stream(games)
        .map(game -> game.stream()
            .filter(r -> r.handle() != null))
        .reduce((s1,s2) -> Stream.concat(s1, s2))
        .get(); 
  }
}
