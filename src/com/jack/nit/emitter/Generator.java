package com.jack.nit.emitter;

import java.io.IOException;
import java.nio.file.Path;

import com.jack.nit.data.GameSet;

public interface Generator
{
  public void generate(CreatorOptions options, GameSet set) throws IOException;
}
