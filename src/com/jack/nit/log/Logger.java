package com.jack.nit.log;

public abstract class Logger
{
  private boolean timeEnabled;
  
  protected abstract void doLog(Log type, String message);
  
  public abstract void startProgress(String message);
  public abstract void updateProgress(float percent);
  public abstract void endProgress();
  
  public static final Logger logger = new Logger()
  {
    @Override protected void doLog(Log type, String message)
    {
      System.out.println("["+type+"] "+message);
    }
    
    final static int PROGRESS_LENGTH = 40;
        
    @Override public void startProgress(String message)
    {
      System.out.println(message);
    }
    
    @Override public void updateProgress(float percent)
    {
      int toPrint = (int)(percent*PROGRESS_LENGTH);
      
      System.out.print("\r[");
      
      int i = 0;
      for (; i < toPrint; ++i)
        System.out.print(".");
      for (; i < PROGRESS_LENGTH; ++i)
        System.out.print(" ");
      
      System.out.printf("] %d%% ", (int)(percent*100));
    }
    
    @Override public void endProgress()
    {
      System.out.print("\r[");
      for (int i = 0; i < PROGRESS_LENGTH; ++i)
        System.out.print(".");
      System.out.println("] 100%");
    }
  };

  public static void log(Log type, String message)
  {
    logger.doLog(type, message);
  }
  
  public static void log(Log type, String message, Object arg, Object... args)
  {
    Object[] data = new Object[1+args.length];
    data[0] = arg;
    System.arraycopy(args, 0, data, 1, args.length);
    
    logger.doLog(type, String.format(message, data));
  }
}
