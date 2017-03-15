package com.github.jakz.nit;

import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.github.jakz.nit.config.Config;
import com.github.jakz.nit.config.MergeOptions;
import com.github.jakz.nit.config.ScannerOptions;
import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.nit.data.xmdb.CloneSet;
import com.github.jakz.nit.emitter.CreatorOptions;
import com.github.jakz.nit.exceptions.FatalErrorException;
import com.github.jakz.nit.exceptions.RomPathNotFoundException;
import com.github.jakz.nit.gui.FrameSet;
import com.github.jakz.nit.gui.GameSetComparePanel;
import com.github.jakz.nit.gui.SimpleFrame;
import com.github.jakz.nit.merger.Merger;
import com.github.jakz.nit.scanner.Renamer;
import com.github.jakz.nit.scanner.HandleSet;
import com.github.jakz.nit.scanner.Scanner;
import com.github.jakz.nit.scanner.Verifier;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.LoggerFactory;
import com.pixbits.lib.log.ProgressLogger;
import com.pixbits.lib.ui.UIUtils;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Hello world!
 *
 */
public class Main 
{
  private static void initializeSevenZip() throws SevenZipNativeInitializationException
  {
    SevenZip.initSevenZipFromPlatformJAR();
  }
  private static final Logger logger = Log.getLogger(Main.class);
  
  public static FrameSet frames;
  
  public static void main(String[] args)
  {
    ArgumentParser arguments = Args.generateParser();
        
    try
    {
      Config config = Config.load(Paths.get("./config.json"));

      
      UIUtils.setNimbusLNF();
      //Operations.prepareGUIMode(config);
      
      //if (true)
      //  return;

      initializeSevenZip();
      
      if (false && args.length > 0)
      {
        Namespace rargs = arguments.parseArgs(args);
        System.out.println(rargs);
        
        Command command = rargs.get("command");
        
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
                    path -> Operations.loadGameSet(Options.simpleDatLoad(path))
                ))
                .collect(Collectors.toList());
            
            SimpleFrame<GameSetComparePanel> frame = new SimpleFrame<>("Game Set Compare", new GameSetComparePanel(sets), true);
            frame.setVisible(true);
            
            break;
          }
         
          case ORGANIZE:
          {
            
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

        return;
      }
      
      Options options = new Options();
      Log.setProgressLogger(ProgressLogger.STDOUT_PROGRESS);
      
      GameSet set = Operations.loadGameSet(options);
      CloneSet clones = Operations.loadCloneSetFromXMDB(set, options.cloneDatPath);
      set.setClones(clones);
      
      ScannerOptions soptions = new ScannerOptions();
      soptions.discardUnknownSizes = true;
      soptions.includeSubfolders = true;
      soptions.multithreaded = false;
      
      Scanner scanner = new Scanner(set, soptions);
      HandleSet handles = scanner.computeHandles(Arrays.asList(options.dataPath));
      
      Verifier verifier = new Verifier(options, set);
      
      int foundCount = verifier.verify(handles);
      
      logger.i1("Found %d verified roms", foundCount);
      //found.forEach(r -> Logger.log(Log.INFO3, "> %s", r.rom.game.name));
      
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
      }

      Operations.printStatistics(set);
      Operations.saveStatusOnTextFiles(set, options);
      
      /*RomHandle[] compress = found.stream().limit(2).map(rh -> rh.handle).toArray(i -> new RomHandle[i]);
      
      Compressor compressor = new Compressor(options);
      
      compressor.createArchive(Paths.get("/Volumes/RAMDisk/Archive.7z"), compress);*/
      
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
          stack[0].getLineNumber());
    }
    catch (RomPathNotFoundException e)
    {
      logger.e("unable to find specified rom path: "+e.path);
    }
    catch (SevenZipNativeInitializationException e)
    {
      logger.e("failed to initialize SevenZip library to manage archives, exiting:\n\t"+e.getMessage());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}