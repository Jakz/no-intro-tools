package com.github.jakz.nit.data.xmdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.romlib.data.game.GameClone;

public class CloneSet implements Iterable<GameClone<Game>>
{
  private final GameClone<Game>[] clones;
  
  private final Map<Game, GameClone<Game>> cloneMap;
  
  public CloneSet(GameSet set, GameClone<Game>[] clones)
  {
    this.clones = clones;
    this.cloneMap = new HashMap<>(clones.length);
    
    Arrays.stream(clones).forEach(gc -> {
      gc.stream().forEach(g -> cloneMap.put(g, gc));
    });  
  }
  
  public GameClone<Game> get(Game game) { return cloneMap.get(game); }
  public GameClone<Game> get(int index) { return clones[index]; }
  public int size() { return clones.length; }
  
  public Iterator<GameClone<Game>> iterator() { return Arrays.asList(clones).iterator(); }
  public Stream<GameClone<Game>> stream() { return Arrays.stream(clones); }
}
