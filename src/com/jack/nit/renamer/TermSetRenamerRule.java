package com.jack.nit.renamer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class TermSetRenamerRule implements RenamerRule
{
  private final Set<String> words;
  
  TermSetRenamerRule(String... words)
  {
    this.words = new HashSet<String>(Arrays.asList(words));
  }

  @Override
  public String apply(String name) {
    // TODO Auto- method stub
    return null;
  }
}
