package com.github.jakz.nit.data;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameSetInfo
{
  public final String name;
  public final String description;
  public final String version;
  public final String comment;
  public final String author;
  
  private int romCount;
  private int gameCount;
  private int uniqueGameCount;
  private long sizeInBytes;
  
  public GameSetInfo(String name, String description, String version, String comment, String author)
  {
    this.name = name;
    this.description = description;
    this.version = version;
    this.comment = comment;
    this.author = author;
  }
  
  void computeStats(GameSet set)
  {
    this.romCount = (int) set.stream().parallel().map(Game::stream).mapToLong(Stream::count).sum();
    this.gameCount = set.size();
    this.uniqueGameCount = set.clones() != null ? set.clones().size() : set.size();
    this.sizeInBytes = set.stream().parallel().map(Game::stream).map(s -> s.map(r -> r.size).collect(Collectors.summingLong(Long::longValue))).mapToLong(Long::longValue).sum();

  }
  
  public int romCount() { return romCount; }
  public int gameCount() { return gameCount; }
  public int uniqueGameCount() { return uniqueGameCount; }
  public long sizeInBytes() { return sizeInBytes; }
}
