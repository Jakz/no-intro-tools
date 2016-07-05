package com.pixbits.parser.advanced;

public class CommentTokenSpec extends QuoteTokenSpec
{
  CommentTokenSpec(String svalue, String evalue)
  {
    super(TokenSpec.Type.QUOTE, svalue, evalue);
  }
  
  @Override
  public String transform(String token)
  {
    return null;
  }
}
