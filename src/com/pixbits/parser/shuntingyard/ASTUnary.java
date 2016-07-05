package com.pixbits.parser.shuntingyard;

public class ASTUnary implements ASTNode
{
  public final ASTNode inner;
  public final Operator operator;
  
  ASTUnary(Operator operator, ASTNode inner)
  {
    this.operator = operator;
    this.inner = inner;
  }
  
  @Override public <T> T accept(Visitor<T> visitor)
  {
    visitor.enterNode(this);
    inner.accept(visitor);
    T value = visitor.doVisitNode(this);
    visitor.exitNode(this);
    return value;
  }
  
  public String toString() { return "ASTUnary("+operator.mnemonic+")"; }

}
