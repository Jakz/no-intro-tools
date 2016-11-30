package com.jack.nit.log;

public interface Logger
{  
  public void doLog(Log type, String message);
  public void startProgress(String message);
  public void updateProgress(float percent, String message);
  public void endProgress();
}
