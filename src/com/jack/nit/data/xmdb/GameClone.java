package com.jack.nit.data.xmdb;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import com.jack.nit.data.Game;

public class GameClone
{
  private final Game[] games;
  private final Game[] zones;
  
  public GameClone(Game[] games, Game[] zones)
  {
    this.games = games;
    this.zones = zones;
  }
  
  public Game get(Zone zone) { return zones[zone.ordinal()]; }
  public Game get(int index) { return games[index]; }
  public int size() { return games.length; }
  
  public Iterator<Game> iterator() { return Arrays.asList(games).iterator(); }
  public Stream<Game> stream() { return Arrays.stream(games); }
}
