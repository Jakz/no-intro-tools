package com.jack.nit.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.jack.nit.data.GameSet;

public class GameSetListPanel extends JPanel
{
  final class RomSetListCellRenderer extends DefaultListCellRenderer
  {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
      JLabel c = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      GameSet set = (GameSet)value;   
      if (set != null && set.system() != null)
        c.setIcon(set.system().getIcon());
      return c;
    }
  }
  
  
  //private final JList list;
  
  
}
