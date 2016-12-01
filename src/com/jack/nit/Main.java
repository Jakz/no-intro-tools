package com.jack.nit;

import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.jack.nit.emitter.CreatorOptions;
import com.jack.nit.config.ConfigFile;
import com.jack.nit.config.MergeOptions;
import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.exceptions.FatalErrorException;
import com.jack.nit.exceptions.RomPathNotFoundException;
import com.jack.nit.gui.FrameSet;
import com.jack.nit.gui.GameSetComparePanel;
import com.jack.nit.gui.SimpleFrame;
import com.jack.nit.log.Log;
import com.jack.nit.merger.Merger;
import com.jack.nit.scanner.Renamer;
import com.jack.nit.scanner.RomHandlesSet;
import com.jack.nit.scanner.Scanner;
import com.jack.nit.scanner.Verifier;
import com.pixbits.lib.stream.StreamException;

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
  
  public static void setLNF()
  {
    try {
      for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
          if ("Nimbus".equals(info.getName())) {
            
            UIManager.setLookAndFeel(info.getClassName());
            break;
          }
      }
    } catch (Exception e) {
      e.printStackTrace();
        // If Nimbus is not available, you can set the GUI to another look and feel.
    }
  }
  
  public static FrameSet frames;
  
  public static void main(String[] args)
  {
    Log.init();
    
    ConfigFile file = ConfigFile.load(Paths.get("./config.json"));
    file.verify();
    
    if (true)
      return;

    ArgumentParser arguments = Args.generateParser();
    
    try
    {
      setLNF();
      //Operations.prepareGUIMode(Paths.get("dats/"));

      initializeSevenZip();
      
      if (args.length > 0)
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
         
          case VERIFY:
          {
            
            break;
          }
          
          case GUI:
          {
            setLNF();
            
            Path path = Paths.get(rargs.getString("folder")).normalize();
            
            if (!Files.exists(path) || !Files.isDirectory(path))
              throw new ArgumentParserException(String.format("path '%s' doesn't exist or it's not a directory", path.toString()), arguments);
            
            Operations.prepareGUIMode(path);
            
            break;
          }
          
          case CONSOLE:
          {
            setLNF();
            Operations.openConsole();
            break;
          }
        }

        return;
      }
      
      Options options = new Options();

      
      Log.init(options);
      
      GameSet set = Operations.loadGameSet(options);
      CloneSet clones = Operations.loadCloneSetFromXMDB(set, options.cloneDatPath);
      set.setClones(clones);

      
      Scanner scanner = new Scanner(set, options);
      RomHandlesSet handles = scanner.computeHandles();
      
      Verifier verifier = new Verifier(options, set);
      
      int foundCount = verifier.verify(handles);
      
      Log.log(Log.INFO1, "Found %d verified roms", foundCount);
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
      Log.log(Log.ERROR, e.getMessage());
    }
    catch (RomPathNotFoundException e)
    {
      Log.log(Log.ERROR, "unable to find specified rom path: "+e.path);
    }
    catch (SevenZipNativeInitializationException e)
    {
      Log.log(Log.ERROR, "failed to initialize SevenZip library to manage archives, exiting:\n\t"+e.getMessage());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
