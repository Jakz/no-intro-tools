package com.jack.nit.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import com.jack.nit.data.header.Header;

public class GameSet
{
  public final String name;
  public final String description;
  public final Header header;
  public final String version;
  public final String comment;
  
  private final Game[] games;
  
  final private HashCache cache;

  public GameSet(String name, String description, Header header, String version, String comment, Game[] games)
  {
    this.name = name;
    this.description = description;
    this.header = header;
    this.version = version;
    this.comment = comment;
    this.games = games;
    this.cache = new HashCache(this);
  }
  
  public HashCache cache() { return cache; }

  public Game get(int index) { return games[index]; }
  public int size() { return games.length; }
  
  public int realSize() { return stream().mapToInt(Game::size).sum(); }
  
  public Stream<Game> stream() { return Arrays.stream(games); }
  public Iterator<Game> iterator() { return Arrays.asList(games).iterator(); }
}
