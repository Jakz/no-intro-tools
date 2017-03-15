package com.github.jakz.nit;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.xml.sax.SAXException;

import com.github.jakz.nit.config.Config;
import com.github.jakz.nit.config.GameSetConfig;
import com.github.jakz.nit.config.MergeOptions;
import com.github.jakz.nit.config.ScannerOptions;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.nit.data.xmdb.CloneSet;
import com.github.jakz.nit.emitter.ClrMameProEmitter;
import com.github.jakz.nit.emitter.CreatorOptions;
import com.github.jakz.nit.emitter.GameSetCreator;
import com.github.jakz.nit.gui.FrameSet;
import com.github.jakz.nit.gui.GameSetListPanel;
import com.github.jakz.nit.gui.GameSetMenu;
import com.github.jakz.nit.gui.LogPanel;
import com.github.jakz.nit.gui.SimpleFrame;
import com.github.jakz.nit.parser.ClrMameProParserDat;
import com.github.jakz.nit.parser.DatFormat;
import com.github.jakz.nit.parser.XMDBParser;
import com.github.jakz.nit.scanner.HandleSet;
import com.github.jakz.nit.scanner.Scanner;
import com.github.jakz.nit.scripts.ConsolePanel;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.io.xml.XMLEmbeddedDTD;
import com.pixbits.lib.io.xml.XMLParser;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.functional.StreamException;

public class Operations
{
  private final static Logger logger = Log.getLogger(Operations.class);
  
  public static GameSet loadGameSet(Options options) throws IOException, SAXException
  {
    ClrMameProParserDat parser = new ClrMameProParserDat(options);
    GameSet set = parser.load(options.datPath);
    logger.i("Loaded set \'"+set.info.name+"\' ("+set.size()+" games, "+set.filesCount()+" roms)");

    return set;
  }
  
  public static CloneSet loadCloneSetFromXMDB(GameSet set, Path path) throws IOException, SAXException
  {
    XMLEmbeddedDTD resolver = new XMLEmbeddedDTD("GoodMerge.dtd", "com/github/jakz/nit/parser/GoodMerge.dtd");    
    XMDBParser xparser = new XMDBParser(set);
    XMLParser<CloneSet> xmdbParser = new XMLParser<>(xparser, resolver);

    CloneSet cloneSet = xmdbParser.load(path);
        
    logger.i("Loaded clone set for \'"+set.info.name+"\' ("+set.size()+" games in "+cloneSet.size()+" entries)");

    return cloneSet;
  }
  
  public static void printStatistics(GameSet set)
  {
    long found = set.foundRoms().count();
    
    logger.i("Statistics for %s:", set.info.name);
    logger.i("  %d total roms", set.size());
    
    if (set.clones() != null && set.clones().size() > 0)
      logger.i("  %d total games", set.clones().size());
    
    if (found > 0)
    {
      logger.i("  %d found roms (%d%%)", found, (found*100)/set.size());
      logger.i("  %d missing roms", set.size() - found);
    }
  }
  
  public static HandleSet scanEntriesForGameSet(GameSet set, List<Path> paths, ScannerOptions options) throws IOException
  {
    Scanner scanner = new Scanner(set, options);
    return scanner.computeHandles(paths);
  }
  
  public static void cleanMergePath(GameSet set, Options options) throws IOException
  {
    logger.i("Cleaning merge path from unneeded files");
    
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
    
    logger.i("Generated game set from folders.");
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
    
    logger.i("Saving found status on files.");

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
  
  private Set<Path> scanFolderForDats(Path path) throws IOException
  {
    PathMatcher datMatcher = FileSystems.getDefault().getPathMatcher("glob:*.dat");
    FolderScanner scanner = new FolderScanner(datMatcher, false);
    return scanner.scan(path);
  }
  
  public static void prepareGUIMode(Config config) throws IOException
  {
    Main.frames = new FrameSet();
    SimpleFrame<LogPanel> logFrame = new SimpleFrame<>("Log", new LogPanel(40,120), false);
    Main.frames.add("log", logFrame);
    Log.setFactory(logFrame.panel(), true);
        
    List<GameSet> sets = config.dats.stream().map(StreamException.rethrowFunction(d -> {
      Path p = d.datFile;
      GameSet set = Operations.loadGameSet(Options.simpleDatLoad(p));
      
      Path optionalXMDB = d.xmdbFile;
      
      if (Files.exists(optionalXMDB))
      {
        CloneSet clones = Operations.loadCloneSetFromXMDB(set, optionalXMDB);
        set.setClones(clones);
      }
      
      set.setSystem(d.system);
      set.getConfig().romsetPath = d.romsetPaths.get(0);
      
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
