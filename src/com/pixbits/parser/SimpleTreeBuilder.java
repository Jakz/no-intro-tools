package com.pixbits.parser;

import java.util.Stack;
import java.util.function.BiConsumer;

public class SimpleTreeBuilder
{
  private final SimpleParser parser;
  private String scope[] = new String[2];
  private Stack<String> tokens;
  private boolean partial;
  
  private BiConsumer<String, String> pairCallback;
  private BiConsumer<String, Boolean> scopeCallback;

  public SimpleTreeBuilder(SimpleParser parser, BiConsumer<String, String> pairCallback, BiConsumer<String, Boolean> scopeCallback)
  {
    this.parser = parser;
    this.parser.setCallback(this::token);
    this.tokens = new Stack<>();
    this.partial = false;
    this.pairCallback = pairCallback;
    this.scopeCallback = scopeCallback;
  }
  
  public void setScope(String... chars)
  {
    scope[0] = chars[0];
    scope[1] = chars[1];
  }
  
  private boolean isStartScope(String token) { return token.equals(scope[0]); }
  private boolean isEndScope(String token) { return token.equals(scope[1]); }
  private boolean isScope(String token) { return isStartScope(token) || isEndScope(token); }
  
  private void token(String token)
  {
    boolean wasPartial = partial;
    
    if (!partial)
      partial = true;
    
    if (!isScope(token))
      tokens.push(token);
    
    if (isStartScope(token))
    {
      partial = false;
      scopeCallback.accept(tokens.peek(), false);
    }
    else if (isEndScope(token))
    {
      partial = false;
      scopeCallback.accept(tokens.pop(), true);
    }
    else
    {
      if (wasPartial)
      {
        String value = tokens.pop();
        String key = tokens.pop();
        pairCallback.accept(key, value);
        partial = false;
      }
    }
    
    
  }
}
