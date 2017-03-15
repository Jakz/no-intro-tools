package com.github.jakz.nit.scripts;

import java.util.function.Predicate;

import com.github.jakz.nit.data.Game;

public class SelectStatement implements Statement
{
  SelectStatement(Predicate<Game> query) { }
  
  public void execute(ScriptEnvironment env)
  {
    
  }
}