package com.pixbits.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;

import javax.swing.JTextField;

public class JPlaceHolderTextField extends JTextField
{
	private static final long serialVersionUID = 1L;
	
	private String placeholder;
	
	public JPlaceHolderTextField(int s, String placeholder)
	{
		super(s);
		this.placeholder = placeholder;
	}
	
	public void setPlaceholder(String str)
	{
	  this.placeholder = str;
	}
	
  @Override
  protected void paintComponent(java.awt.Graphics g) {
      super.paintComponent(g);

      if(getText().isEmpty() && ! (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)){
          Graphics2D g2 = (Graphics2D)g.create();
          g2.setColor(Color.GRAY);
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
          
          FontMetrics fm = g2.getFontMetrics();
          
          g2.drawString(placeholder, 10, this.getSize().height/2+fm.getAscent()/2); //figure out x, y from font's FontMetrics and size of component.
          g2.dispose();
      }
    }

}
