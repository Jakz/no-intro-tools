package com.jack.nit.scripts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;

public class ConsolePanel extends JPanel implements KeyListener, ScriptStdout
{
  private final JTextArea console;
  private final JScrollPane pane;
  private int startCommandPosition;
  
  private Parser<Script> parser;
  
  public ConsolePanel()
  {
    console = new JTextArea();
    pane = new JScrollPane(console);
    pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    console.setFont(new Font("Monaco", Font.BOLD, 12));
    console.setBackground(Color.BLACK);
    console.setForeground(new Color(177,242,27));
    
    this.setLayout(new BorderLayout());
    this.add(pane, BorderLayout.CENTER);
    
    parser = new ScriptParser().script();
    
    pane.setPreferredSize(new Dimension(1024,768));
    
    console.addKeyListener(this);
        
    appendPrompt();
  }
  
  public void appendPrompt()
  {
    /*if (console.getText().length() != 0 && console.getText().charAt(console.getText().length()-1) != '\n');
      console.append("\n");*/
    
    console.append("> ");
    console.setCaretPosition(console.getText().length());
    startCommandPosition = console.getText().length();
  }
  
  public void syntaxError(String message)
  {
    console.append("Syntax error: "+message.replaceAll("\n", " ")+"\n");
  }
  
  public void keyPressed(KeyEvent k)
  {

  }
  
  public void setMySize(int x, int y)
  {

  }
  
  public void keyReleased(KeyEvent k)
  {
    if (k.getKeyChar() == KeyEvent.VK_ENTER)
    {
      try
      {
        String command = console.getText(startCommandPosition, console.getText().length() - startCommandPosition - 1);
        System.out.println("Executing \'"+command+"\'");
        
        Script script = parser.parse(command);
        script.execute(new ScriptEnvironment(null, this));
      }
      catch (ParserException e)
      {
        //ParseErrorDetails details = e.getErrorDetails();
        syntaxError(e.getMessage());

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      finally
      {
        appendPrompt();
      }
    }
  }
  
  public void keyTyped(KeyEvent k)
  {
    
  }
  
  public void append(String text)
  {
    console.append(text+"\n");
  }
}
