package com.github.jakz.nit.gui;

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

import com.github.jakz.nit.DevMain;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.LogAttribute;
import com.pixbits.lib.log.LogScope;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.LoggerFactory;
import com.pixbits.lib.log.ProgressLogger;
import com.pixbits.lib.ui.elements.ProgressDialog;

public class LogPanel extends JPanel implements LoggerFactory, ProgressLogger
{
  private class LoggerReceiver extends Logger
  {
    LoggerReceiver(LogScope scope)
    {
      super(scope);
    }
    
    @Override
    protected void doLog(Log type, String message, LogAttribute attr)
    {
      SwingUtilities.invokeLater(() -> {
        appendLog(type, message);
        entries.add(new LogEntry(type, message));
      });
    }
  }
    
  private final ProgressDialog.Manager manager = new ProgressDialog.Manager();
  private final LoggerReceiver receiver = new LoggerReceiver(LogScope.ANY);
  
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
  public Logger build(LogScope scope)
  {
    return receiver;
  }
  
  float lastProgress;
  String progressMessage;
  
  private boolean running = false;
  private Thread dialogThread = null;
  private Runnable dialogUpdater = () -> {
    try
    {
      while (running)
      {
        Thread.sleep(50);
        SwingUtilities.invokeLater(() -> { 
          if (running) manager.update(lastProgress, progressMessage); 
        });
      }
    }
    catch (InterruptedException e)
    {
      manager.finished();
    }
  }; 
  
  @Override public void startProgress(Log type, String message)
  {
    try
    {
      lastProgress = 0;
      SwingUtilities.invokeAndWait(() -> manager.show(DevMain.frames.get("main"), message, null));
      dialogThread = new Thread(dialogUpdater);
      running = true;
      dialogThread.start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  @Override public synchronized void updateProgress(float percent, String message)
  {
    lastProgress = percent;
    progressMessage = message;
  }
  
  @Override public void endProgress()
  {
    running = false;
    SwingUtilities.invokeLater(manager::finished);
  }

}
