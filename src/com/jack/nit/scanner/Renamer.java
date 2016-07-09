package com.jack.nit.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jack.nit.data.RomFoundReference;
import com.jack.nit.data.RomReference;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.pixbits.stream.StreamException;

public class Renamer
{
  private Function<RomReference,String> renamer;
  private final Options options;
  
  public Renamer(Options options, Function<RomReference,String> renamer)
  {
    this.renamer = renamer;
    this.options = options;
  }
  
  
  public Renamer(Options options)
  {
    this(options, rr -> rr.game.name);
  }
  
  
  public void rename(List<RomFoundReference> files) throws IOException
  {
    Stream<RomFoundReference> stream = files.stream();
    
    Map<Path, Set<RomFoundReference>> mappedFiles = new HashMap<>();
    
    files.stream().forEach(rh -> mappedFiles.compute(rh.handle.file(), (r, v) -> {
      if (v == null)
        v = new HashSet<RomFoundReference>();
      
      v.add(rh);
      
      return v;
    }));
    
    if (options.multiThreaded)
      stream = stream.parallel();
    
    stream.forEach(StreamException.rethrowConsumer(rr -> {
      if (rr.handle.isArchive() && mappedFiles.get(rr.handle.file()).size() > 1)
      {
        Logger.log(Log.WARNING, "Skipping rename of "+rr.rom.game.name+" because it's archived together with other verified files");
        return;
      }
        
      Path finalName = Paths.get(renamer.apply(rr.rom)+"."+rr.handle.getExtension());
      Path currentName = rr.handle.file().getFileName();
      
      if (!finalName.equals(currentName))
      {
        Path finalPath = rr.handle.file().getParent().resolve(finalName);

        Files.move(rr.handle.file(), finalPath);
        rr.handle.relocate(finalPath);
      }
    }));
    
  }
  
}
