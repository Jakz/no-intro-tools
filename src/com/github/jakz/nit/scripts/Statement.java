package com.github.jakz.nit.scripts;

@FunctionalInterface
public interface Statement
{
  public void execute(ScriptEnvironment env);
}