package com.github.jakz.nit.data.header;

public class Header
{
  public final String name;
  public final String author;
  public final String version;
  public Header(String name, String author, String version, Rule[] rules)
  {
    this.name = name;
    this.author = author;
    this.version = version;
  }
}
