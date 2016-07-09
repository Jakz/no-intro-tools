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
  
  public String getTitleForBias(BiasSet bias)
  {
    // find first occurring game for the zone list requested
    for (Zone zone : bias.getZones())
      if (zones[zone.ordinal()] != null)
        return zones[zone.ordinal()].name;
    
    // otherwise just return any title
    return games[0].name;
  }
  
  public Game get(Zone zone) { return zones[zone.ordinal()]; }
  public Game get(int index) { return games[index]; }
  public int size() { return games.length; }
  
  public Iterator<Game> iterator() { return Arrays.asList(games).iterator(); }
  public Stream<Game> stream() { return Arrays.stream(games); }
}
