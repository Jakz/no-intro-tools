package com.pixbits.parser.shuntingyard;

import java.util.Stack;

public abstract class StackVisitor<T> implements Visitor<T>
{
  final protected Stack<T> values = new Stack<>();

  public T doVisitNode(ASTNode node)
  {
    T value = visitNode(node);
    push(value);
    return value;
  }
  
  public void push(T value) { values.push(value); }
  protected T pop() { return values.pop(); }
  
  public void reset() { values.clear(); }
}
