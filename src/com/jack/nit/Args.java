package com.jack.nit;

import com.jack.nit.creator.CreatorOptions;
import com.jack.nit.parser.DatFormat;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

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

  private static void generateVerifierParser(ArgumentParser parser)
  {
    parser.setDefault("command", Command.VERIFY);
    
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
      .help("path to DAT file");
    
    parser.addArgument("--roms-path", "-roms=")
      .dest(DATA_PATH)
      .type(String.class)
      .nargs("+")
      .required(true)
      .help("path to roms folder");
    
    parser.addArgument("--clones-file", "--clones")
      .dest(CLONE_PATH)
      .type(String.class)
      .help("path to clones definition file");
    
    parser.addArgument("--header-file", "--header")
      .dest(HEADER_PATH)
      .type(String.class)
      .help("path to optional header file for DAT");
  }
  
  static void generateDatCreateParser(ArgumentParser parser)
  {
    parser.setDefault("command", Command.CREATE_DAT);
        
    parser.addArgument("--format", "-f")
      .dest("format")
      .type(DatFormat.class)
      .choices(DatFormat.values())
      .setDefault(DatFormat.clrmamepro)
      .help("output format for generated DAT file");
    
    parser.addArgument("--mode", "-m")
      .dest("mode")
      .type(CreatorOptions.Mode.class)
      .choices(CreatorOptions.Mode.values())
      .setDefault(CreatorOptions.Mode.merged)
      .help("mode of generation, merged means that an archive contains multiple versions of same game while multi means that an archive contains multiple roms for same game");
    
    parser.addArgument("--folder-as-archives", "-faa")
      .dest("folder-as-archives")
      .action(Arguments.storeConst())
      .setConst(true)
      .setDefault(false)
      .help("treat folder as archives, so that a folder will be considered a clone set or a multiple rom game according to mode");
    
    parser.addArgument("--exts", "-e")
      .dest("exts")
      .type(String.class)
      .nargs("+")
      .help("comma separated list of accepted estensions, use * for any files");
    
    parser.addArgument("--out", "-o")
      .dest("outfile")
      .type(String.class)
      .setDefault("output.dat")
      .help("path to created DAT file");
    
    parser.addArgument("--name", "-n")
      .setDefault("")
      .dest("name")
      .type(String.class)
      .help("name of the DAT file");
    
    parser.addArgument("--desc", "-d")
      .setDefault("")
      .dest("description")
      .type(String.class)
      .help("description of the DAT file");
    
    parser.addArgument("--version")
      .setDefault("")
      .dest("version")
      .type(String.class)
      .help("version of the DAT file");
    
    parser.addArgument("--comment", "-c")
      .setDefault("")
      .dest("comment")
      .type(String.class)
      .help("description of the DAT file");
    
    parser.addArgument("infiles")
      .dest("infile")
      .type(String.class)
      .nargs("+")
      .required(true)
      .help("paths to folders or archives to scan to generate DAT");
    

  }
  
  static ArgumentParser generateParser()
  {
    ArgumentParser parser = ArgumentParsers.newArgumentParser("no-intro-tools")
        .defaultHelp(true)
        .description("Verify, rename and merge ROMS for no-intro DATs");
        
    Subparsers subparsers = parser.addSubparsers().help("action to perform");
    
    Subparser verifierParser = subparsers.addParser("verify").help("verify and organize roms");
    generateVerifierParser(verifierParser);

    Subparser createDatParser = subparsers.addParser("create-dat").help("create DAT from existing files");
    generateDatCreateParser(createDatParser);
    
    parser.addArgument("--no-multi-thread", "-nmt")
    .dest(NO_MULTI_THREAD)
    .help("disable multi-threaded workflow")
    .action(Arguments.storeConst())
    .setConst(true)
    .setDefault(false);
 
    
    return parser;
  }
}
