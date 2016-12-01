package com.pixbits.lib.parser.shuntingyard;

@FunctionalInterface
public interface ASTNode
{
  public <T> T accept(Visitor<T> visitor);
}
