package com.pixbits.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class AlternateColorTableCellRenderer implements TableCellRenderer
{
  public static Color evenColor = Color.white;
  public static Color oddColor = UIManager.getColor("Table.alternateRowColor");
  public static Color selectedColor = UIManager.getColor("Table[Enabled+Selected].textBackground");
  
  private TableCellRenderer renderer;
  
  public AlternateColorTableCellRenderer(TableCellRenderer renderer)
  {
    this.renderer = renderer;
  }
  
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean cellHasFocus, int r, int c)
  {
    JComponent component = (JComponent)renderer.getTableCellRendererComponent(table, value, isSelected, cellHasFocus, r, c);
    
    component.setOpaque(true);
    
    if (isSelected)
      component.setBackground(selectedColor);
    else
      component.setBackground(r % 2 == 0 ? evenColor : oddColor);

    return component;
  }
}