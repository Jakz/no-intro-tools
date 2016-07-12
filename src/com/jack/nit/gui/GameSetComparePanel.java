package com.jack.nit.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.Rom;

public class GameSetComparePanel extends JPanel
{
  private class TableModel extends AbstractTableModel
  {
    private List<GameSet> sets;
    private final int columnCount;
    private final int rowCount;
    private Game[][] data;
    
    private OptionalInt findEquivalentGame(GameSet set, Game game)
    {
      return IntStream.range(0, set.size()).parallel().filter(i -> set.get(i).isEquivalent(game)).findFirst();
    }
    
    TableModel(List<GameSet> sets)
    {
      this.sets = sets;
      
      GameSet mainSet = sets.get(0);
      
      columnCount = sets.size();  
      rowCount = sets.get(0).size();
      
      data = new Game[sets.size()][mainSet.size()];
      data[0] = sets.get(0).stream().toArray(i -> new Game[i]);
            
      for (int i = 1; i < sets.size(); ++i)
      {
        GameSet set = sets.get(i);
        for (int j = 0; j < mainSet.size(); ++j)
        {
          OptionalInt index = findEquivalentGame(set, mainSet.get(j));
          if (index.isPresent())
            data[i][j] = set.get(index.getAsInt());

        }
      }
    }

    @Override public String getColumnName(int i) { return sets.get(i).info.name; }
    @Override public int getRowCount() { return rowCount; }
    @Override public int getColumnCount() { return columnCount; }

    @Override
    public Object getValueAt(int r, int c)
    {
      Game game = data[c][r];
      
      if (game != null)
        return game.name;
      else
        return "";
    }
  }
  
  private final JTable table;
  private final TableModel model;
  
  public GameSetComparePanel(List<GameSet> sets)
  {
    model = new TableModel(sets);
    table = new JTable(model);
    
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(new Dimension(800,800));
    
    setLayout(new BorderLayout());
    add(pane, BorderLayout.CENTER);
  }
}
