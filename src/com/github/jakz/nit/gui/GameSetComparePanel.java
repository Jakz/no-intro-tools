package com.github.jakz.nit.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.github.jakz.nit.data.Game;
import com.github.jakz.nit.data.GameSet;

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
  
  private class InfoPanel extends JPanel
  {
    JLabel[] labels;
    
    private InfoPanel(List<GameSet> sets)
    {
      labels = sets.stream().map(set -> {
        return new JLabel(set.info.name);
      }).toArray(i -> new JLabel[i]);
      
      setLayout(new GridLayout(labels.length, 1));
      for (JLabel label : labels) add(label);
      
      setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
    }
    
  }
  
  private final JTable table;
  private final TableModel model;
  private final InfoPanel info;
    
  public GameSetComparePanel(List<GameSet> sets)
  {
    model = new TableModel(sets);
    table = new JTable(model);
    
    
    info = new InfoPanel(sets);
        
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(new Dimension(400,800));
       
    setLayout(new BorderLayout());
    add(pane, BorderLayout.CENTER);
    add(info, BorderLayout.SOUTH);
  }
}
