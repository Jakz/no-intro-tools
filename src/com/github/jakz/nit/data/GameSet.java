package com.github.jakz.nit.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.github.jakz.nit.config.GameSetConfig;
import com.github.jakz.nit.data.header.Header;
import com.github.jakz.nit.data.xmdb.CloneSet;
import com.github.jakz.nit.data.xmdb.GameClone;

public class GameSet implements Iterable<Game>
{
  public final GameSetInfo info;
  public final Header header;

  private final Game[] games;
  private final Map<String, Game> gameMap;
  
  final private HashCache cache;
  
  private CloneSet clones;
  
  private final GameSetConfig config;
  private System system;

  public GameSet(GameSetInfo info, Header header, Game[] games)
  {
    this.info = info;
    this.header = header;
    this.games = games;
    this.cache = new HashCache(this);
    this.gameMap = new HashMap<>();
    this.config = new GameSetConfig();
    
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
  
  public HashCache cache() { return cache; }

  public Game get(String name) { return gameMap.get(name); }
  public Game get(int index) { return games[index]; }
  public int size() { return games.length; }
  
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
  
 public void setSystem(System system) { this.system = system; }
  public System system() { return system; }
  
  public GameSetConfig getConfig() { return config; }
}