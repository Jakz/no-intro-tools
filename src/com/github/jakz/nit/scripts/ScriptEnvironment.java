package com.github.jakz.nit.scripts;

import com.github.jakz.nit.data.GameSet;

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
