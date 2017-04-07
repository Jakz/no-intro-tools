package com.github.jakz.nit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
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

import com.github.jakz.nit.config.Config.DatEntry;
import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.io.archive.ArchiveFormat;
import com.pixbits.lib.io.archive.Scanner;
import com.pixbits.lib.lang.StringUtils;
import com.pixbits.lib.ui.elements.BrowseButton;

public class GameSetListPanel extends JPanel
{    
  private class SetTableRenderer implements TableCellRenderer
  {
    private final TableCellRenderer renderer;
    public Color oddColor = UIManager.getColor("Table.alternateRowColor");
    public Color selectedColor = UIManager.getColor("Table[Enabled+Selected].textBackground");
    
    private final SetTableModel model;
    private final Border nonFinalBorder, finalBorder;
    
    SetTableRenderer(TableCellRenderer renderer, SetTableModel model)
    {
      this.renderer = renderer;
      this.model = model;
      
      nonFinalBorder = BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180,180,180)), BorderFactory.createEmptyBorder(2, 5, 1, 5));
      finalBorder = BorderFactory.createEmptyBorder(2, 5, 2, 5);
    }
    
    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int r, int c)
    {
      JLabel label = (JLabel)renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, r, c);
      
      if (c == 0)
        label.setHorizontalAlignment(SwingConstants.LEFT);
      else
        label.setHorizontalAlignment(SwingConstants.RIGHT);
      
      if (r < model.getRowCount()-1)
      {
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        label.setBorder(nonFinalBorder);
        
        if (c == 0)
        {
          GameSet set = sets.get(r);
          
          if (setData.get(set).platform != null)
            label.setIcon(setData.get(set).platform.getIcon());
          else
            label.setIcon(null);
        }
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
        case 0: return set.info().getName();
        case 1: return set.info().gameCount();
        case 2: return set.info().uniqueGameCount();
        case 3: return set.info().romCount();
        case 4: return StringUtils.humanReadableByteCount(set.info().sizeInBytes());
        }
        
        return sets.get(r).info().getName();
      }
      else
      {
        switch (c)
        {
        case 0: return "Total";
        case 1: return sets.stream().mapToInt(s -> s.info().gameCount()).sum();
        case 2: return sets.stream().mapToInt(s -> s.info().uniqueGameCount()).sum();
        case 3: return sets.stream().mapToInt(s -> s.info().romCount()).sum();
        case 4: return StringUtils.humanReadableByteCount(sets.stream().mapToLong(s -> s.info().sizeInBytes()).sum());
        }
      }
      
      return null;
    }
    
    private final Class<?>[] clazzes = {String.class, Integer.class, Integer.class, Integer.class, String.class};
    @Override public Class<?> getColumnClass(int i) { return clazzes[i]; }
    
    private final String[] names = {"name", "games", "unique", "roms", "size"};
    @Override public String getColumnName(int i) { return names[i]; }
  }
  
  private final class SetOptions extends JPanel
  {
    BrowseButton romsetPath;
    JButton verify;
    
    public SetOptions()
    {
      setPreferredSize(new Dimension(300,200));
      
      romsetPath = new BrowseButton(30, BrowseButton.Type.FILES_AND_DIRECTORIES);
      romsetPath.setFilter(ArchiveFormat.getReadableMatcher(), "Romsets");
      
      verify = new JButton("Verify");
      
      add(romsetPath);
      add(verify);
    }
    
    void verify()
    {
      romsetPath.getPath();
      
      
      
    }
    
    void update(GameSet set)
    {
      verify.setEnabled(set != null);
      romsetPath.setEnabled(set != null);
      romsetPath.setPath(set != null ? setData.get(set).romsetPaths.get(0) : null);
    }
  }
  
  private GameSet set;
  private final List<GameSet> sets;
  private final Map<GameSet, DatEntry> setData;
  private final JTable table;
  private final SetTableModel model;
  
  private final SetOptions setOptions;
  private final GameListPanel setPanel;
  
  public GameSetListPanel(List<GameSet> sets, Map<GameSet, DatEntry> data)
  {
    this.sets = sets;
    this.setData = data;
    this.model = new SetTableModel();
    this.table = new JTable(model);
    this.setPanel = new GameListPanel();
    this.setOptions = new SetOptions();
        
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setDefaultRenderer(String.class, new SetTableRenderer(table.getDefaultRenderer(String.class), model));
    table.setDefaultRenderer(Integer.class, new SetTableRenderer(table.getDefaultRenderer(Integer.class), model));
    table.setRowHeight(20);
    
    for (int i = 0; i < 4; ++i)
    {
      int width = i == 0 ? 80 : 50;
      
      table.getColumnModel().getColumn(table.getColumnCount()-i-1).setMinWidth(width);
      table.getColumnModel().getColumn(table.getColumnCount()-i-1).setMaxWidth(width);
    }
    
    table.getSelectionModel().addListSelectionListener(e -> {
      int index = table.getSelectedRow();
            
      if (!e.getValueIsAdjusting())
      {
        this.set = index < sets.size() ? sets.get(index) : null;
        setPanel.populate(set);
        setOptions.update(set);
      }
    });
    
    JScrollPane pane = new JScrollPane(table);
    //pane.setPreferredSize(new Dimension(300,800));
    
    JPanel leftPane = new JPanel();
    leftPane.setLayout(new BorderLayout());
    leftPane.add(pane, BorderLayout.CENTER);
    leftPane.add(setOptions, BorderLayout.SOUTH);
    
    JSplitPane spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    spane.add(leftPane);
    spane.add(setPanel);
    
    setLayout(new BorderLayout());
    add(spane, BorderLayout.CENTER);
  }
}
