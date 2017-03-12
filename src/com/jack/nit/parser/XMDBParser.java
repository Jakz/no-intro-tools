package com.jack.nit.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.xml.sax.Attributes;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.data.xmdb.Zone;
import com.jack.nit.log.Log;
import com.pixbits.lib.io.xml.XMLHandler;

public class XMDBParser extends XMLHandler<CloneSet>
{
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
  
  @Override protected void end(String name)
  {
    if (name.equals("zoned"))
    {
      GameClone clone = new GameClone(this.clone.toArray(new Game[this.clone.size()]), zones);
      clones.add(clone);
    }
  }
  
  @Override protected void start(String name, Attributes attr)
  {
    if (name.equals("parents"))
    {
      clones = new ArrayList<>();
    }
    else if (name.equals("zoned"))
    {
      zones = new Game[Zone.values().length];
      clone = new ArrayList<>();
    }
    else if (name.equals("bias"))
    {
      Game game = set.get(attrString("name"));
      Zone zone = Zone.forTinyName(attrString("zone"));
      
      if (game == null)
      {
        Log.log(Log.WARNING, "Zoned clone '"+attrString("name")+"' is not present in corresponding game set");
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
        Log.log(Log.WARNING, "Game clone '"+attrString("name")+"' is not present in corresponding game set");
        return;
      }
              
      clone.add(game);
    }
  }

  @Override public CloneSet get()
  {
    return new CloneSet(set, clones.toArray(new GameClone[clones.size()]));
  }

}
