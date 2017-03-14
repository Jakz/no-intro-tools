package com.github.jakz.nit.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pixbits.lib.functional.StreamException;

public class HashCache
{
  private final Map<Long, Rom> cache;
  private final Set<Long> sizes;
  
  HashCache()
  {
    cache = new HashMap<>();
    sizes = new HashSet<>();
  }
  
  public HashCache(GameSet set)
  {
    cache = new HashMap<>(set.filesCount());
    sizes = new HashSet<>();
    precompute(set);
  }
  
  public void precompute(GameSet set)
  {
    cache.clear();
    sizes.clear();
    
    set.stream().forEach(StreamException.rethrowConsumer(g -> {
      g.stream().forEach(StreamException.rethrowConsumer(r -> {
        /*if (cache.containsKey(r.crc32))
          throw new RuntimeException("Duplicate CRC found!");*/
        
        cache.put(r.crc32, r);
        sizes.add(r.size);
      }));
    }));
  }
  
  public boolean isValidSize(long size) { return sizes.contains(size); }
  public Rom romForCrc(long crc) { return cache.get(crc); }
}
