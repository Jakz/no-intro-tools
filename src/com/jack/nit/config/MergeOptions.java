package com.jack.nit.config;

import java.util.Arrays;

import com.jack.nit.Options.ArchiveFormat;

public class MergeOptions
{
  public static enum Mode
  {
    UNCOMPRESSED("uncompressed"),
    SINGLE_ARCHIVE_PER_GAME("archive-by-game"),
    SINGLE_ARCHIVE_PER_CLONE("archive-by-clone"),
    SINGLE_ARCHIVE_PER_SET("single-archive"),
    NO_MERGE("no-merge")
    ;
    
    public final String mnemonic;
    
    private Mode(String mnemonic)
    {
      this.mnemonic = mnemonic;
    }
    
    public static Mode forName(String name)
    {
      return Arrays.stream(values())
          .filter(m -> m.mnemonic.equals(name))
          .findFirst().orElse(Mode.SINGLE_ARCHIVE_PER_CLONE);
    }
    
  };
  
  public Mode mode;
  public ArchiveFormat archiveFormat;
  public boolean useSolidArchives;
  public int compressionLevel;
  
  public MergeOptions()
  {
    mode = Mode.NO_MERGE;
    archiveFormat = ArchiveFormat._7ZIP;
    useSolidArchives = true;
    compressionLevel = 9;
  }
}
