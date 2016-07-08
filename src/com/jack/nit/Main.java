package com.jack.nit;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.header.Header;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.jack.nit.merger.Compressor7Zip;
import com.jack.nit.parser.DatParser;
import com.jack.nit.parser.HeaderParser;
import com.jack.nit.parser.XMDBParser;
import com.jack.nit.scanner.Renamer;
import com.jack.nit.scanner.RomHandle;
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
    /*Set<Point> pts = new HashSet<Point>();
    Stream<Point> stream = RandomPointSpliterator.stream();
    Stream<Point> truncated = stream.limit(50);
    truncated = truncated.distinct();
    System.out.println("trying");
    truncated.forEach(pts::add);
    System.out.println("Size should match 50 == "+pts.size());*/
    
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
      
      Renamer renamer = new Renamer(options);
      renamer.rename(found);
      
      RomHandle[] compress = found.stream().limit(5).map(rh -> rh.handle).toArray(i -> new RomHandle[i]);
      Compressor7Zip.createArchive(Paths.get("/Volumes/RAMDisk/Archive.7z"), compress, 5, true);
      
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
