package com.github.jakz.nit.batch;

import java.nio.file.Path;
import java.util.Map;

import com.github.jakz.romlib.data.set.GameSet;

@FunctionalInterface
public interface SetToPathMapper
{
  Path getRompath(GameSet set);
  
  public static SetToPathMapper of(final Map<String, Path> nameToPathMap)
  {
    return set -> nameToPathMap.getOrDefault(set.info().getName(), null);
  }
}
