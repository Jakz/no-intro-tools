package com.jack.nit.log;

import com.jack.nit.Options;

public enum Log
{
  ERROR,
  WARNING,
  INFO1,
  INFO2,
  INFO3,
  DEBUG;

  public static Logger logger;

  public static void init(Options options)
  {
    logger = new StdoutLogger(options.logLevel);
  }

  public static void init()
  {
    logger = new StdoutLogger(DEBUG);
  }

  public static void setLogger(Logger logger)
  {
    Log.logger = logger;
  }

  public static void log(String message, Object... args)
  {
    Log.log(null, String.format(message, args));
  }

  public static void log(Log type, String message, Object arg, Object... args)
  {
    Object[] data = new Object[1+args.length];
    data[0] = arg;
    System.arraycopy(args, 0, data, 1, args.length);
    
    Log.log(type, String.format(message, data));
  }

  public static void log(Log type, String message)
  {
    logger.doLog(type, message);
  }
};