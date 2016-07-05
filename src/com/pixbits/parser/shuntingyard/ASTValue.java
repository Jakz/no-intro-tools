package com.pixbits.parser.shuntingyard;

public class ASTValue implements ASTNode
{
  public final String value;
  
  ASTValue(String value)
  {
    this.value = value;
  }
  
  @Override public <T> T accept(Visitor<T> visitor)
  {
    visitor.enterNode(this);
    T value = visitor.doVisitNode(this);
    visitor.exitNode(this); 
    return value;
  }
  
  public String toString() { return "ASTValue("+value+")"; }
}
