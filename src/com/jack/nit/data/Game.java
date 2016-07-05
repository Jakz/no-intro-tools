package com.jack.nit.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Game
{
  public final String name;
  public final String description;
  
  private final Rom[] roms;
  
  public Game(String name, String description, Rom[] roms)
  {
    this.name = name;
    this.description = description;
    this.roms = roms;
  }
  
  public Rom get(int index) { return roms[index]; }
  public int size() { return roms.length; }
  
  public Iterator<Rom> iterator() { return Arrays.asList(roms).iterator(); }
  public Stream<Rom> stream() { return Arrays.stream(roms); }
}
