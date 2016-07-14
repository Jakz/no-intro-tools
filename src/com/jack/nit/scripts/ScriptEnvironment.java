package com.jack.nit.scripts;

import com.jack.nit.data.GameSet;

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
