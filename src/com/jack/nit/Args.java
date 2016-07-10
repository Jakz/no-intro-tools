package com.jack.nit;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Args
{
  public final static String COMPRESSION_LEVEL = "compression-level";
  public final static String NO_SOLID_ARCHIVES = "no-solid-archives";
  public final static String ALWAYS_REWRITE_ARCHIVES = "always-rewrite-archives";
  public final static String KEEP_UNRECOGNIZED_FILES = "keep-unrecognized-files-in-archives";
  
  public final static String NO_MULTI_THREAD = "no-multi-thread";
  public final static String MERGE_MODE = "merge-mode";
  public final static String IN_PLACE_MERGE = "force-merge-in-place";
  public final static String NO_SHA1_CHECK = "no-sha1";
  public final static String NO_MD5_CHECK = "no-md5";
  public final static String NO_SIZE_CHECK = "no-size";
  
  public final static String DATA_PATH = "data-path";
  public final static String DAT_PATH = "dat-path";
  public final static String HEADER_PATH = "header-path";
  public final static String CLONE_PATH = "clone-path";
  public final static String DEST_PATH = "dest-path";

  
  static ArgumentParser generateParser()
  {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("no-intro-tools")
        .defaultHelp(true)
        .description("Verify, rename and merge ROMS for no-intro DATs");
    
    parser.addArgument("--compression-level", "-cl")
      .dest(COMPRESSION_LEVEL)
      .type(Integer.class)
      .help("compression level for creation of archives from 0 (store) to 9 (ultra)")
      .metavar("n")
      .setDefault(5);
    
    parser.addArgument("--no-solid-archives", "-nsa")
      .dest(NO_SOLID_ARCHIVES)
      .help("disable creation of solid archives")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--always-rewrite-archives", "-ara")
      .dest(ALWAYS_REWRITE_ARCHIVES)
      .help("force creation of archives regardless of their up-to-date status")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--keep-unrecognized-files", "-kuf")
      .dest(KEEP_UNRECOGNIZED_FILES)
      .help("keep unrecognized files in already present archives")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--force-merge-in-place", "-mip")
      .dest(IN_PLACE_MERGE)
      .help("override merge path to be the same of rom path")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--no-multi-thread", "-nmt")
      .dest(NO_MULTI_THREAD)
      .help("disable multi-threaded workflow")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--merge-mode", "-mm")
      .dest(MERGE_MODE)
      .help("specify kind of merge you want")
      .choices("uncompressed", "single-archive", "archive-by-clone", "archive-by-game")
      .setDefault("archive-by-clone");
    
    parser.addArgument("--no-md5")
      .dest(NO_MD5_CHECK)
      .help("disable MD5 check")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--no-sha1")
      .dest(NO_SHA1_CHECK)
      .help("disable SHA-1 check")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--no-size-check")
      .dest(NO_SIZE_CHECK)
      .help("disable size check")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false);
    
    parser.addArgument("--dat-file", "-dat=")
      .dest(DAT_PATH)
      .type(String.class)
      .required(true)
      .help("Path to DAT file");
    
    parser.addArgument("--roms-path", "-roms=")
      .dest(DATA_PATH)
      .type(String.class)
      .nargs("+")
      .required(true)
      .help("Path to roms folder");
    
    parser.addArgument("--clones-file", "--clones")
      .dest(CLONE_PATH)
      .type(String.class)
      .help("Path to clones definition file");
    
    parser.addArgument("--header-file", "--header")
      .dest(HEADER_PATH)
      .type(String.class)
      .help("Path to optional header file for DAT");
    
    return parser;
  }
}
