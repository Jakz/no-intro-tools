package com.jack.nit.log;

import com.jack.nit.Options;

public abstract class Logger
{  
  protected abstract void doLog(Log type, String message);
  
  public abstract void startProgress(String message);
  public abstract void updateProgress(float percent, String message);
  public abstract void endProgress();
  
  public static class MyLogger extends Logger
  {
    int lastProgress;
    final int logLevel;
    boolean showProgress = false;
    
    MyLogger(Options options)
    {
      this.logLevel = options.logLevel.ordinal();
    }
    
    @Override protected void doLog(Log type, String message)
    {
      if (type.ordinal() > logLevel)
        return;
      
      if (type == Log.DEBUG)
        System.err.println("["+type+"] "+message);
      else
        System.out.println("["+type+"] "+message);
    }
    
    final static int PROGRESS_LENGTH = 20;
        
    @Override public void startProgress(String message)
    {
      System.out.println(message);
      lastProgress = -1;
    }
    
    @Override public synchronized void updateProgress(float percent, String message)
    {
      if (!showProgress) return;
      
      int toPrint = (int)(percent*PROGRESS_LENGTH);
      int ipercent = (int)(percent*100);
            
      if (ipercent != lastProgress)
      {
        lastProgress = ipercent;
        
        System.out.print("\r[");

        
        int i = 0;
        for (; i < toPrint; ++i)
          System.out.print(".");
        for (; i < PROGRESS_LENGTH; ++i)
          System.out.print(" ");
        
        System.out.printf("] %3d%% %s", ipercent, message.length() < 40 ? message : (message.substring(0, 36)+"..."));
        System.out.flush();
      }
    }
    
    @Override public void endProgress()
    {
      System.out.print("\r[");
      for (int i = 0; i < PROGRESS_LENGTH; ++i)
        System.out.print(".");
      System.out.println("] 100%");
    }
  };
  
  public static Logger logger;
  
  public static void init(Options options)
  {
    logger = new MyLogger(options);
  }

  public static void log(Log type, String message)
  {
    //if (type == Log.INFO2 || type == Log.INFO3)
    //  return;
    logger.doLog(type, message);
  }
  
  public static void log(Log type, String message, Object arg, Object... args)
  {
    Object[] data = new Object[1+args.length];
    data[0] = arg;
    System.arraycopy(args, 0, data, 1, args.length);
    
    log(type, String.format(message, data));
  }
}
