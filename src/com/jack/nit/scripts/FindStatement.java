package com.jack.nit.scripts;

import java.util.function.Predicate;
import com.jack.nit.data.Game;

public class FindStatement implements Statement
{
  private final Predicate<Game> query;
  FindStatement(Predicate<Game> query) { this.query = query; }
  
  public void execute(ScriptEnvironment env)
  {

  }
}