package com.jack.nit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xml.sax.SAXException;

import com.jack.nit.emitter.ClrMameProGenerator;
import com.jack.nit.emitter.CreatorOptions;
import com.jack.nit.emitter.GameSetCreator;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.Rom;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.parser.ClrMameProParser;
import com.jack.nit.parser.DatFormat;
import com.jack.nit.parser.XMDBParser;
import com.pixbits.io.FolderScanner;
import com.pixbits.io.XMLEmbeddedDTD;
import com.pixbits.io.XMLParser;
import com.pixbits.stream.StreamException;

public class Operations
{
  public static GameSet loadGameSet(Options options) throws IOException, SAXException
  {
    ClrMameProParser parser = new ClrMameProParser(options);
    GameSet set = parser.load(options.datPath);
    Logger.log(Log.INFO1, "Loaded set \'"+set.info.name+"\' ("+set.size()+" games, "+set.realSize()+" roms)");

    return set;
  }
  
  public static CloneSet loadCloneSetFromXMDB(GameSet set, Path path) throws IOException, SAXException
  {
    XMLEmbeddedDTD resolver = new XMLEmbeddedDTD("GoodMerge.dtd", "com/jack/nit/parser/GoodMerge.dtd");    
    XMDBParser xparser = new XMDBParser(set);
    XMLParser<CloneSet> xmdbParser = new XMLParser<>(xparser, resolver);

    CloneSet cloneSet = xmdbParser.load(path);
        
    Logger.log(Log.INFO1, "Loaded clone set for \'"+set.info.name+"\' ("+set.size()+" games in "+cloneSet.size()+" entries)");

    return cloneSet;
  }
  
  public static void printStatistics(GameSet set)
  {
    long found = set.foundRoms().count();
    
    Logger.log("Statistics for %s:", set.info.name);
    Logger.log("  %d total roms", set.size());
    
    if (set.clones() != null && set.clones().size() > 0)
      Logger.log("  %d total games", set.clones().size());
    
    if (found > 0)
    {
      Logger.log("  %d found roms (%d%%)", found, (found*100)/set.size());
      Logger.log("  %d missing roms", set.size() - found);
    }
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
  
  public static GameSet createGameSet(CreatorOptions options) throws IOException
  {
    GameSetCreator creator = new GameSetCreator(options);
    GameSet set = creator.create();
    
    Logger.log(Log.INFO1, "Generated game set from folders.");
    printStatistics(set);
    
    return set;
  }
  
  public static void consolidateGameSet(CreatorOptions options, GameSet set) throws IOException
  {
    if (options.format == DatFormat.clrmamepro)
    {
      ClrMameProGenerator generator = new ClrMameProGenerator();
      generator.generate(options, set);
    }
  }
}
