package com.pixbits.parser.advanced;

public class QuoteTokenSpec extends TokenSpec
{
  String svalue;
  String evalue;
  
  QuoteTokenSpec(TokenSpec.Type type, String svalue, String evalue)
  {
    super(type);
    this.svalue = svalue;
    this.evalue = evalue;
  }
    
  public boolean match(String value) { return value.length() >= svalue.length()+evalue.length() && value.startsWith(svalue) && value.endsWith(evalue); }
  public boolean partialMatch(String value) { return value.length() >= 1 && svalue.startsWith(value.substring(0, Math.min(svalue.length(),value.length()-1))); }
  
  @Override
  public String transform(String token)
  {
    return token.substring(svalue.length(), token.length()-evalue.length());
  }

}
