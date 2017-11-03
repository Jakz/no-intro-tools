package com.github.jakz.nit.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.io.archive.ArchiveFormat;

@FunctionalInterface
public interface SetToPathMapper
{
  Path getRompath(GameSet set);
  
  public static SetToPathMapper of(final Map<String, Path> nameToPathMap)
  {
    return set -> nameToPathMap.getOrDefault(set.info().getName(), null);
  }
  
  public static SetToPathMapper ofDefaultNamingInFolder(Path folder)
  {
    return set -> {
      String expectedName = set.info().getName();
      
      Path asFolder = folder.resolve(expectedName);
      
      if (Files.exists(asFolder) && Files.isDirectory(asFolder))
        return asFolder;
      else
      {
        for (ArchiveFormat format : ArchiveFormat.readableFormats)
        {
          Path asArchive = folder.resolve(expectedName + format.dottedExtension());
          
          if (Files.exists(asArchive) && !Files.isDirectory(asArchive))
            return asArchive;
        }
        
        return null;
      }
    };
  }
}
