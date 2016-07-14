package com.jack.nit.scripts;

@FunctionalInterface
public interface Statement
{
  public void execute(ScriptEnvironment env);
}