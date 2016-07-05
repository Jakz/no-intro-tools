package com.pixbits.parser.shuntingyard;

public interface Visitor<T>
{
  default T doVisitNode(ASTNode node) { return visitNode(node); }
  
  default void enterNode(ASTNode node) { }
  default void exitNode(ASTNode node) { }
  T visitNode(ASTNode node);
}