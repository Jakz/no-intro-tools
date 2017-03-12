package com.jack.nit.renamer;

@FunctionalInterface
public interface RenamerRule
{
  String apply(String name);
  
  public static final RenamerRule TRIM = name -> name.trim();
  public static final RenamerRule REMOVE_MULTIPLE_WHITESPACE = name -> name.replaceAll(" +", " ");
 
}
