package com.github.jakz.nit.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.jakz.nit.Options;
import com.github.jakz.nit.data.Rom;
import com.github.jakz.nit.handles.Handle;
import com.pixbits.lib.functional.StreamException;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;

public class Renamer
{
  private static final Logger logger = Log.getLogger(Renamer.class);
  
  private Function<Rom,String> renamer;
  private final Options options;
  
  public Renamer(Options options, Function<Rom,String> renamer)
  {
    this.renamer = renamer;
    this.options = options;
  }
  
  
  public Renamer(Options options)
  {
    this(options, rr -> rr.game().name);
  }
  
  
  public void rename(List<Rom> files) throws IOException
  {
    Stream<Rom> stream = files.stream();
    
    Map<Path, Set<Rom>> mappedFiles = new HashMap<>();
    
    /* build a map of roms contained in each file */
    files.stream().forEach(rh -> mappedFiles.compute(rh.handle().file(), (r, v) -> {
      if (v == null)
        v = new HashSet<Rom>();
      
      v.add(rh);
      
      return v;
    }));
    
    if (false && options.multiThreaded)
      stream = stream.parallel(); //FIXME: this will create problems caused by Files.move which will make parallel tasks fail on relocate of path
    
    stream.forEach(StreamException.rethrowConsumer(rom -> {
      Handle handle = rom.handle();
      
      /* if rom is stored inside an archive together with other files */
      if (handle.isArchive() && mappedFiles.get(handle.file()).size() > 1)
      {
        logger.w("Skipping rename of "+rom.game().name+" because it's archived together with other verified files");
        return;
      }
        
      Path finalName = Paths.get(renamer.apply(rom)+"."+handle.getExtension());
      Path currentName = handle.file().getFileName();

      if (!finalName.equals(currentName))
      {
        Path finalPath = handle.file().getParent().resolve(finalName);
        boolean sameNameDifferentCase = handle.file().getFileName().toString().compareToIgnoreCase(finalPath.getFileName().toString()) == 0;

        /* if name is the same but with different capitalization then first move it to temporary name to avoid problems on case insensitive file-systems */
        if (sameNameDifferentCase)
        {
          Path temp = Files.createTempFile(finalPath.getParent(), "", "." + handle.getExtension());
          Files.move(handle.file(), temp, StandardCopyOption.REPLACE_EXISTING);
          handle.relocate(temp);
        }
        /* if file exists then it could be another found game but with a wrong name, in that case we shall rename the destination to something else before renaming */
        else if (Files.exists(finalPath))
        {
          if (mappedFiles.containsKey(finalPath))
          {
            logger.d("Renaming found reference for %s to a temporary name to avoid name clashing with another found reference which should have its name.", finalName.toString());
            Path temporaryFile = Files.createTempFile(finalPath.getParent(), "", "."+handle.getExtension());
            Files.move(finalPath, temporaryFile, StandardCopyOption.REPLACE_EXISTING);
            Set<Rom> references = mappedFiles.get(finalPath);
            references.forEach(rrr -> rrr.handle().relocate(temporaryFile));
            mappedFiles.remove(finalPath);
            mappedFiles.put(temporaryFile, references);
          }
          else
          {
            //TODO: add setting to enable overwrite or stop renaming
            Files.delete(finalPath);
          }
        }

        logger.d("Renaming %s to %s.", handle.file().getFileName(), finalPath.getFileName());
        Files.move(handle.file(), finalPath);
        handle.relocate(finalPath);
      }
    }));
    
  }
  
}
