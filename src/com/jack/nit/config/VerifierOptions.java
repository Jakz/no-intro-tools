package com.jack.nit.config;

import com.jack.nit.Args;

import net.sourceforge.argparse4j.inf.Namespace;

public class VerifierOptions
{
  public boolean matchSize;
  public boolean matchSHA1;
  public boolean matchMD5;
  public boolean checkNestedArchives;

  
  public VerifierOptions(Namespace args)
  {
    matchSize = !args.getBoolean(Args.NO_SIZE_CHECK);
    matchSHA1 = !args.getBoolean(Args.NO_SHA1_CHECK);
    matchMD5 = !args.getBoolean(Args.NO_MD5_CHECK);
    checkNestedArchives = !args.getBoolean(Args.NO_NESTED_ARCHIVES);
  }
  
  public VerifierOptions()
  {
    matchSize = true;
    matchSHA1 = true;
    matchMD5 = true;
    checkNestedArchives = true;
  }
  
  public boolean verifyJustCRC() { return !(matchSHA1 || matchMD5); }
}
