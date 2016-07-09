package com.jack.nit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.GameSetStatus;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.merger.Compressor;
import com.jack.nit.merger.Merger;
import com.jack.nit.scanner.Renamer;
import com.jack.nit.scanner.RomHandle;
import com.jack.nit.scanner.RomHandlesSet;
import com.jack.nit.scanner.Scanner;
import com.jack.nit.scanner.Options;
import com.jack.nit.scanner.Verifier;

import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

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

  public static void main( String[] args )
  {
    Options options = new Options();
    
    try
    {
      initializeSevenZip();
      
      GameSet set = Operations.loadGameSet(options);
      CloneSet clones = Operations.loadCloneSetFromXMDB(set, options.cloneDatPath) ;

      
      Scanner scanner = new Scanner(set, options);
      RomHandlesSet handles = scanner.computeHandles();
      
      Verifier verifier = new Verifier(options, set);
      
      List<RomFoundReference> found = verifier.verify(handles);
      
      Logger.log(Log.INFO1, "Found %d verified roms", found.size());
      found.forEach(r -> Logger.log(Log.INFO3, "> %s", r.rom.game.name));
      
      Renamer renamer = new Renamer(options);
      renamer.rename(found);
      
      GameSetStatus status = new GameSetStatus(set, clones, found.toArray(new RomFoundReference[found.size()]));
      
      Merger merger = new Merger(status, options);
      merger.merge(options.mergePath);
      
      /*RomHandle[] compress = found.stream().limit(2).map(rh -> rh.handle).toArray(i -> new RomHandle[i]);
      
      Compressor compressor = new Compressor(options);
      
      compressor.createArchive(Paths.get("/Volumes/RAMDisk/Archive.7z"), compress);*/
      
    }
    catch (SevenZipNativeInitializationException e)
    {
      Logger.log(Log.ERROR, "Failed to initialize SevenZip library to manage archives, exiting:\n\t"+e.getMessage());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
