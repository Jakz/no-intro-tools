package com.jack.nit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.parser.DatParser;
import com.jack.nit.scanner.RomHandlesSet;
import com.jack.nit.scanner.Scanner;
import com.jack.nit.scanner.ScannerOptions;
import com.jack.nit.scanner.Verifier;

/**
 * Hello world!
 *
 */
public class Main 
{
  public static void main( String[] args )
  {
    Path path = Settings.DATS_PATH.resolve("gba.dat");
    DatParser parser = new DatParser();
    
    try
    {
      GameSet set = parser.load(path);
      
      Logger.log(Log.INFO1, "Loaded set \'"+set.name+"\' ("+set.size()+" games, "+set.realSize()+" roms)");
      
      ScannerOptions options = new ScannerOptions();
      
      Scanner scanner = new Scanner(set, options);
      RomHandlesSet handles = scanner.computeHandles();
      
      Verifier verifier = new Verifier(options, set);
      
      List<RomFoundReference> found = verifier.verify(handles);
      
      Logger.log(Log.INFO1, "Found %d verified roms", found.size());
      found.forEach(r -> Logger.log(Log.INFO3, "> %s", r.rom.game.name));
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
