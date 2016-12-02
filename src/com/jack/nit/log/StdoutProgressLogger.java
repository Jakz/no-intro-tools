package com.jack.nit.log;

import javax.swing.SwingUtilities;

import com.pixbits.lib.gui.ProgressDialog;

public class StdoutProgressLogger implements Logger
{
  float lastProgress;
  String progressMessage;
  final int logLevel;
  
  StdoutProgressLogger(Log logLevel)
  {
    this.logLevel = logLevel.ordinal();
  }
  
  @Override public void doLog(Log type, String message)
  {
    if (type != null && type.ordinal() > logLevel)
      return;
    
    if (type == Log.DEBUG || type == Log.ERROR)
      System.err.println("["+type+"] "+message);
    else if (type != null)
      System.out.println("["+type+"] "+message);
    else
      System.out.println(message);
  }
       
  private boolean running = false;
  private Thread dialogThread = null;
  private Runnable dialogUpdater = () -> {
    try
    {
      while (running)
      {
        Thread.sleep(50);
        SwingUtilities.invokeLater(() -> { 
          if (running) ProgressDialog.update(lastProgress, progressMessage); 
        });
      }
    }
    catch (InterruptedException e)
    {
      ProgressDialog.finished();
    }
  };
  
  @Override public void startProgress(Log type, String message)
  {
    lastProgress = 0;
    SwingUtilities.invokeLater(() -> ProgressDialog.init(null, message, null));
    dialogThread = new Thread(dialogUpdater);
    running = true;
    dialogThread.start();
  }
  
  @Override public synchronized void updateProgress(float percent, String message)
  {
    lastProgress = percent;
    progressMessage = message;
  }
  
  @Override public void endProgress()
  {
    running = false;
    SwingUtilities.invokeLater(() -> ProgressDialog.finished());
  }
}