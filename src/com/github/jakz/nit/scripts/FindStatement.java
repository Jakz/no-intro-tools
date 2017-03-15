package com.github.jakz.nit.scripts;

import java.util.function.Predicate;

import com.github.jakz.nit.data.Game;

public class FindStatement implements Statement
{
  FindStatement(Predicate<Game> query) { }
  
  public void execute(ScriptEnvironment env)
  {

  }
}