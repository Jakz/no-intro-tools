package com.github.jakz.nit.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import com.github.jakz.nit.data.xmdb.GameClone;
import com.github.jakz.romlib.data.game.attributes.GameInfo;

public class Game implements Iterable<Rom>
{
  public final String name;
  public final String description;
  public final GameInfo info;
  
  private final Rom[] roms;
  private GameClone clone;
  
  public Game(String name, String description, Rom[] roms)
  {
    this.info = new GameInfo();
    
    this.name = name;
    this.description = description;
    this.roms = roms;
    this.clone = null;
    Arrays.stream(this.roms).forEach(r -> r.setGame(this));
  }
  
  public GameInfo info() { return info; }
    
  public boolean isComplete() { return Arrays.stream(roms).allMatch(r -> r.handle() != null); }
  public Rom get(int index) { return roms[index]; }
  public int size() { return roms.length; }
  
  public Iterator<Rom> iterator() { return Arrays.asList(roms).iterator(); }
  public Stream<Rom> stream() { return Arrays.stream(roms); }
  
  public void setClone(GameClone clone) { this.clone = clone; }
  public GameClone getClone() { return clone; }   
  
  public boolean hasEquivalentRom(Rom rom)
  {
    return Arrays.stream(roms).anyMatch(irom -> irom.isEquivalent(rom));
  }
  
  public boolean isEquivalent(Game game)
  {
    return Arrays.stream(roms).allMatch(rom -> game.hasEquivalentRom(rom));
  }
}
