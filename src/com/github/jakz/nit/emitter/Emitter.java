package com.github.jakz.nit.emitter;

import java.io.IOException;

import com.github.jakz.romlib.data.set.GameSet;

public interface Emitter
{
  public void generate(CreatorOptions options, GameSet set) throws IOException;
}
