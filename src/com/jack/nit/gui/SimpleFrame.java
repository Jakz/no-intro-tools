package com.jack.nit.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimpleFrame<T extends JPanel> extends JFrame
{
  protected final T content;
  
  public SimpleFrame(String title, T content, boolean exitOnClose)
  {
    this.content = content;
    
    getContentPane().add(content);
    setTitle(title);
    
    pack();   
    
    if (exitOnClose)
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
}
