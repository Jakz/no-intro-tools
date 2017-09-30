package com.github.jakz.nit.parser;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.xml.sax.SAXException;

import com.github.jakz.nit.Options;
import com.github.jakz.nit.data.header.Header;
import com.github.jakz.nit.merger.TitleNormalizer;
import com.github.jakz.romlib.data.cataloguers.GameCataloguer;
import com.github.jakz.romlib.data.cataloguers.NoIntroCataloguer;
import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.game.RomSize;
import com.github.jakz.romlib.data.set.DatFormat;
import com.github.jakz.romlib.data.set.DataSupplier;
import com.github.jakz.romlib.data.set.GameList;
import com.github.jakz.romlib.data.set.GameSet;
import com.github.jakz.romlib.data.set.GameSetInfo;
import com.github.jakz.romlib.data.set.Provider;
import com.pixbits.lib.exceptions.FatalErrorException;
import com.pixbits.lib.io.xml.XMLParser;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.parser.SimpleParser;
import com.pixbits.lib.parser.SimpleTreeBuilder;

public class ClrMameProParserDat
{
  private final static Logger logger = Log.getLogger(ClrMameProParserDat.class);
  
  private final Options options;
  
  public ClrMameProParserDat(Options options)
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
  
  GameCataloguer cataloguer = new NoIntroCataloguer();
  
  RomSize.Set sizeSet;
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
      throw new IllegalArgumentException(String.format("required value %s in clrmamepro dat was not found at (%s:%d,%d).", missingKey.get(), path.getFileName().toString(), parser.getLine(), parser.getColumn()));
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
      game = null;
      rom = null;
      sizeSet = new RomSize.RealSet();
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
      
      GameSet set = new GameSet(
          null,
          Provider.DUMMY,
          /*TODO: header, */
          new GameList(games, sizeSet),
          null
      );
      
      set.info().setName(value("name"));
      set.info().setDescription(value("description"));
      set.info().setVersion(value("version"));
      set.info().setComment(value("comment"));
      set.info().setAuthor(value("author"));
      
      popState();
      
      
      Set<String> tags = new TreeSet<String>(); 
      for (String s : naming)
      {
        if (!TitleNormalizer.words.contains("("+s+")"))
           tags.add(s);
      }
      logger.i3("Tags which will not be automatically filtered: %s",tags.stream().collect(Collectors.joining(", ", "[", "]")));
      
      return set;
      
    }
    catch (NoSuchFileException e)
    {
      throw new FatalErrorException(e, "Dat file not found");
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
      throw new IllegalArgumentException("unrecognized key in dat: "+k+", "+v+" at "+parser.getLine()+":"+parser.getColumn());
  }
  
  Set<String> naming = new TreeSet<>();
  
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
        String name = value("name");
        
        int fi = 0;
        while (fi != -1)
        {
          fi = name.indexOf('(', fi);
          
          if (fi == -1) break;
          
          int si = name.indexOf(')', fi);
          naming.add(name.substring(fi+1, si));
          fi = si;
        }
        
        Game game = new Game(roms.toArray(new Rom[roms.size()]));
        game.setTitle(value("name"));
        game.setDescription(value("description"));
        cataloguer.catalogue(game);
        games.add(game);
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
        
        roms.add(new Rom(
          value("name"), 
          sizeSet.forBytes(valueOrDefault("size", -1L)), 
          valueOrDefault("crc", -1L), 
          valueOrDefault("md5", null), 
          valueOrDefault("sha1", null))
        );
        
        popState();
      }
    }
  }
}