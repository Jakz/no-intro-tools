package com.pixbits.parser.shuntingyard;

public class Operator implements Comparable<Operator>
{
  final String mnemonic;
  final int precedence;
  final boolean rightAssociative;
  final boolean unary;
  
  public Operator(String mnemonic, int precedence, boolean unary, boolean rightAssociative)
  {
    this.mnemonic = mnemonic;
    this.precedence = precedence;
    this.rightAssociative = rightAssociative;
    this.unary = unary;
  }
  
  @Override 
  public int compareTo(Operator other) { return precedence - other.precedence; } 
  boolean isRightAssociative() { return rightAssociative; }
  boolean isUnary() { return unary; }
  
  public String getMnemonic() { return mnemonic; }
}
