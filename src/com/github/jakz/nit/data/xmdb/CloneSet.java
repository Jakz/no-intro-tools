package com.github.jakz.nit.data.xmdb;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameSet;

public class CloneSet implements Iterable<GameClone>
{
  private final GameSet set;
  private final GameClone[] clones;
  
  private final Map<Game, GameClone> cloneMap;
  
  public CloneSet(GameSet set, GameClone[] clones)
  {
    this.clones = clones;
    this.set = set;
    
    cloneMap = new HashMap<>(this.set.size());
    
    Arrays.stream(clones).forEach(gc -> {
      gc.stream().forEach(g -> cloneMap.put(g, gc));
    });  
  }
  
  public GameClone get(Game game) { return cloneMap.get(game); }
  public GameClone get(int index) { return clones[index]; }
  public int size() { return clones.length; }
  
  public Iterator<GameClone> iterator() { return Arrays.asList(clones).iterator(); }
  public Stream<GameClone> stream() { return Arrays.stream(clones); }
}
