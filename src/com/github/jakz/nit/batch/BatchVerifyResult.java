package com.github.jakz.nit.batch;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.jakz.romlib.data.game.GameStatus;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.set.GameSet;

public class BatchVerifyResult
{
  public final boolean skipped;
  public final Path scanPath;
  public final GameSet set;
  
  private final int[] gameCounts;
  private final int[] romCounts;
  
  public final List<Rom> missingRoms;
  
  public BatchVerifyResult(GameSet set, Path scanPath, boolean skipped)
  {
    this.skipped = skipped;
    this.set = set;
    this.scanPath = scanPath;
    
    this.gameCounts = new int[GameStatus.values().length];
    this.romCounts = new int[2];
    
    this.missingRoms = new ArrayList<>();
  }
  
  public void computeStats()
  {
    set.refreshStatus();
    
    set.stream().forEach(game -> {
      ++gameCounts[game.getStatus().ordinal()];
      
      game.stream().forEach(rom -> {
        ++romCounts[rom.isPresent() ? 1 : 0];
        
        if (rom.isMissing())
          missingRoms.add(rom);
      });
    });
  }
  
  public int getGameCountByStatus(GameStatus status)
  {
    return gameCounts[status.ordinal()];
  }
  
  public int getFoundRomCount() { return romCounts[1]; }
  public int getMissingRomCount() { return romCounts[0]; }
}
