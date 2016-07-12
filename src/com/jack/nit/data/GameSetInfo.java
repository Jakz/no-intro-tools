package com.jack.nit.data;

public class GameSetInfo
{
  public final String name;
  public final String description;
  public final String version;
  public final String comment;
  public final String author;
  
  public GameSetInfo(String name, String description, String version, String comment, String author)
  {
    this.name = name;
    this.description = description;
    this.version = version;
    this.comment = comment;
    this.author = author;
  }
}
