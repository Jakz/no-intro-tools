package com.jack.nit.log;

public abstract class Logger
{
  private boolean timeEnabled;
  
  protected abstract void doLog(Log type, String message);
  
  static final Logger logger = new Logger()
  {
    @Override public void doLog(Log type, String message)
    {
      System.out.println("["+type+"] "+message);
    }
  };
  
  public static void log(Log type, String message)
  {
    logger.doLog(type, message);
  }
}
