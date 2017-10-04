package com.github.jakz.nit;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.jakz.nit.config.Config;
import com.github.jakz.nit.config.MergeOptions;
import com.github.jakz.nit.emitter.CreatorOptions;
import com.github.jakz.nit.gui.GameSetComparePanel;
import com.github.jakz.nit.gui.SimpleFrame;
import com.github.jakz.nit.merger.Merger;
import com.github.jakz.nit.scanner.Renamer;
import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.set.Feature;
import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.exceptions.FatalErrorException;
import com.pixbits.lib.exceptions.FileNotFoundException;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.archive.HandleSet;
import com.pixbits.lib.io.archive.ScannerOptions;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.StdoutProgressLogger;
import com.pixbits.lib.ui.UIUtils;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main
{
  static void initializeSevenZip() throws SevenZipNativeInitializationException
  {
    try
    {
      SevenZip.initSevenZipFromPlatformJAR();
    }
    catch (SevenZipNativeInitializationException e)
    {
      throw new FatalErrorException(e, "Error while initializing 7z library");
    }
  }
  
  public static void main(String[] args)
  {
    executeCLI(args, false);
  }
  
  public static void init()
  {
    Log.setFactory(StdoutProgressLogger.ONE_LINER_BUILDER);

  }
  
  public static void executeCLI(String[] args, boolean devMode)
  {  
    UIUtils.setNimbusLNF();
    final Logger logger = Log.getLogger(Main.class);
    
    ArgumentParser arguments = Args.generateParser();

    try
    {
      initializeSevenZip();
      
      Namespace rargs = arguments.parseArgs(args);
      System.out.println(rargs);
    
      Command command = rargs.get("command");
      
      if (command != Command.ORGANIZE && !devMode)
      {
        System.err.println("Only organize command is available through the release build.");
        return;
      }
    
      switch (command)
      {
        case CREATE_DAT:
        {
          CreatorOptions coptions = new CreatorOptions(rargs.getAttrs());
          GameSet set = Operations.createGameSet(coptions);
          Operations.consolidateGameSet(coptions, set);
          break;
        }
        
        case COMPARE_DAT:
        {
          List<String> dats = rargs.getList("infile");
          if (dats.size() < 2)
            throw new ArgumentParserException("compare-dat expects at least 2 DAT files", arguments);
                    
          List<Path> paths = dats.stream().map(Paths::get).collect(Collectors.toList());
          
          List<GameSet> sets = paths.stream()
            .map(StreamException.rethrowFunction(
              path -> Operations.loadClrMameGameSet(Options.simpleDatLoad(path))
                ))
                  .collect(Collectors.toList());
          
          SimpleFrame<GameSetComparePanel> frame = new SimpleFrame<>("Game Set Compare", new GameSetComparePanel(sets), true);
          frame.setVisible(true);
          
          break;
        }
       
        case ORGANIZE:
        {
          Options options = new Options(rargs);
          GameSet set = Operations.loadGameSet(options);
          set.load();
          logger.i("Loaded '%s' romset, composed by %d roms in %d games", set.info().getName(), set.info().romCount(), set.info().gameCount());
          
          ScannerOptions soptions = new ScannerOptions();          
          HandleSet handles = Operations.scanEntriesForGameSet(set, Arrays.asList(options.dataPath), soptions, true);
          
          if (set.hasFeature(Feature.SHARED_ROM_BETWEEN_GAMES))
            logger.w("Romset has roms which are shared between multiple games, this implies that multiple copies are expected");
          
          Operations.verifyGameSet(set, handles, options);
          
          if (!options.skipRename)
          {
            Renamer renamer = new Renamer(options);
            renamer.rename(set.foundRoms().collect(Collectors.toList()));
          }
          
          if (options.merge.mode != MergeOptions.Mode.NO_MERGE)
          {              
            Predicate<Game> predicate = g -> true;
            
            /*Searcher searcher = new Searcher();
            Predicate<Game> predicate = searcher.buildExportByRegionPredicate(Location.ITALY, Location.EUROPE, Location.USA);
            predicate.and(searcher.buildPredicate("is:proper is:licensed"));*/
               
            Merger merger = new Merger(set, predicate, options);
            merger.merge(options.mergePath());
            
            if (options.cleanMergePathAfterMerge)
              Operations.cleanMergePath(set, options);
            
            if (options.verifyMerge)
              Operations.verifySuccessfulMerge(set, options);
          }
          else
            logger.i("Skipping merge phase");
  
          Operations.printStatistics(set);
          Operations.saveStatusOnTextFiles(set, options);
     
          break;
        }
        
        case GUI:
        {
          UIUtils.setNimbusLNF();
  
          Path path = Paths.get(rargs.getString("cfgfile"));
          
          if (!Files.exists(path) || Files.isDirectory(path))
            throw new FatalErrorException(String.format("config file %s doesn't exists", path.toString()));
          
          Config cfg = Config.load(path);
          
          Operations.prepareGUIMode(cfg);
          
          break;
        }
        
        case CONSOLE:
        {
          UIUtils.setNimbusLNF();
          Operations.openConsole();
          break;
        }
      }
    }
    catch (ArgumentParserException e)
    {
      arguments.handleError(e);
    }
    catch (FatalErrorException e)
    {
      StackTraceElement[] stack = e.getStackTrace();
      logger.e("%s at %s.%s(...) : %d",
        e.getMessage(), 
        stack[0].getClassName(),
        stack[0].getMethodName(),
        stack[0].getLineNumber()
      );
      e.printStackTrace();
    }
    catch (FileNotFoundException e)
    {
      logger.e("unable to find specified path: "+e.path);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}