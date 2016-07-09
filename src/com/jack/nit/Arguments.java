package com.jack.nit;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Arguments
{
  static ArgumentParser generateParser()
  {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("no-intro-tools")
        .defaultHelp(true)
        .description("Verify, rename and merge ROMS for no-intro DATs");
    
    parser.addArgument("--compression-level", "-cl")
      .help("compression level for creation of archives from 0 (store) to 9 (ultra)")
      .metavar("n")
      .type(Integer.class)
      .setDefault(5);
    
    parser.addArgument("--solid-archives", "-sa")
      .help("wheter not to use solid archives if available for format")
      .type(Boolean.class)
      .setDefault(true);
    
    return parser;
  }
}
