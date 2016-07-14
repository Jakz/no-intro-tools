package com.jack.nit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.jack.nit.data.GameSet;
import com.pixbits.strings.StringUtils;

public class GameSetListPanel extends JPanel
{    
  private class SetTableRenderer implements TableCellRenderer
  {
    private final TableCellRenderer renderer;
    public Color evenColor = Color.white;
    public Color oddColor = UIManager.getColor("Table.alternateRowColor");
    public Color selectedColor = UIManager.getColor("Table[Enabled+Selected].textBackground");
    
    private final Border nonFinalBorder, finalBorder;
    
    SetTableRenderer(TableCellRenderer renderer)
    {
      this.renderer = renderer;
      
      nonFinalBorder = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180,180,180)), BorderFactory.createEmptyBorder(2, 5, 1, 5));
      finalBorder = BorderFactory.createEmptyBorder(2, 5, 2, 5);
    }
    
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int r, int c)
    {
      JLabel label = (JLabel)renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);

      /*label.setOpaque(true);
      
      if (isSelected)
        label.setBackground(selectedColor);
      else
        label.setBackground(r % 2 == 0 ? evenColor : oddColor);*/
      
      if (c == 0)
      {
        label.setHorizontalAlignment(SwingConstants.LEFT);
      }
      else
      {
        label.setHorizontalAlignment(SwingConstants.RIGHT);
      }
      
      if (r < model.getRowCount()-1)
      {
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        label.setBorder(nonFinalBorder);
      }
      else
      {
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(finalBorder);
      }

      return label;
    }
  }
  
  private final class SetTableModel extends AbstractTableModel
  {
    @Override public int getRowCount() { return sets.size()+1; }
    @Override public int getColumnCount() { return clazzes.length; }
    
    @Override public Object getValueAt(int r, int c)
    {
      if (r < sets.size())
      {  
        GameSet set = sets.get(r);
        
        switch (c)
        {
        case 0: return set.info.name;
        case 1: return set.info.gameCount();
        case 2: return set.info.uniqueGameCount();
        case 3: return set.info.romCount();
        case 4: return StringUtils.humanReadableByteCount(set.info.sizeInBytes());
        }
        
        return sets.get(r).info.name;
      }
      else
      {
        switch (c)
        {
        case 0: return "Total";
        case 1: return sets.stream().mapToInt(s -> s.info.gameCount()).sum();
        case 2: return sets.stream().mapToInt(s -> s.info.uniqueGameCount()).sum();
        case 3: return sets.stream().mapToInt(s -> s.info.romCount()).sum();
        case 4: return StringUtils.humanReadableByteCount(sets.stream().mapToLong(s -> s.info.sizeInBytes()).sum());
        }
      }
      
      return null;
    }
    
    private final Class<?>[] clazzes = {String.class, Integer.class, Integer.class, Integer.class, String.class};
    @Override public Class<?> getColumnClass(int i) { return clazzes[i]; }
    
    private final String[] names = {"name", "games", "unique", "roms", "size"};
    @Override public String getColumnName(int i) { return names[i]; }

  }
  
  private final List<GameSet> sets;
  private final JTable table;
  private final SetTableModel model;
  
  private final GameListPanel setPanel;
  
  public GameSetListPanel(List<GameSet> sets)
  {
    this.sets = sets;
    this.model = new SetTableModel();
    this.table = new JTable(model);
    this.setPanel = new GameListPanel();
    
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setDefaultRenderer(String.class, new SetTableRenderer(table.getDefaultRenderer(String.class)));
    table.setDefaultRenderer(Integer.class, new SetTableRenderer(table.getDefaultRenderer(Integer.class)));
    
    for (int i = 0; i < 4; ++i)
    {
      int width = i == 0 ? 100 : 60;
      
      table.getColumnModel().getColumn(table.getColumnCount()-i-1).setMinWidth(width);
      table.getColumnModel().getColumn(table.getColumnCount()-i-1).setMaxWidth(width);
    }
    
    table.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting())
      {
        setPanel.populate(sets.get(e.getFirstIndex()));
      }
    });
    
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(new Dimension(600,800));
    
    JSplitPane spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    spane.add(pane);
    spane.add(setPanel);
    
    setLayout(new BorderLayout());
    add(spane, BorderLayout.CENTER);
  }
}
