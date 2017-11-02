package com.github.jakz.nit;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.jakz.nit.config.Config;
import com.github.jakz.nit.config.MergeOptions;
import com.github.jakz.nit.emitter.CreatorOptions;
import com.github.jakz.nit.gui.FrameSet;
import com.github.jakz.nit.gui.GameSetComparePanel;
import com.github.jakz.nit.gui.SimpleFrame;
import com.github.jakz.nit.merger.Merger;
import com.github.jakz.nit.scanner.Renamer;
import com.github.jakz.romlib.data.cataloguers.NoIntroCataloguer;
import com.github.jakz.romlib.data.game.BiasSet;
import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.GameClone;
import com.github.jakz.romlib.data.game.Location;
import com.github.jakz.romlib.data.game.RomSize;
import com.github.jakz.romlib.data.platforms.GBC;
import com.github.jakz.romlib.data.platforms.Platform;
import com.github.jakz.romlib.data.set.CloneSet;
import com.github.jakz.romlib.data.set.DataSupplier;
import com.github.jakz.romlib.data.set.Feature;
import com.github.jakz.romlib.data.set.GameList;
import com.github.jakz.romlib.data.set.GameSet;
import com.github.jakz.romlib.data.set.Provider;
import com.github.jakz.romlib.parsers.LogiqxXMLHandler;
import com.github.jakz.romlib.parsers.XMDBHandler;
import com.pixbits.lib.exceptions.FatalErrorException;
import com.pixbits.lib.exceptions.FileNotFoundException;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.io.archive.HandleSet;
import com.pixbits.lib.io.archive.ScannerOptions;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.ProgressLoggerFactory;
import com.pixbits.lib.log.StdoutProgressLogger;
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
public class DevMain 
{
  private static final Logger logger = Log.getLogger(DevMain.class);
  
  public static FrameSet frames;
  
  public static void main(String[] args)
  {    
    try
    {
      Options mopt = new Options();  
      BatchOptions bopt = new BatchOptions();
      Operations.scanFolderForDats(bopt, mopt);
      if (true)
        return;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    
    args = new String[] {
        "organize", 
        "--dat-file", "dats/n64-with-clones.xml", 
        "--dat-format", "logiqx", 
        "--clones-file", "dats/n64-with-clones.xmdb",
        
        "--roms-path", "/Volumes/RAMDisk/Organized", 
        //"--roms-path", "/Volumes/RAMDisk/Organized/Nintendo - SNES - NoIntro 2017-10-03.zip",
        //"--roms-path", "/Volumes/Vicky/Movies HD/Nintendo - Nintendo 64 [Big Endian]", 
        
        "--fast", 
        "--skip-rename",
        
        "--no-merge",

        "--merge-path", "/Volumes/RAMDisk/Organized", 
        "--merge-mode", "archive-by-clone"
        //, "--force-folder-per-game"
        , "--auto-merge-clones"
    };
 
    try
    {
      if (args.length > 0)
      {
        Main.executeCLI(args, true);
        return;
      }
      
      Main.initializeSevenZip();
      Log.setFactory(StdoutProgressLogger.ONE_LINER_BUILDER);

      Options options = new Options();
      Log.setFactory(StdoutProgressLogger.PLAIN_BUILDER);
      
      GameSet set = null;

      {
        final NoIntroCataloguer cataloguer = new NoIntroCataloguer();

        LogiqxXMLHandler.Data supplier;
        GameList list;
        
        /*DataSupplier supplier = LogiqxXMLParser.load(Paths.get("./dats/nes.xml"));
        GameList list = supplier.load(null).games.get();
        list.stream().forEach(cataloguer::catalogue);*/         
        
        supplier = LogiqxXMLHandler.load(Paths.get("./dats/gbc.xml"));
        list = supplier.list;
        list.stream().forEach(cataloguer::catalogue);    
        
        /*supplier = LogiqxXMLParser.load(Paths.get("./dats/snes.xml"));
        list = supplier.load(null).games.get();
        list.stream().forEach(cataloguer::catalogue);*/    
        
        cataloguer.printAddendums();

        
        try
        {
          CloneSet clonez = XMDBHandler.loadCloneSet(list, Paths.get("./dats/gbc.xmdb"));
          set = new GameSet(null, null, list, clonez);
        }
        catch (Exception e)
        {
          e.printStackTrace(System.err);
        }
      }
      
      
      //GameSet set = Operations.loadGameSet(options);
      //CloneSet clones = Operations.loadCloneSetFromXMDB(set, options.cloneDatPath);
      //set.setClones(clones);
      
      ScannerOptions soptions = new ScannerOptions();
      soptions.scanSubfolders = true;
      soptions.multithreaded = false;
      
      HandleSet handles = Operations.scanEntriesForGameSet(set, Arrays.asList(options.dataPath), soptions, true);

      Operations.verifyGameSet(set, handles, options);
      
      /* EXPORT ONE GAME PER CLONE WITH BIAS */
      /*{
        BiasSet bias = new BiasSet(Location.ITALY, Location.EUROPE, Location.USA, Location.JAPAN);
        Map<String, Game> exportGames = new TreeMap<>();
        for (GameClone clone : set.clones())
        {
          Game game = null;
          
          if (clone.size() == 1)
            game = clone.get(0);
          else
            game = clone.getBestMatchForBias(bias, false);
          
          if (game != null && game.isComplete())
          {
            String title = game.getTitle().substring(0, game.getTitle().indexOf("(")-1);
            exportGames.put(title, game);
          }
        }
        
        Path base = Paths.get("/Users/jack/Desktop/everdrive/gbc");
        int i = 0;
        for (Map.Entry<String, Game> entry : exportGames.entrySet())
        {
          Game game = entry.getValue();
          int size = game.getClone().size();
          
          System.out.println(i+"("+size+")"+" "+entry.getKey()+": "+game.rom().handle().fileName()+"/"+game.rom().handle().plainInternalName());
          ++i;
          
          if (game.getLocation().isJust(Location.JAPAN))
            Files.copy(game.rom().handle().getInputStream(), base.resolve("japan/"+entry.getKey()+".gb"));
          else
            Files.copy(game.rom().handle().getInputStream(), base.resolve(entry.getKey()+".gb"));
        }       
      }*/
      
            
      /*RomHandle[] compress = found.stream().limit(2).map(rh -> rh.handle).toArray(i -> new RomHandle[i]);
      
      Compressor compressor = new Compressor(options);
      
      compressor.createArchive(Paths.get("/Volumes/RAMDisk/Archive.7z"), compress);*/
      
    }
    catch (FatalErrorException e)
    {
      StackTraceElement[] stack = e.getStackTrace();
      logger.e("%s at %s.%s(...) : %d",
          e.getMessage(), 
          stack[0].getClassName(),
          stack[0].getMethodName(),
          stack[0].getLineNumber());
      e.printStackTrace();
    }
    catch (FileNotFoundException e)
    {
      logger.e("unable to find specified path: "+e.path);
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
