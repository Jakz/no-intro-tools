package com.github.jakz.nit.emitter;

import java.io.IOException;

import com.github.jakz.nit.data.GameSet;

public interface Emitter
{
  public void generate(CreatorOptions options, GameSet set) throws IOException;
}
