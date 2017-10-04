package com.github.jakz.nit.gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.github.jakz.nit.DevMain;

public class GameSetMenu extends JMenuBar
{
  public GameSetMenu()
  {
    JMenu viewMenu = new JMenu("View");
    
    JMenuItem showLog = new JMenuItem("Show Log");
    showLog.addActionListener(e -> {
      DevMain.frames.get("log").setLocationRelativeTo(DevMain.frames.get("main"));
      DevMain.frames.get("log").setVisible(true);
    });
    
    viewMenu.add(showLog);
    
    this.add(viewMenu);
  }
  
}
