package com.pixbits.parser.advanced;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimpleParser
{
  private InputStream is;
  private final StringBuilder sb;
  private Consumer<String> callback;
  private final List<TokenSpec> map;
  
  public SimpleParser()
  {
    this(null, null);
  }
  
  public SimpleParser(Consumer<String> callback)
  {
    this(null, callback);
  }
  
  public SimpleParser(InputStream is)
  {
    this(is, null);
  }
  
  public SimpleParser(InputStream is, Consumer<String> callback)
  {
    this.is = is;
    this.sb = new StringBuilder();
    this.callback = callback;
    this.map = new ArrayList<>();
  }
  
  public void reset(InputStream is)
  {
    this.is = is;
    sb.setLength(0);
  }
  
  public void setCallback(Consumer<String> callback)
  {
    this.callback = callback;
  }
  
  public SimpleParser addWhiteSpace(char... chars)
  {
    for (char c : chars)
      addToken(new FixedTokenSpec(TokenSpec.Type.WHITESPACE, Character.toString(c)));
    return this;
  }
  
  public SimpleParser addSingle(char... chars)
  {
    for (char c : chars)
      addToken(new FixedTokenSpec(TokenSpec.Type.FIXED, Character.toString(c)));
    return this;
  }
  
  public SimpleParser addSingle(String... strings)
  {
    for (String s : strings)
      addToken(new FixedTokenSpec(TokenSpec.Type.FIXED, s));
    return this;
  }
  
  public SimpleParser addQuote(String svalue, String evalue)
  {
    addToken(new QuoteTokenSpec(TokenSpec.Type.QUOTE, svalue, evalue));
    return this;
  }
    
  public SimpleParser addQuote(char svalue, char evalue)
  {
    addQuote(Character.toString(svalue), Character.toString(evalue));
    return this;
  }
  
  public SimpleParser addQuote(char value)
  {
    addQuote(value, value);
    return this;
  }
  
  public SimpleParser addComment(String svalue, String evalue)
  {
    addToken(new CommentTokenSpec(svalue, evalue));
    return this;
  }
  
  private void addToken(TokenSpec token)
  {
    map.add(token);
  }
  
  /*private TokenSpec token(char value)
  {
    return map.getOrDefault(value, defaultToken);
  }*/
  
  private String pop()
  {
    String token = sb.toString();
    sb.setLength(0);
    return token;
  }
  
  private void produce(TokenSpec spec)
  {
    if (sb.length() > 0)
    {
      String token = spec.transform(pop());
      if (token != null)
        callback.accept(token);
    }
  }
  
  private void clearStack()
  {
    sb.setLength(0);
  }
  
  private void push(int c)
  {
    sb.append((char)c);
  }
  
  public void parse() throws IOException
  {
    int c = -1;
    while ((c = is.read()) != -1)
    {
      if (sb.length() > 0)
      {
        //System.out.printf("Stack: /%s/\n", sb.toString());
        
        List<TokenSpec> partialMatches = map.stream().filter(t -> t.partialMatch(sb.toString())).collect(Collectors.toList());
        List<TokenSpec> totalMatches = map.stream().filter(t -> t.match(sb.toString())).collect(Collectors.toList());
        
        if (partialMatches.isEmpty())
          throw new RuntimeException("Unrecognized lexer token: "+sb.toString());
        
        if (!totalMatches.isEmpty())
        {
          TokenSpec spec = totalMatches.get(0);
          
          //System.out.println("Matched: "+spec.type);
          
          produce(spec);
          clearStack();
        }
      }
      
      push(c);
    } 
  }
}
