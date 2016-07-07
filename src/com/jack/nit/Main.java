package com.jack.nit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.header.Header;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.parser.DatParser;
import com.jack.nit.parser.HeaderParser;
import com.jack.nit.parser.XMDBParser;
import com.jack.nit.scanner.RomHandlesSet;
import com.jack.nit.scanner.Scanner;
import com.jack.nit.scanner.ScannerOptions;
import com.jack.nit.scanner.Verifier;
import com.pixbits.io.XMLParser;

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
    Path path = Settings.DATS_PATH.resolve("gba.dat");
    
    try
    {
      initializeSevenZip();
      
      GameSet set = Operations.loadGameSet(path);
      CloneSet clones = Operations.loadCloneSetFromXMDB(set, Settings.DATS_PATH.resolve("gba.xmdb")) ;

      ScannerOptions options = new ScannerOptions();
      
      Scanner scanner = new Scanner(set, options);
      RomHandlesSet handles = scanner.computeHandles();
      
      Verifier verifier = new Verifier(options, set);
      
      List<RomFoundReference> found = verifier.verify(handles);
      
      Logger.log(Log.INFO1, "Found %d verified roms", found.size());
      found.forEach(r -> Logger.log(Log.INFO3, "> %s", r.rom.game.name));
      
    }
    catch (SevenZipNativeInitializationException e)
    {
      Logger.log(Log.ERROR, "Failed to initialize SevenZip library to manage archives, exiting.");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
