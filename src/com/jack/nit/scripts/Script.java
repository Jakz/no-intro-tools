package com.jack.nit.scripts;

import java.util.List;

public class Script
{
  private final List<Statement> statements;
  Script(List<Statement> statements) { this.statements = statements; }
  public int length() { return statements.size(); }
  
  public void execute(ScriptEnvironment env)
  {
    statements.forEach(s -> s.execute(env));
  }
}