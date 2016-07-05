package com.jack.nit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jack.nit.data.GameSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.parser.DatParser;

/**
 * Hello world!
 *
 */
public class Main 
{
  public static void main( String[] args )
  {
    Path path = Settings.DATS_PATH.resolve("nes.dat");
    DatParser parser = new DatParser();
    
    try
    {
      GameSet set = parser.load(path);
      
      Logger.log(Log.INFO, "Loaded set \'"+set.name+"\' ("+set.size()+" games, "+set.realSize()+" roms)");
      
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
