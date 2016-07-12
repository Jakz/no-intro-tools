package com.jack.nit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.jack.nit.emitter.CreatorOptions;
import com.jack.nit.Options.MergeMode;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.exceptions.FatalErrorException;
import com.jack.nit.exceptions.RomPathNotFoundException;
import com.jack.nit.gui.GameSetComparePanel;
import com.jack.nit.gui.SimpleFrame;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.merger.Merger;
import com.jack.nit.scanner.Renamer;
import com.jack.nit.scanner.RomHandlesSet;
import com.jack.nit.scanner.Scanner;
import com.jack.nit.scanner.Verifier;
import com.pixbits.stream.StreamException;

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

  public static void main(String[] args)
  {
    Logger.init();

    
    ArgumentParser arguments = Args.generateParser();
    
    try
    {
      initializeSevenZip();
      
      if (args.length > 1)
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
          
        }
      }
      
            
 
      if (true)
        return;
      }
      
      Options options = new Options();

      
      Logger.init(options);
      
      GameSet set = Operations.loadGameSet(options);
      CloneSet clones = Operations.loadCloneSetFromXMDB(set, options.cloneDatPath);
      set.setClones(clones);

      
      Scanner scanner = new Scanner(set, options);
      RomHandlesSet handles = scanner.computeHandles();
      
      Verifier verifier = new Verifier(options, set);
      
      int foundCount = verifier.verify(handles);
      
      Logger.log(Log.INFO1, "Found %d verified roms", foundCount);
      //found.forEach(r -> Logger.log(Log.INFO3, "> %s", r.rom.game.name));
      
      Renamer renamer = new Renamer(options);
      renamer.rename(set.foundRoms().collect(Collectors.toList()));
      
      if (options.mergeMode != MergeMode.NO_MERGE)
      {
        Merger merger = new Merger(set, options);
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
      Logger.log(Log.ERROR, e.getMessage());
    }
    catch (RomPathNotFoundException e)
    {
      Logger.log(Log.ERROR, "unable to find specified rom path: "+e.path);
    }
    catch (SevenZipNativeInitializationException e)
    {
      Logger.log(Log.ERROR, "failed to initialize SevenZip library to manage archives, exiting:\n\t"+e.getMessage());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
