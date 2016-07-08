package com.jack.nit.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.RomReference;
import com.pixbits.stream.StreamException;

public class Renamer
{
  private Function<RomReference,String> renamer;
  private final ScannerOptions options;
  
  public Renamer(ScannerOptions options, Function<RomReference,String> renamer)
  {
    this.renamer = renamer;
    this.options = options;
  }
  
  
  public Renamer(ScannerOptions options)
  {
    this(options, rr -> rr.game.name);
  }
  
  
  public void rename(List<RomFoundReference> files) throws IOException
  {
    Stream<RomFoundReference> stream = files.stream();
    
    if (options.multiThreaded)
      stream = stream.parallel();
    
    stream.forEach(StreamException.rethrowConsumer(rr -> {
      Path finalName = Paths.get(renamer.apply(rr.rom)+"."+rr.handle.getExtension());
      Path currentName = rr.handle.file().getFileName();
      
      if (!finalName.equals(currentName))
      {
        Path finalPath = rr.handle.file().getParent().resolve(finalName);

        Files.move(rr.handle.file(), finalPath);
      }
    }));
    
  }
  
}
