package com.pixbits.parser.shuntingyard;

import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import com.pixbits.parser.ParseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class ShuntingYardParser
{
  private final Map<String,Operator> operators;
  private final Set<Character> quotes;
  private final Set<Character> whitespace;
  
  private final Stack<StackOperator> operatorStack;
  private final Stack<ASTNode> nodesStack;
  
  private Consumer<String> logger = s -> { };
  
  private class StackOperator
  {
    private Operator operator;
    private boolean isClosingParen;
    
    StackOperator(Operator operator) { this.operator = operator; }
    StackOperator(boolean isClosing) { this.isClosingParen = isClosing; }
    
    boolean isOpenParen() { return operator == null && !isClosingParen; }
    
    Operator getOperator() { return operator; }
    
    public String toString()
    {
      if (operator != null) { return "StackOperator("+operator.mnemonic+")"; }
      else if (isClosingParen) { return "StackOperator(')')"; }
      else return "StackOperator('(')";
    }
  }
  
  public ShuntingYardParser(Operator... operators)
  {
    this.operators = new HashMap<>();
    Arrays.stream(operators).forEach(o -> this.operators.put(o.mnemonic, o));
    
    operatorStack = new Stack<>();
    nodesStack = new Stack<>();
    
    quotes = new HashSet<>();
    quotes.add('\"');
    
    whitespace = new HashSet<>();
    whitespace.add(' ');
    whitespace.add('\t');
  }
  
  private void emitValue(String value)
  {
    pushNode(new ASTValue(value));
  }
  
  private void pushNode(ASTNode node)
  {
    nodesStack.push(node);
    logger.accept("Pushed "+node+" on stack");
  }
  
  private void pushOperator(StackOperator op)
  {
    operatorStack.push(op);
    logger.accept("Pushed "+op);
  }
  
  private void emitOperator(Operator o1) throws ParseException
  {
    Operator o2;

    while (!operatorStack.isEmpty() && (o2 = operatorStack.peek().operator) != null)
    {
      if( (!o1.isRightAssociative() && o1.compareTo(o2) >= 0) || o1.compareTo(o2) > 0)
      {
        operatorStack.pop();   
        forceEmitOperator(o2);   
      } 
      else
        break;
    }

    pushOperator(new StackOperator(o1));
  }
  
  private void forceEmitOperator(Operator op) throws ParseException
  {
    if (op.isUnary())
      pushNode(new ASTUnary(op, popNode()));
    else
    {
      ASTNode n2 = popNode(), n1 = popNode();
      pushNode(new ASTBinary(op, n1, n2));
    }   
  }
  
  private void tryToEmitOperator(StringBuilder buffer) throws ParseException
  {
    String string = buffer.toString();
    for (Operator operator : operators.values())
    {
      if (string.endsWith(operator.mnemonic))
      {
        if (buffer.length() > operator.mnemonic.length())
        {
          int valueLength = buffer.length() - operator.mnemonic.length();
          emitValue(buffer.substring(0, valueLength));
          buffer.delete(0, valueLength);
        }
        
        emitOperator(operator);
        buffer.delete(0, buffer.length());

        return;
      }
    }
  }
  
  private void tryToEmitValue(StringBuilder buffer)
  {
    if (buffer.length() > 0)
    {
      emitValue(buffer.toString());
      buffer.delete(0, buffer.length());
    }
  }
  
  private void tryToEmitEverything(StringBuilder buffer) throws ParseException
  {
    tryToEmitOperator(buffer);
    tryToEmitValue(buffer);
  }
  
  private ASTNode popNode() throws ParseException
  {
    if (nodesStack.empty())
      throw new ParseException("expecting node on nodes stack");
    
    ASTNode node = nodesStack.pop();
    return node;
  }
    
  public ASTNode parse(String string) throws ParseException
  {
    nodesStack.clear();
    operatorStack.clear();
    
    boolean insideQuote = false;
    char quote = 0;
    
    StringBuilder buffer = new StringBuilder();
    
    int index = 0;
    int length = string.length();
    
    while (index < length)
    {      
      char c = string.charAt(index++);
      
      if (insideQuote && quote != c)
        buffer.append(c);
      else if (quotes.contains(c))
      {
        if (!insideQuote)
        {
          insideQuote = true;
          quote = c;
        }
        else
          insideQuote = false;
        
        buffer.append(c);
      }
      else if (c == '(')
      {
        tryToEmitEverything(buffer);
        pushOperator(new StackOperator(false));
      }
      else if (c == ')')
      {
        tryToEmitEverything(buffer);
        
        boolean matchFound = false;
        
        while (!operatorStack.isEmpty())
        {
          StackOperator sop = operatorStack.pop();
          
          if (sop.isOpenParen())
          {
            matchFound = true;
            break;
          }
          else
          {
            Operator op = sop.getOperator();
            forceEmitOperator(op);               
          }   
        }
        
        if (operatorStack.isEmpty() && !matchFound)
          throw new ParseException("unbalanced parenthesis");
      }
      
      else if (whitespace.contains(c))
        continue;
      else
        buffer.append(c);
      
      if (!insideQuote)
        tryToEmitOperator(buffer);
    }
    
    tryToEmitEverything(buffer);
    
    while (!operatorStack.empty())
    {
      StackOperator op = operatorStack.pop();
      if (op.operator == null)
        throw new ParseException("unbalanced parenthesis");
      else
        forceEmitOperator(op.operator);
    }
    
    if (insideQuote)
      throw new ParseException("unclosed quote");
    
    return nodesStack.pop();
  }
  
  public static void main(String[] args)
  {
    ShuntingYardParser parser = new ShuntingYardParser(
        new Operator("&&", 1, false, false),
        new Operator("||", 2, false, false),
        new Operator("!", 0, true, false)
    );
        
    try
    {
      parser.parse("(loc:us && !language:\"aa  english\") || loc:ita");
      PrintVisitor visitor = new PrintVisitor();
      parser.nodesStack.peek().accept(visitor);
    }
    catch (ParseException e)
    {
      e.printStackTrace();
    }
  }
}
