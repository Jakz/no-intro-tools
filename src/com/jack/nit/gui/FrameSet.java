package com.jack.nit.gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

public class FrameSet
{
  private final Map<String, JFrame> frames;
  
  public FrameSet()
  {
    frames = new HashMap<>();
  }
  
  public void add(String name, JFrame frame)
  {
    frames.put(name, frame);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends JFrame> T get(String name)
  {
    return (T)frames.get(name);
  }
}
