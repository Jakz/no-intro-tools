package com.jack.nit.gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;

public class LogPanel extends JPanel implements Logger
{
  private class LogEntry
  {
    final Log type;
    final String message;
    LogEntry(Log type, String message) { this.type = type; this.message = message; }
  }
  
  private final List<LogEntry> entries;
  private Log filter;
  
  private final JTextArea area;
  private final JComboBox<Log> filterBox;
  
  public LogPanel(int rows, int cols)
  {
    entries = new ArrayList<>();
    
    area = new JTextArea(rows, cols);
    area.setEditable(false);
    area.setFont(new Font("Monaco", Font.PLAIN, 10));
    JScrollPane pane = new JScrollPane(area);
    this.setLayout(new BorderLayout());
    this.add(pane, BorderLayout.CENTER);
    
    filterBox = new JComboBox<>(Log.values());
    this.add(filterBox, BorderLayout.SOUTH);
    
    filterBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
        filter = filterBox.getItemAt(filterBox.getSelectedIndex());
        rebuildLog();
      }
    });
    
    filter = Log.DEBUG;
    
    filterBox.setSelectedItem(filter);
  }
  
  private void rebuildLog()
  {
    area.setText("");
    for (LogEntry entry : entries)
      appendLog(entry.type, entry.message);
  }
  
  private void appendLog(Log type, String message)
  {
    if (type.ordinal() <= filter.ordinal())
      area.append("["+type+"] "+message+"\n");
  }

  @Override
  public void doLog(Log type, String message)
  {
    SwingUtilities.invokeLater(() -> {
      appendLog(type, message);
      entries.add(new LogEntry(type, message));
    });
  }

  @Override
  public void startProgress(String message)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateProgress(float percent, String message)
  {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void endProgress()
  {
    // TODO Auto-generated method stub
    
  }

}
