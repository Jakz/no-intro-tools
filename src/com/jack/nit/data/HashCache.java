package com.jack.nit.data;

import java.util.HashMap;
import java.util.Map;

public class HashCache
{
  private final Map<Long, RomReference> cache;
  
  HashCache()
  {
    cache = new HashMap<>();
  }
  
  HashCache(GameSet set)
  {
    cache = new HashMap<>(set.realSize());
    precompute(set);
  }
  
  void precompute(GameSet set)
  {
    set.stream().forEach(g -> {
      g.stream().forEach(r -> {
        cache.put(r.crc32, new RomReference(g,r));
      });
    });
  }
}
