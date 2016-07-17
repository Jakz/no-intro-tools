package com.pixbits.gui;

import java.awt.Component;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class LambdaTableCellRenderer<T> implements TableCellRenderer
{
  private final Predicate<T> predicate;
  private final Consumer<JComponent> trueEffect, falseEffect;
  
  private final TableCellRenderer renderer;
  
  public LambdaTableCellRenderer(Predicate<T> predicate, Consumer<JComponent> trueEffect, Consumer<JComponent> falseEffect, TableCellRenderer renderer)
  {
    this.renderer = renderer;
    this.predicate = predicate;
    this.trueEffect = trueEffect;
    this.falseEffect = falseEffect;
  }
  
  @SuppressWarnings("unchecked")
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean cellHasFocus, int r, int c)
  {
    JComponent component = (JComponent)renderer.getTableCellRendererComponent(table, value, isSelected, cellHasFocus, r, c);
    
    component.setOpaque(true);
    
    if (predicate.test((T)value))
      trueEffect.accept(component);
    else
      falseEffect.accept(component);
    
    return component;
  }
}