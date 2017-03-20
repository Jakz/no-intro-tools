package com.github.jakz.nit.parser;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.nit.data.xmdb.CloneSet;
import com.github.jakz.nit.data.xmdb.GameClone;
import com.github.jakz.romlib.data.game.Location;
import com.pixbits.lib.io.xml.XMLEmbeddedDTD;
import com.pixbits.lib.io.xml.XMLHandler;
import com.pixbits.lib.io.xml.XMLParser;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;

public class XMDBParser extends XMLHandler<CloneSet>
{
  private final static Map<String, Location> zoneMap = new HashMap<>();
  
  static
  {
    zoneMap.put("J", Location.JAPAN);
    zoneMap.put("U", Location.USA);
    zoneMap.put("E", Location.EUROPE);
    zoneMap.put("S", Location.SPAIN);
    zoneMap.put("G", Location.GERMANY);
    zoneMap.put("F", Location.FRANCE);
    zoneMap.put("I", Location.ITALY);
    zoneMap.put("A", Location.AUSTRALIA);
    zoneMap.put("Ne", Location.NETHERLANDS);
    zoneMap.put("Da", Location.DENMARK);
    zoneMap.put("Sw", Location.SWEDEN);
    zoneMap.put("K", Location.KOREA);
    zoneMap.put("Cn", Location.CHINA);
    zoneMap.put("Ca", Location.CANADA);
    zoneMap.put("Br", Location.BRASIL);
    zoneMap.put("As", Location.ASIA);
    zoneMap.put("Th", Location.TAIWAN);
    zoneMap.put("No", Location.NORWAY);
    zoneMap.put("Ru", Location.RUSSIA);
  }
  
  
  private final static Logger logger = Log.getLogger(XMDBParser.class);
  
  
  
  
  List<GameClone> clones;
  GameSet set;
  Game[] zones;
  List<Game> clone;
  
  public XMDBParser(GameSet set)
  {
    this.set = set;
  }
  
  @Override protected void init()
  {
    
  }
  
  @Override protected void end(String ns, String name)
  {
    if (name.equals("zoned"))
    {
      GameClone clone = new GameClone(this.clone.toArray(new Game[this.clone.size()]), zones);
      clones.add(clone);
    }
  }
  
  @Override protected void start(String ns, String name, Attributes attr) throws SAXException
  {
    if (name.equals("parents"))
    {
      clones = new ArrayList<>();
    }
    else if (name.equals("zoned"))
    {
      zones = new Game[Location.values().length];
      clone = new ArrayList<>();
    }
    else if (name.equals("bias"))
    {
      Game game = set.get(attrString("name"));
      Location zone = zoneMap.get(attrString("zone"));
                
      if (game == null)
      {
        logger.w("Zoned clone '"+attrString("name")+"' is not present in corresponding game set");
        return;
      }
      if (zone == null)
        throw new NoSuchElementException("zone found in zoned rom is not valid: "+attrString("zone"));
      
      zones[zone.ordinal()] = game;
    }
    else if (name.equals("clone"))
    {
      Game game = set.get(attrString("name"));

      if (game == null)
      {
        logger.w("Game clone '"+attrString("name")+"' is not present in corresponding game set");
        return;
      }
              
      clone.add(game);
    }
  }

  @Override public CloneSet get()
  {
    return new CloneSet(set, clones.toArray(new GameClone[clones.size()]));
  }
  
  
  public static CloneSet loadCloneSet(GameSet set, Path path) throws IOException, SAXException
  {
    final String dtdName = "GoodMerge.dtd";
    final String packageName = MethodHandles.lookup().lookupClass().getPackage().getName().replaceAll("\\.", "/");
    
    XMLEmbeddedDTD resolver = new XMLEmbeddedDTD(dtdName, packageName + "/" + dtdName);    
    XMDBParser xparser = new XMDBParser(set);
    XMLParser<CloneSet> xmdbParser = new XMLParser<>(xparser, resolver);

    CloneSet cloneSet = xmdbParser.load(path);
    return cloneSet;
  }
}
