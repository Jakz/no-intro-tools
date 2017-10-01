package com.github.jakz.nit;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.xml.sax.SAXException;

import com.github.jakz.nit.config.Config;
import com.github.jakz.nit.config.MergeOptions;
import com.github.jakz.nit.emitter.ClrMameProEmitter;
import com.github.jakz.nit.emitter.CreatorOptions;
import com.github.jakz.nit.emitter.GameSetCreator;
import com.github.jakz.nit.gui.FrameSet;
import com.github.jakz.nit.gui.GameSetListPanel;
import com.github.jakz.nit.gui.GameSetMenu;
import com.github.jakz.nit.gui.LogPanel;
import com.github.jakz.nit.gui.SimpleFrame;
import com.github.jakz.nit.parser.ClrMameProParserDat;
import com.github.jakz.nit.scripts.ConsolePanel;
import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.platforms.Platform;
import com.github.jakz.romlib.data.set.CloneSet;
import com.github.jakz.romlib.data.set.DataSupplier;
import com.github.jakz.romlib.data.set.GameList;
import com.github.jakz.romlib.data.set.GameSet;
import com.github.jakz.romlib.data.set.GameSetAttribute;
import com.github.jakz.romlib.data.set.Provider;
import com.github.jakz.romlib.parsers.LogiqxXMLHandler;
import com.github.jakz.romlib.parsers.XMDBHandler;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.io.archive.HandleSet;
import com.pixbits.lib.io.archive.Scanner;
import com.pixbits.lib.io.archive.ScannerOptions;
import com.pixbits.lib.io.archive.Verifier;
import com.pixbits.lib.io.archive.VerifierEntry;
import com.pixbits.lib.io.archive.VerifierHelper;
import com.pixbits.lib.io.archive.VerifierResult;
import com.pixbits.lib.io.archive.handles.ArchiveHandle;
import com.pixbits.lib.io.archive.handles.BinaryHandle;
import com.pixbits.lib.io.archive.handles.Handle;
import com.pixbits.lib.io.archive.handles.NestedArchiveHandle;
import com.pixbits.lib.lang.Pair;
import com.pixbits.lib.lang.StringUtils;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.ProgressLogger;
import com.pixbits.lib.exceptions.FatalErrorException;
import com.pixbits.lib.functional.StreamException;

public class Operations
{
  private final static Logger logger = Log.getLogger(Operations.class);
  
  public static DatType guessFormat(Options options)
  {
    if (options.datFormat != DatType.UNSPECIFIED)
      return options.datFormat;
    else if (options.datPath.toString().endsWith(".dat"))
      return DatType.CLR_MAME_PRO;
    else if (options.datPath.toString().endsWith(".xml"))
      return DatType.LOGIQX;
    else
      return DatType.UNSPECIFIED;
  }
  
  
  public static GameSet loadGameSet(Options options) throws IOException, SAXException
  {
    DatType format = guessFormat(options);
    
    if (format == DatType.UNSPECIFIED)
      throw new FatalErrorException("Unable to guess dat format, please specify it explicitly.");
    else
    {
      switch (format)
      {
        case CLR_MAME_PRO:
        {
          return loadClrMameGameSet(options);
        }
        
        case LOGIQX:
        {
          return loadLogiqxDat(options);
        }
      }
    }
    
    return null;
  }
  
  public static GameSet loadLogiqxDat(Options options) throws IOException, SAXException
  {
    LogiqxXMLHandler.Data supplier = LogiqxXMLHandler.load(options.datPath);
    
    String name = supplier.setAttributes.get("name");
    
    GameSet set = new GameSet(Platform.of(name), new Provider(name, "", null), supplier.list, null);
    set.info().setAttribute(GameSetAttribute.NAME, supplier.setAttributes.get("name"));
    //TODO: add others
    return set;
  }
  
  public static GameSet loadClrMameGameSet(Options options) throws IOException, SAXException
  {
    ClrMameProParserDat parser = new ClrMameProParserDat(options);
    GameSet set = parser.load(options.datPath);
    logger.i("Loaded set \'"+set.info().getName()+"\' ("+set.gameCount()+" games, "+set.info().romCount()+" roms)");

    return set;
  }
  
  public static CloneSet loadCloneSetFromXMDB(GameSet set, Path path) throws IOException, SAXException
  {
    CloneSet cloneSet = XMDBHandler.loadCloneSet(set, path);
    logger.i("Loaded clone set for \'"+set.info().getName()+"\' ("+set.gameCount()+" games in "+cloneSet.size()+" entries)");
    return cloneSet;
  }
  
  public static void printStatistics(GameSet set)
  {
    long found = set.foundRoms().count();
    
    logger.i("Statistics for %s:", set.info().getName());
    logger.i("  %d total roms", set.info().romCount());
    
    if (set.clones() != null && set.clones().size() > 0)
    {
      long orphaned = set.stream().filter(g -> !g.hasClone()).count();
      
      logger.i("  %d total games", set.clones().size() + orphaned);
      logger.i("  %d orphan games", orphaned);
    }
    
    if (found > 0)
    {
      logger.i("  %d found roms (%d%%)", found, (found*100)/set.info().romCount());
      logger.i("  %d missing roms", set.info().romCount() - found);
      
      long completedGames = set.stream().filter(Game::isComplete).count();
      logger.i("  %d of %d completed games (%d%%)", completedGames, set.info().gameCount(), (completedGames*100)/set.info().gameCount());
    }
  }
  
  public static HandleSet scanEntriesForGameSet(GameSet set, List<Path> paths, ScannerOptions options, boolean discardUnknownSizes) throws IOException
  {
    Logger logger = Log.getLogger(Scanner.class);
    
    FolderScanner folderScanner = new FolderScanner(true);
    final Set<Path> pathsToScan = folderScanner.scan(paths);
    
    Scanner scanner = new Scanner(options);
    
    
    List<VerifierEntry> skipped = new ArrayList<>();
       
    options.assumeCRCisCorrect = true; /*TODO: set.header == null;*/
    options.shouldSkip = s -> !set.hashCache().isValidSize(s.getVerifierHandle().size()) && discardUnknownSizes;
    
    options.onEntryFound = h -> logger.i("Found entry: %s", h.toString());
    options.onSkip = h -> skipped.add(h);
    options.onFaultyArchive = p -> logger.w("File "+p.toString()+" is not a valid archive.");
    
    // set.cache().isValidSize(s.size) || !options.discardUnknownSizes; //
    
    HandleSet handles = new HandleSet(scanner.scanPaths(pathsToScan.stream()));
    HandleSet.Stats stats = handles.stats();

    logger.i1("Found %d potential matches (%d binary, %d inside archives, %d nested inside %d archives).", 
        stats.totalHandles, stats.binaryCount, stats.archiveCount, stats.nestedArchiveInnerCount, stats.nestedArchiveCount);
    
    if (!skipped.isEmpty())
      logger.i1("Skipped %d entries:", skipped.size());
    
    skipped.forEach(s -> logger.i3("> %s", s));
    
    /*GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeHierarchyAdapter(Handle.class, new JsonHandleAdapter());
    builder.registerTypeHierarchyAdapter(Path.class, new PathAdapter());
    
    Gson gson = builder.create();
    
    try (FileWriter wrt = new FileWriter("test.json"))
    {
      gson.toJson(handles.stream().collect(Collectors.toList()), new TypeToken<List<Handle>>(){}.getType(), wrt);
    }*/
    
    
    return handles;
  }
  
  public static void verifyGameSet(GameSet set, HandleSet handles, Options options) throws IOException, NoSuchAlgorithmException
  {
    final ProgressLogger verifierProgress = Log.getProgressLogger(Verifier.class);
    final Logger logger = Log.getLogger(Verifier.class);

    AtomicInteger binaryCount = new AtomicInteger();
    AtomicInteger archiveCount = new AtomicInteger();
    AtomicInteger nestedCount = new AtomicInteger();
    AtomicInteger totalVerified = new AtomicInteger();
    
    final int totalCount = handles.size();

    final Consumer<List<VerifierResult<Rom>>> callback = results -> {
      for (VerifierResult<Rom> result : results)
      {
        Handle handle = result.handle;
        Rom rom = result.element;
        
        if (handle instanceof BinaryHandle)
          binaryCount.getAndIncrement();
        else if (handle instanceof ArchiveHandle)
          archiveCount.getAndIncrement();
        else if (handle instanceof NestedArchiveHandle)
          nestedCount.getAndIncrement();
        
        int current = totalVerified.incrementAndGet();
        
        if (handle != null)
          verifierProgress.updateProgress(current / (float)totalCount, handle.toString());
        
        if (handle != null && rom != null)
        {
          if (set.hasSharedRomsBetweenGames() && rom.handle() != null)
          {
            Set<Pair<Rom,Game>> games = set.sharedRomMap().gamesForRom(rom);
            Optional<Rom> anyRom = games.stream().filter(p -> p.first.handle() == null).map(p -> p.first).findAny();
            
            rom = anyRom.isPresent() ? anyRom.get() : rom;
          }  
          
          if (rom.handle() != null)
          {
            logger.w("Duplicate entry found for %s", rom.name);
            logger.d("  Already present file: %s", rom.handle().toString());
            logger.d("  Duplicate found: %s", handle.toString());
          }
          else
            rom.setHandle(handle);
        } 
      }
    };
    
    options.verifier.matchMD5 = false;
    options.verifier.matchSHA1 = false;
      
    final VerifierHelper<Rom> verifier = new VerifierHelper<Rom>(options.verifier, options.multiThreaded, set.hashCache(), callback);
    
    verifier.setReporter(r -> {
      if (r.type == VerifierHelper.Report.Type.START)
        verifierProgress.startProgress(Log.INFO1, "Verifying " + totalCount + " roms...");
      else if (r.type == VerifierHelper.Report.Type.END)
        verifierProgress.endProgress();
    });

    
    verifier.verify(handles);
    
    logger.i1("Found %d verified roms", binaryCount.get() + archiveCount.get() + nestedCount.get());
  }
  
  public static void duplicateSharedRomsIfNeeded(GameSet set, Options options)
  {
    if (set.hasSharedRomsBetweenGames())
    {
      set.sharedRomMap().stream().forEach(rset -> {
        /* for each group of shared roms which has at least one missing rom */
        if (rset.stream().anyMatch(p -> !p.first.isPresent()))
        {
          // TODO
        }
        
      });
    }
  }
  
  public static void cleanMergePath(GameSet set, Options options) throws IOException
  {
    logger.i("Cleaning merge path from unneeded files");
    
    FolderScanner scanner = new FolderScanner(true);
    
    Set<Path> romFiles = set.foundRoms().map(r -> r.handle().path()).collect(Collectors.toSet());
    
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
    if (options.format.is("clr-mame-pro"))
    {
      ClrMameProEmitter generator = new ClrMameProEmitter();
      generator.generate(options, set);
    }
    else
      throw new UnsupportedOperationException("Unknown dat format: "+options.format.getIdent());
  }
  
  public static void saveStatusOnTextFiles(GameSet set, Options options) throws IOException
  {
    List<String> have = new ArrayList<>();
    List<String> miss = new ArrayList<>();
    
    set.stream().forEach(game -> {
      (game.isComplete() ? have : miss).add(game.getTitle());
    });
    
    logger.i("Saving found status on files.");

    Path basePath = Files.isDirectory(options.dataPath[0]) ? options.dataPath[0] : options.dataPath[0].getParent();
    
    if (options.merge.mode != MergeOptions.Mode.NO_MERGE)
      basePath = options.mergePath();

    try (PrintWriter wrt = new PrintWriter(Files.newBufferedWriter(basePath.resolve("SetHave.txt"))))
    {
      wrt.printf(" You have %d of %d known %s games\n\n", have.size(), have.size()+miss.size(), set.info().getName());      
      for (String h : have) wrt.println(h);
    }
    
    try (PrintWriter wrt = new PrintWriter(Files.newBufferedWriter(basePath.resolve("SetMiss.txt"))))
    {
      wrt.printf(" You are missing %d of %d known %s games\n\n", miss.size(), have.size()+miss.size(), set.info().getName());      
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
        
    Map<GameSet, Config.DatEntry> setData = new HashMap<>();
    
    List<GameSet> sets = config.dats.stream().map(StreamException.rethrowFunction(d -> {
      Path p = d.datFile;
      GameSet set = Operations.loadGameSet(Options.simpleDatLoad(p));
      
      Path optionalXMDB = d.xmdbFile;
      
      if (Files.exists(optionalXMDB))
      {
        CloneSet clones = Operations.loadCloneSetFromXMDB(set, optionalXMDB);
        set.setClones(clones);
      }

      setData.put(set, d);      
      return set;
    })).collect(Collectors.toList());
    
    SimpleFrame<GameSetListPanel> frame = new SimpleFrame<>("DAT Manager", new GameSetListPanel(sets, setData), true);
    frame.setJMenuBar(new GameSetMenu());
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    
    Main.frames.add("main", frame);
    
    
  }
  
  public static void openLogFrame() throws IOException
  {
    Main.frames.get("main");
    Main.frames.get("log");
      
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
