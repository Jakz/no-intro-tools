package com.jack.nit.scanner;

public class Options
{
  public static enum MergeMode
  {
    UNCOMPRESSED,
    SINGLE_ARCHIVE_PER_GAME,
    SINGLE_ARCHIVE_PER_CLONE,
    SINGLE_ARCHIVE_PER_SET
  };
  
  public static enum ArchiveFormat
  {
    _7ZIP
  };
  
  public final boolean matchSize;
  public final boolean matchSHA1;
  public final boolean matchMD5;
  public final boolean multiThreaded;
  
  public final MergeMode mergeMode;
  public final ArchiveFormat archiveFormat;
  public final boolean useSolidArchives;
  public final int compressionLevel;
  
  public Options()
  {
    matchSize = true;
    matchSHA1 = true;
    matchMD5 = true;
    multiThreaded = true;
    
    mergeMode = MergeMode.SINGLE_ARCHIVE_PER_CLONE;
    
    archiveFormat = ArchiveFormat._7ZIP;
    useSolidArchives = true;
    compressionLevel = 5;
  }
}
