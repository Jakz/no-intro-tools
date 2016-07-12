package com.jack.nit.gui;

import javax.swing.JPanel;
import javax.swing.table.AbstractTableModel;

import com.jack.nit.data.GameSet;

public class GameSetComparePanel extends JPanel
{
  private class TableModel extends AbstractTableModel
  {
    private GameSet[] sets;
    //private final int rowCount;
    
    
    TableModel(GameSet... sets)
    {
      this.sets = sets;
    }


    @Override public int getRowCount()
    {
      // TODO Auto-generated method stub
      return 0;
    }


    @Override public int getColumnCount()
    {
      return 0;
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      
      return null;
    }
  }
}
