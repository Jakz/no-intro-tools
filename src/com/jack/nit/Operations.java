package com.jack.nit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xml.sax.SAXException;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.GameSetStatus;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.parser.DatParser;
import com.jack.nit.parser.XMDBParser;
import com.pixbits.io.FolderScanner;
import com.pixbits.io.XMLEmbeddedDTD;
import com.pixbits.io.XMLParser;
import com.pixbits.stream.StreamException;

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
  
  public static void printStatistics(GameSetStatus set, Options options)
  {
    long found = set.set.foundRoms().count();
    
    Logger.log("Statistics for %s:", set.set.name);
    Logger.log("  %d total roms", set.set.size());
    if (set.clones != null)
      Logger.log("  %d total games", set.clones.size());
    Logger.log("  %d found roms (%d%%)", found, (found*100)/set.set.size());
    Logger.log("  %d missing roms", set.set.size() - found);
  }
  
  public static void cleanMergePath(GameSet set, Options options) throws IOException
  {
    Logger.log(Log.INFO1, "Cleaning merge path from unneeded files");
    
    FolderScanner scanner = new FolderScanner(true);
    
    Set<Path> romFiles = set.foundRoms().map(r -> r.handle().file()).collect(Collectors.toSet());
    
    Set<Path> files = scanner.scan(options.mergePath());
    
    files.removeAll(romFiles);
    
    files.forEach(StreamException.rethrowConsumer(f -> Files.delete(f)));
  }
}
