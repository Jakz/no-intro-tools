package com.github.jakz.nit.handles;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class NestedArchiveCache
{
  Map<Path, Map<Integer, MemoryArchive>> archives;
  
  NestedArchiveCache()
  {
    archives = new HashMap<>();
  }
  
  MemoryArchive get(Path path, int index)
  {
    Map<Integer, MemoryArchive> cachedForArchive = archives.get(path);
    return cachedForArchive.getOrDefault(index, null);
  }
  
  void cacheArchive(Path path, int index, MemoryArchive archive)
  {
    archives.computeIfAbsent(path, p -> new HashMap<>()).put(index, archive);
  }
  
  void freeArchive(Path path)
  {
    archives.remove(path);
  }
}
