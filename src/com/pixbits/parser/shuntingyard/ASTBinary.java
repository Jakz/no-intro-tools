package com.pixbits.parser.shuntingyard;

public class ASTBinary implements ASTNode
{
  public final ASTNode left, right;
  public final Operator operator;
  
  ASTBinary(Operator operator, ASTNode left, ASTNode right)
  {
    this.operator = operator;
    this.left = left;
    this.right = right;
  }
  
  @Override public <T> T accept(Visitor<T> visitor)
  {
    visitor.enterNode(this);
    left.accept(visitor);
    right.accept(visitor);
    T value = visitor.doVisitNode(this);
    visitor.exitNode(this);
    return value;
  }
  
  public String toString() { return "ASTBinary("+operator.mnemonic+")"; }

}
