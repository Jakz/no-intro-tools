package com.pixbits.parser.shuntingyard;

@FunctionalInterface
public interface ASTNode
{
  public <T> T accept(Visitor<T> visitor);
}
