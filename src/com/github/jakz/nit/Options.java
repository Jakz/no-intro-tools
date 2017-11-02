package com.github.jakz.nit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.github.jakz.nit.config.MergeOptions;
import com.github.jakz.romlib.data.game.BiasSet;
import com.github.jakz.romlib.data.game.Location;
import com.pixbits.lib.io.archive.CompressorOptions;
import com.pixbits.lib.io.archive.VerifierOptions;
import com.pixbits.lib.log.Log;

import net.sourceforge.argparse4j.inf.Namespace;

public class Options
{
  public final Log logLevel;

  public final boolean multiThreaded;
  
  public final MergeOptions merge;
  public final VerifierOptions verifier;
  
  public final boolean alwaysRewriteArchives;
  public final boolean keepUnrecognizedFilesInArchives;
  public final boolean cleanMergePathAfterMerge;
  public final boolean verifyMerge;
  public final boolean skipRename;
  
  public DatType datFormat;
  public Path datPath;
  public Path headerPath;
  public Path cloneDatPath;
  
  public Path[] dataPath;
  private Path mergePath;
  private Path wholeArchivePath;
  
  public final BiasSet zonePriority;
  
  public CompressorOptions getCompressorOptions()
  {
    return new CompressorOptions(merge.archiveFormat, merge.useSolidArchives, merge.compressionLevel);
  }
  
  public Options(Namespace args)
  {
    logLevel = Log.INFO3;
        
    multiThreaded = !args.getBoolean(Args.NO_MULTI_THREAD);
    
    merge = new MergeOptions(args);
    
    boolean verifyNestedArchives = !args.getBoolean(Args.NO_NESTED_ARCHIVES);
    if (args.getBoolean(Args.FAST))
      verifier = new VerifierOptions(true, false, false, verifyNestedArchives);
    else
      verifier = new VerifierOptions(!args.getBoolean(Args.NO_SIZE_CHECK), !args.getBoolean(Args.NO_MD5_CHECK), !args.getBoolean(Args.NO_SHA1_CHECK), verifyNestedArchives);

    alwaysRewriteArchives = args.getBoolean(Args.ALWAYS_REWRITE_ARCHIVES);
    keepUnrecognizedFilesInArchives = args.getBoolean(Args.KEEP_UNRECOGNIZED_FILES);
    cleanMergePathAfterMerge = true;
    verifyMerge = args.getBoolean("verify-merge");
    skipRename = args.getBoolean(Args.SKIP_RENAME);
    
    datFormat = DatType.forName(args.getString(Args.DAT_FORMAT));
    datPath =  Paths.get(args.getString(Args.DAT_PATH));
    headerPath = args.get(Args.HEADER_PATH) != null ? Paths.get(args.getString(Args.HEADER_PATH)) : null;
    cloneDatPath = args.get(Args.CLONE_PATH) != null ? Paths.get(args.getString(Args.CLONE_PATH)) : null;
    
    List<String> dataPaths = args.getList(Args.DATA_PATH);
    
    dataPath = dataPaths.stream().map(s -> Paths.get(s)).toArray(i -> new Path[i]);
    mergePath = args.get(Args.DEST_PATH) != null ? Paths.get(args.getString(Args.DEST_PATH)) : null;
    
    zonePriority = new BiasSet(/*Location.ITALY,*/ Location.EUROPE, Location.USA, Location.JAPAN);
  }
  
  public Options()
  {
    logLevel = Log.DEBUG;
    
    merge = new MergeOptions();
    verifier = new VerifierOptions();

    multiThreaded = false;
        
    alwaysRewriteArchives = false;
    keepUnrecognizedFilesInArchives = false;
    cleanMergePathAfterMerge = true;
    verifyMerge = false;
    skipRename = true;
    
    datPath = null;
    headerPath = null;
    cloneDatPath = null;
    
    dataPath = new Path[0];
    //dataPath = new Path[] { Paths.get("/Users/jack/Desktop/romset/gb") };

    mergePath = null;
    
    wholeArchivePath = null;
    
    zonePriority = new BiasSet(Location.EUROPE, Location.USA, Location.JAPAN);
  }
  
  public Options(Options other)
  {
    logLevel = other.logLevel;
    
    merge = other.merge;
    verifier = other.verifier;
    
    multiThreaded = other.multiThreaded;
    
    alwaysRewriteArchives = other.alwaysRewriteArchives;
    keepUnrecognizedFilesInArchives = other.keepUnrecognizedFilesInArchives;
    cleanMergePathAfterMerge = other.cleanMergePathAfterMerge;
    verifyMerge = other.verifyMerge;
    skipRename = other.skipRename;
    
    datPath = other.datPath;
    headerPath = other.headerPath;
    cloneDatPath = other.cloneDatPath;
    
    dataPath = Arrays.copyOf(other.dataPath, other.dataPath.length);
    mergePath = other.mergePath;
    wholeArchivePath = other.wholeArchivePath;
    
    zonePriority = other.zonePriority; 
  }
  
  
  //TODO: not correct, if merge in place it should be original path of rom or force 1 data path max
  public Path mergePath() { return merge.forceMergeInPlace ? dataPath[0] : mergePath; }
  public boolean doesMergeInPlace() { return merge.forceMergeInPlace || dataPath.equals(mergePath); } 
  
  public static Options simpleDatLoad(Path path)
  {
    Options options = new Options();
    options.datPath = path;
    return options;
  }
}
