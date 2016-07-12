package com.jack.nit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.jack.nit.data.xmdb.BiasSet;
import com.jack.nit.data.xmdb.Zone;
import com.jack.nit.log.Log;

import net.sourceforge.argparse4j.inf.Namespace;

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
  
  public Log logLevel;
  
  public boolean matchSize;
  public boolean matchSHA1;
  public boolean matchMD5;
  public boolean multiThreaded;
  
  
  private boolean forceMergeInPlace;
  public MergeMode mergeMode;
  public ArchiveFormat archiveFormat;
  public boolean useSolidArchives;
  public int compressionLevel;
  public boolean alwaysRewriteArchives;
  public boolean keepUnrecognizedFilesInArchives;
  public boolean cleanMergePathAfterMerge;
  
  public Path datPath;
  public Path headerPath;
  public Path cloneDatPath;
  
  public Path[] dataPath;
  private Path mergePath;
  
  public final BiasSet zonePriority;
  
  public Options(Namespace args)
  {
    logLevel = Log.INFO3;
    
    matchSize = !args.getBoolean(Args.NO_SIZE_CHECK);
    matchSHA1 = !args.getBoolean(Args.NO_SHA1_CHECK);
    matchMD5 = !args.getBoolean(Args.NO_MD5_CHECK);
    
    multiThreaded = !args.getBoolean(Args.NO_MULTI_THREAD);
    
    switch (args.getString(Args.MERGE_MODE))
    {
      case "uncompressed":
        mergeMode = MergeMode.UNCOMPRESSED; break;
      case "archive-by-clone":
        mergeMode = MergeMode.SINGLE_ARCHIVE_PER_CLONE; break;
      case "archive-by-game":
        mergeMode = MergeMode.SINGLE_ARCHIVE_PER_GAME; break;
      case "single-archive":
        mergeMode = MergeMode.SINGLE_ARCHIVE_PER_SET; break;
      default:
        mergeMode = MergeMode.SINGLE_ARCHIVE_PER_CLONE; break;
    }
    
    forceMergeInPlace = args.getBoolean(Args.IN_PLACE_MERGE);
    
    archiveFormat = ArchiveFormat._7ZIP;
    
    useSolidArchives = !args.getBoolean(Args.NO_SOLID_ARCHIVES);
    compressionLevel = args.getInt(Args.COMPRESSION_LEVEL);
    alwaysRewriteArchives = args.getBoolean(Args.ALWAYS_REWRITE_ARCHIVES);
    keepUnrecognizedFilesInArchives = args.getBoolean(Args.KEEP_UNRECOGNIZED_FILES);
    cleanMergePathAfterMerge = true;
    
    datPath =  Paths.get(args.getString(Args.DAT_PATH));
    headerPath = args.get(Args.HEADER_PATH) != null ? Paths.get(args.getString(Args.HEADER_PATH)) : null;
    cloneDatPath = args.get(Args.CLONE_PATH) != null ? Paths.get(args.getString(Args.CLONE_PATH)) : null;
    
    List<String> dataPaths = args.getList(Args.DATA_PATH);
    
    dataPath = dataPaths.stream().map(s -> Paths.get(s)).toArray(i -> new Path[i]);
    mergePath = args.get(Args.DEST_PATH) != null ? Paths.get(args.getString(Args.DEST_PATH)) : null;
    
    zonePriority = new BiasSet(Zone.ITALY, Zone.EUROPE, Zone.USA, Zone.JAPAN);
  }
  
  public Options()
  {
    logLevel = Log.DEBUG;
    
    matchSize = true;
    matchSHA1 = false;
    matchMD5 = false;
    multiThreaded = true;
    
    mergeMode = MergeMode.SINGLE_ARCHIVE_PER_CLONE;
    forceMergeInPlace = false;
    
    archiveFormat = ArchiveFormat._7ZIP;
    useSolidArchives = true;
    compressionLevel = 9;
    alwaysRewriteArchives = false;
    keepUnrecognizedFilesInArchives = false;
    cleanMergePathAfterMerge = true;
    
    datPath = Paths.get("dats/gb.dat");
    headerPath = null;
    cloneDatPath = Paths.get("dats/gb.xmdb");
    
    dataPath = new Path[] { Paths.get("/Volumes/RAM Disk/gb") };
    //dataPath = new Path[] { Paths.get("/Users/jack/Desktop/romset/gb") };

    mergePath = Paths.get("/Volumes/RAM Disk/merge");
    
    zonePriority = new BiasSet(Zone.ITALY, Zone.EUROPE, Zone.USA, Zone.JAPAN);
  }

  public boolean verifyJustCRC() { return !(matchSHA1 || matchMD5); }
  
  //TODO: not correct, if merge in place it should be original path of rom or force 1 data path max
  public Path mergePath() { return forceMergeInPlace ? dataPath[0] : mergePath; }
  public boolean doesMergeInPlace() { return forceMergeInPlace || dataPath.equals(mergePath); } 
  
  public static Options simpleDatLoad(Path path)
  {
    Options options = new Options();
    options.datPath = path;
    return options;
  }
}
