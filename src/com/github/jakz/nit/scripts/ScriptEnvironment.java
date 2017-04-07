package com.github.jakz.nit.scripts;

import com.github.jakz.romlib.data.set.GameSet;

public class ScriptEnvironment
{
  final ScriptStdout out;
  final GameSet set;
  
  public ScriptEnvironment(GameSet set, ScriptStdout out)
  {
    this.out = out;
    this.set = set;
  }
}
