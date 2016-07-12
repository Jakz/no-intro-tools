package com.jack.nit.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class Game implements Iterable<Rom>
{
  public final String name;
  public final String description;
  
  private final Rom[] roms;
  
  public Game(String name, String description, Rom[] roms)
  {
    this.name = name;
    this.description = description;
    this.roms = roms;
    Arrays.stream(this.roms).forEach(r -> r.setGame(this));
  }
  
  public String normalizedTitle() { return name.substring(0, name.indexOf('(')-1); }
  
  public Rom get(int index) { return roms[index]; }
  public int size() { return roms.length; }
  
  public Iterator<Rom> iterator() { return Arrays.asList(roms).iterator(); }
  public Stream<Rom> stream() { return Arrays.stream(roms); }
  
  public boolean hasEquivalentRom(Rom rom)
  {
    return Arrays.stream(roms).anyMatch(irom -> irom.isEquivalent(rom));
  }
  
  public boolean isEquivalent(Game game)
  {
    return Arrays.stream(roms).allMatch(rom -> game.hasEquivalentRom(rom));
  }
}
