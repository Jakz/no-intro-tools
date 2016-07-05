package com.jack.nit.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashCache
{
  private final Map<Long, RomReference> cache;
  private final Set<Long> sizes;
  
  HashCache()
  {
    cache = new HashMap<>();
    sizes = new HashSet<>();
  }
  
  public HashCache(GameSet set)
  {
    cache = new HashMap<>(set.realSize());
    sizes = new HashSet<>();
    precompute(set);
  }
  
  public void precompute(GameSet set)
  {
    cache.clear();
    sizes.clear();
    
    set.stream().forEach(g -> {
      g.stream().forEach(r -> {
        cache.put(r.crc32, new RomReference(g,r));
        sizes.add(r.size);
      });
    });
  }
  
  public boolean isValidSize(long size) { return sizes.contains(size); }
  public RomReference romForCrc(long crc) { return cache.get(crc); }
}
