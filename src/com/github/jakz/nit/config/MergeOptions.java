package com.github.jakz.nit.config;

import java.util.Arrays;

import com.github.jakz.nit.Args;
import com.pixbits.lib.io.archive.ArchiveFormat;

import net.sourceforge.argparse4j.inf.Namespace;

public class MergeOptions
{
  public static enum Mode
  {
    UNCOMPRESSED("uncompressed"),
    SINGLE_ARCHIVE_PER_SET("single-archive"),
    SINGLE_ARCHIVE_PER_GAME("archive-by-game"),
    SINGLE_ARCHIVE_PER_CLONE("archive-by-clone"),
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
  
  public final Mode mode;
  public final ArchiveFormat archiveFormat;
  public final boolean useSolidArchives;
  public final int compressionLevel;
  public final boolean forceMergeInPlace;
  public final boolean forceFolderPerGameStructure;
  public final boolean automaticallyMergeClonesForSameNormalizedNames;
  
  public MergeOptions()
  {
    mode = Mode.NO_MERGE;
    archiveFormat = ArchiveFormat._7ZIP;
    useSolidArchives = true;
    compressionLevel = 9;
    forceMergeInPlace = false;
    forceFolderPerGameStructure = false;
    automaticallyMergeClonesForSameNormalizedNames = false;
  }
  
  public MergeOptions(Namespace args)
  {
    if (args.getBoolean("no-merge"))
      mode = MergeOptions.Mode.NO_MERGE;
    else
      mode = MergeOptions.Mode.forName(args.getString(Args.MERGE_MODE));
    
    archiveFormat = ArchiveFormat._7ZIP;
    useSolidArchives = !args.getBoolean(Args.NO_SOLID_ARCHIVES);
    compressionLevel = args.getInt(Args.COMPRESSION_LEVEL);
    forceMergeInPlace = args.getBoolean(Args.IN_PLACE_MERGE);
    forceFolderPerGameStructure = args.getBoolean(Args.FORCE_FOLDER_PER_GAME);
    automaticallyMergeClonesForSameNormalizedNames = args.getBoolean(Args.AUTOMATICALLY_CREATE_CLONES);
  }
  
  public MergeOptions(MergeOptions other)
  {
    this.mode = other.mode;
    this.archiveFormat = other.archiveFormat;
    this.useSolidArchives = other.useSolidArchives;
    this.compressionLevel = other.compressionLevel;
    this.forceMergeInPlace = other.forceMergeInPlace;
    this.forceFolderPerGameStructure = other.forceFolderPerGameStructure;
    this.automaticallyMergeClonesForSameNormalizedNames = other.automaticallyMergeClonesForSameNormalizedNames;
  }
}
