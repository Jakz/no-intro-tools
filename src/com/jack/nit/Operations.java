package com.jack.nit;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.xml.sax.SAXException;

import com.jack.nit.emitter.ClrMameProEmitter;
import com.jack.nit.emitter.CreatorOptions;
import com.jack.nit.emitter.GameSetCreator;
import com.jack.nit.gui.FrameSet;
import com.jack.nit.gui.GameSetListPanel;
import com.jack.nit.gui.GameSetMenu;
import com.jack.nit.gui.LogPanel;
import com.jack.nit.gui.SimpleFrame;
import com.jack.nit.config.MergeOptions;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.parser.ClrMameProParser;
import com.jack.nit.parser.DatFormat;
import com.jack.nit.parser.XMDBParser;
import com.jack.nit.scripts.ConsolePanel;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.io.XMLEmbeddedDTD;
import com.pixbits.lib.io.XMLParser;
import com.pixbits.lib.stream.StreamException;

public class Operations
{
  public static GameSet loadGameSet(Options options) throws IOException, SAXException
  {
    ClrMameProParser parser = new ClrMameProParser(options);
    GameSet set = parser.load(options.datPath);
    Log.log(Log.INFO1, "Loaded set \'"+set.info.name+"\' ("+set.size()+" games, "+set.realSize()+" roms)");

    return set;
  }
  
  public static CloneSet loadCloneSetFromXMDB(GameSet set, Path path) throws IOException, SAXException
  {
    XMLEmbeddedDTD resolver = new XMLEmbeddedDTD("GoodMerge.dtd", "com/jack/nit/parser/GoodMerge.dtd");    
    XMDBParser xparser = new XMDBParser(set);
    XMLParser<CloneSet> xmdbParser = new XMLParser<>(xparser, resolver);

    CloneSet cloneSet = xmdbParser.load(path);
        
    Log.log(Log.INFO1, "Loaded clone set for \'"+set.info.name+"\' ("+set.size()+" games in "+cloneSet.size()+" entries)");

    return cloneSet;
  }
  
  public static void printStatistics(GameSet set)
  {
    long found = set.foundRoms().count();
    
    Log.log("Statistics for %s:", set.info.name);
    Log.log("  %d total roms", set.size());
    
    if (set.clones() != null && set.clones().size() > 0)
      Log.log("  %d total games", set.clones().size());
    
    if (found > 0)
    {
      Log.log("  %d found roms (%d%%)", found, (found*100)/set.size());
      Log.log("  %d missing roms", set.size() - found);
    }
  }
  
  public static void cleanMergePath(GameSet set, Options options) throws IOException
  {
    Log.log(Log.INFO1, "Cleaning merge path from unneeded files");
    
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
    
    Log.log(Log.INFO1, "Generated game set from folders.");
    printStatistics(set);
    
    return set;
  }
    
  public static void consolidateGameSet(CreatorOptions options, GameSet set) throws IOException
  {
    if (options.format == DatFormat.clrmamepro)
    {
      ClrMameProEmitter generator = new ClrMameProEmitter();
      generator.generate(options, set);
    }
  }
  
  public static void saveStatusOnTextFiles(GameSet set, Options options) throws IOException
  {
    List<String> have = new ArrayList<>();
    List<String> miss = new ArrayList<>();
    
    set.stream().forEach(game -> {
      (game.isFound() ? have : miss).add(game.name);
    });
    
    Log.log(Log.INFO1, "Saving found status on files.");

    Path basePath = Files.isDirectory(options.dataPath[0]) ? options.dataPath[0] : options.dataPath[0].getParent();
    
    if (options.merge.mode != MergeOptions.Mode.NO_MERGE)
      basePath = options.mergePath();

    try (PrintWriter wrt = new PrintWriter(Files.newBufferedWriter(basePath.resolve("SetHave.txt"))))
    {
      wrt.printf(" You have %d of %d known %s games\n\n", have.size(), have.size()+miss.size(), set.info.name);      
      for (String h : have) wrt.println(h);
    }
    
    try (PrintWriter wrt = new PrintWriter(Files.newBufferedWriter(basePath.resolve("SetMiss.txt"))))
    {
      wrt.printf(" You are missing %d of %d known %s games\n\n", miss.size(), have.size()+miss.size(), set.info.name);      
      for (String h : miss) wrt.println(h);
    }
  }
  
  public static void prepareGUIMode(Path path) throws IOException
  {
    Main.frames = new FrameSet();
    SimpleFrame<LogPanel> logFrame = new SimpleFrame<>("Log", new LogPanel(40,120), false);
    Main.frames.add("log", logFrame);
    Log.setLogger(logFrame.getContent());
    
    PathMatcher datMatcher = FileSystems.getDefault().getPathMatcher("glob:*.dat");

    FolderScanner scanner = new FolderScanner(datMatcher, false);

    Set<Path> files = scanner.scan(path);
    
    
    List<GameSet> sets = files.stream().map(StreamException.rethrowFunction(p -> {
      GameSet set = Operations.loadGameSet(Options.simpleDatLoad(p));
      
      Path optionalXMDB = p.getParent().resolve(FileUtils.fileNameWithoutExtension(p) + ".xmdb");
      
      if (Files.exists(optionalXMDB))
      {
        CloneSet clones = Operations.loadCloneSetFromXMDB(set, optionalXMDB);
        set.setClones(clones);
      }
      
      return set;
    })).collect(Collectors.toList());
    
    SimpleFrame<GameSetListPanel> frame = new SimpleFrame<>("DAT Manager", new GameSetListPanel(sets), true);
    frame.setJMenuBar(new GameSetMenu());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    
    Main.frames.add("main", frame);
    
    
  }
  
  public static void openLogFrame() throws IOException
  {
    JFrame main = Main.frames.get("main");
    JFrame frame = Main.frames.get("log");
    //if (frame == null)
      
  }
  
  public static void openConsole() throws IOException
  {
    SimpleFrame<ConsolePanel> console = Main.frames.get("console");
    
    if (console == null)
    {
      ConsolePanel panel = new ConsolePanel();
      panel.setMySize(1024,768);
      console = new SimpleFrame<>("Console", new ConsolePanel(), true);
      Main.frames.add("console", console);
    }
    
    console.setLocationRelativeTo(null);
    console.setVisible(true);
  }
}
