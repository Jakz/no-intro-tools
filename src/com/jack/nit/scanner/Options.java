package com.jack.nit.scanner;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.jack.nit.data.xmdb.BiasSet;
import com.jack.nit.data.xmdb.Zone;

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
    _7ZIP(".7z")
    ;
    
    private ArchiveFormat(String extension) { this.extension = extension; }
    
    public final String extension;
  };
  
  public final boolean matchSize;
  public final boolean matchSHA1;
  public final boolean matchMD5;
  public final boolean multiThreaded;
  
  public final MergeMode mergeMode;
  public final ArchiveFormat archiveFormat;
  public final boolean useSolidArchives;
  public final int compressionLevel;
  
  public final Path datPath;
  public final Path headerPath;
  public final Path cloneDatPath;
  
  public final Path[] dataPath;
  public final Path mergePath;
  
  public final BiasSet zonePriority;
  
  public Options()
  {
    matchSize = true;
    matchSHA1 = true;
    matchMD5 = true;
    multiThreaded = true;
    
    mergeMode = MergeMode.SINGLE_ARCHIVE_PER_CLONE;
    
    archiveFormat = ArchiveFormat._7ZIP;
    useSolidArchives = true;
    compressionLevel = 9;
    
    datPath = Paths.get("dats/gba.dat");
    headerPath = null;
    cloneDatPath = Paths.get("dats/gba.xmdb");
    
    dataPath = new Path[] { Paths.get("/Users/jack/Desktop/romset/gb") };
    mergePath = Paths.get("/Users/jack/Desktop/romset/merge");
    
    zonePriority = new BiasSet(Zone.ITALY, Zone.EUROPE, Zone.USA, Zone.JAPAN);

  }
  
  boolean doesMergeInPlace() { return dataPath.equals(mergePath); } 
}
