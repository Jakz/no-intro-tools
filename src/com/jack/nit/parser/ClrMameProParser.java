package com.jack.nit.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.xml.sax.SAXException;

import com.jack.nit.Options;
import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.GameSetInfo;
import com.jack.nit.data.Rom;
import com.jack.nit.data.header.Header;
import com.pixbits.io.XMLParser;
import com.pixbits.parser.SimpleParser;
import com.pixbits.parser.SimpleTreeBuilder;

public class ClrMameProParser
{
  private final Options options;
  
  public ClrMameProParser(Options options)
  {
    this.options = options;
  }
  
  private enum Status
  {
    NOWHERE,
    HEADER,
    GAME,
    ROM
  };
  
  GameSet set;
  Game game;
  Rom rom;
  
  List<Rom> roms;
  List<Game> games;
  
  String name;
  String description;
  String header;
  String version;
  String comment;
  
  long crc32;
  long size;
  byte[] md5;
  byte[] sha1;
  
  Stack<Map<String,Object>> values;
   
  Status status = Status.NOWHERE;
  
  SimpleParser parser;
  Path path;

  private final HexBinaryAdapter hexConverter = new HexBinaryAdapter();
  
  public void pushState()
  {
    values.push(new HashMap<>());
  }
  
  public void popState()
  {
    values.pop();
  }
  
  public Map<String,Object> valueMap() { return values.peek(); }
  
  public void checkPresenceOfValues(String... keys)
  {
    Optional<String> missingKey = Arrays.stream(keys).filter(key -> !valueMap().containsKey(key)).findAny();
    
    if (missingKey.isPresent())
      throw new IllegalArgumentException(String.format("required value %s in clrmamepro dat was not found at (%s:%d,%d).", missingKey.get(), path.getFileName().toString(), parser.getLine(), parser.getRow()));
  }
  
  @SuppressWarnings("unchecked")
  public <T> T value(String k) { return (T)valueMap().get(k); }
  public <T> T valueOrDefault(String k, T def) { T value = value(k); return value != null ? value : def; }
  
  public void setValueForKey(String k, Object v) { valueMap().put(k, v); }
  
  public GameSet load(Path file) throws IOException, SAXException
  {
    this.path = file;
    try (InputStream fis = new BufferedInputStream(Files.newInputStream(file)))
    {
      status = Status.NOWHERE;
      set = null;
      game = null;
      rom = null;
      values = new Stack<>();
      
      parser = new SimpleParser(fis);
      parser.addSingle('(', ')').addQuote('\"').addWhiteSpace(' ', '\t', '\r', '\n');

      SimpleTreeBuilder builder = new SimpleTreeBuilder(parser, this::pair, this::scope);
      builder.setScope("(", ")");
      
      parser.parse();
      
      String headerFile = value("header");
      Header header = null;
      
      if (headerFile != null)
      {        
        XMLParser<Header> headerParser = new XMLParser<>(new HeaderParser());
        
        Path headerPath = options.headerPath != null && Files.isDirectory(options.headerPath) ? options.headerPath.resolve(headerFile) : file.getParent().resolve(headerFile);
        
        header = headerParser.load(headerPath);
      }
      
      set = new GameSet(
          new GameSetInfo(value("name"), value("description"), value("version"), value("comment"), value("author")),
          header,
          games.toArray(new Game[games.size()])
      );
      popState();
      
      return set;
    }
  }

  static String[] stringKeys = new String[] { "name", "description", "header", "version", "comment" };
  
  public void pair(String k, String v)
  {
    for (String key : stringKeys)
      if (key.equals(k))
      {
        setValueForKey(k, v);
        return;
      }
    
    if (k.equals("size"))
      setValueForKey(k, Long.parseLong(v));
    else if (k.equals("crc"))
      setValueForKey(k, Long.parseLong(v, 16));
    else if (k.equals("sha1") || k.equals("md5"))
      setValueForKey(k, hexConverter.unmarshal(v));
    else if (k.equals("flags") || k.equals("serial"))
      return; // skip
    else if (k.equals("forcenodump"))
      return;
    else
      throw new IllegalArgumentException("unrecognized key in dat: "+k+", "+v);
  }
  
  public void scope(String k, boolean isEnd)
  {   
    if (k.equals("clrmamepro"))
    {
      if (!isEnd)
      {
        pushState();
        games = new ArrayList<>();
      }
    }
    else if (k.equals("game"))
    {
      if (!isEnd)
      {
        pushState();
        roms = new ArrayList<>();
      }
      else
      {
        games.add(new Game(value("name"), value("description"), roms.toArray(new Rom[roms.size()])));
        popState();
      }
    }
    else if (k.equals("rom"))
    {
      if (!isEnd)
      {
        pushState();
      }
      else
      {
        checkPresenceOfValues("name");
        
        roms.add(new Rom(value("name"), valueOrDefault("size", -1L), valueOrDefault("crc", -1L), valueOrDefault("md5", null), valueOrDefault("sha1", null)));
        popState();
      }
    }
  }
}