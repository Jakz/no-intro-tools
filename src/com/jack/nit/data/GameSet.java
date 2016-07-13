package com.jack.nit.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.jack.nit.data.header.Header;
import com.jack.nit.data.xmdb.CloneSet;

public class GameSet implements Iterable<Game>
{
  public final GameSetInfo info;
  public final Header header;

  private final Game[] games;
  private final Map<String, Game> gameMap;
  
  final private HashCache cache;
  
  private CloneSet clones;
  
  private System system;

  public GameSet(GameSetInfo info, Header header, Game[] games)
  {
    this.info = info;
    this.header = header;
    this.games = games;
    this.cache = new HashCache(this);
    this.gameMap = new HashMap<>();
    
    Arrays.stream(games).forEach(g -> gameMap.put(g.name, g));
  }
  
  public CloneSet clones() { return clones; }
  public void setClones(CloneSet clones) { this.clones = clones; }
  
  public HashCache cache() { return cache; }

  public Game get(String name) { return gameMap.get(name); }
  public Game get(int index) { return games[index]; }
  public int size() { return games.length; }
  
  public int realSize() { return stream().mapToInt(Game::size).sum(); }
  
  
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
  
  public System system() { return system; }
}
