package com.jack.nit;

import java.io.IOException;
import java.nio.file.Path;

import org.xml.sax.SAXException;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.parser.DatParser;
import com.jack.nit.parser.XMDBParser;
import com.pixbits.io.XMLEmbeddedDTD;
import com.pixbits.io.XMLParser;

public class Operations
{
  public static GameSet loadGameSet(Options options) throws IOException, SAXException
  {
    DatParser parser = new DatParser(options);
    GameSet set = parser.load(options.datPath);
    Logger.log(Log.INFO1, "Loaded set \'"+set.name+"\' ("+set.size()+" games, "+set.realSize()+" roms)");

    return set;
  }
  
  public static CloneSet loadCloneSetFromXMDB(GameSet set, Path path) throws IOException, SAXException
  {
    XMLEmbeddedDTD resolver = new XMLEmbeddedDTD("GoodMerge.dtd", "com/jack/nit/parser/GoodMerge.dtd");    
    XMDBParser xparser = new XMDBParser(set);
    XMLParser<CloneSet> xmdbParser = new XMLParser<>(xparser, resolver);

    CloneSet cloneSet = xmdbParser.load(path);
    
    Logger.log(Log.INFO1, "Loaded clone set for \'"+set.name+"\' ("+set.size()+" games in "+cloneSet.size()+" entries)");

    return cloneSet;
  }
}
