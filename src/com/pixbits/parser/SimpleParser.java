package com.pixbits.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

public class SimpleParser
{
  private InputStream is;
  private final StringBuilder sb;
  private Consumer<String> callback;
  private boolean quote;
  
  private final Map<Character, TokenSpec> map;
  private final TokenSpec defaultToken;
  
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
    this.quote = false;
    this.callback = callback;
    this.map = new HashMap<>();
    this.defaultToken = new TokenSpec(TokenSpec.Type.NORMAL, ' ');
  }
  
  public void reset(InputStream is)
  {
    this.is = is;
    quote = false;
    sb.setLength(0);
  }
  
  public void setCallback(Consumer<String> callback)
  {
    this.callback = callback;
  }
  
  public SimpleParser addWhiteSpace(char... chars)
  {
    for (char c : chars)
      addToken(new TokenSpec(TokenSpec.Type.WHITESPACE, c));
    return this;
  }
  
  public SimpleParser addSingle(char... chars)
  {
    for (char c : chars)
      addToken(new TokenSpec(TokenSpec.Type.SINGLE, c));
    return this;

  }
  
  public SimpleParser addQuote(char... chars)
  {
    for (char c : chars)
      addToken(new TokenSpec(TokenSpec.Type.QUOTE, c));
    return this;
  }
  
  public void addToken(TokenSpec token)
  {
    map.put(token.value, token);
  }
  
  private TokenSpec token(char value)
  {
    return map.getOrDefault(value, defaultToken);
  }
  
  private String pop()
  {
    String token = sb.toString();
    sb.setLength(0);
    return token;
  }
  
  public void parse() throws IOException
  {
    int c = -1;
    while ((c = is.read()) != -1)
    {
      TokenSpec token = token((char)c);
      
      if (token.type == TokenSpec.Type.WHITESPACE && !quote)
      {
        if (sb.length() > 0)
          callback.accept(pop());
        continue;
      }
      else if (token.type == TokenSpec.Type.QUOTE)
      {
        if (quote)
        {
          if (sb.length() > 0)
            callback.accept(pop());
          quote = false;
        }
        else
          quote = true;
      }
      else if (!quote && token.type == TokenSpec.Type.SINGLE)
      {
        if (sb.length() > 0)
          callback.accept(pop());
        
        callback.accept(Character.toString((char)c));
      }
      else
        sb.append((char)c);
    }
    
    if (sb.length() > 0)
      callback.accept(pop());
  }
}
