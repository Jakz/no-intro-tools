package com.github.jakz.nit.batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.jakz.nit.DatType;
import com.github.jakz.nit.Operations;
import com.github.jakz.nit.Options;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.set.Feature;
import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.io.archive.HandleSet;
import com.pixbits.lib.io.archive.ScannerOptions;
import com.pixbits.lib.lang.StringUtils;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;

public class BatchOperations
{
  private final static Logger logger = Log.getLogger(BatchOperations.class);
  
  public static Set<GameSet> batchLoadGameSetsFromFolder(BatchOptions boptions, Options options) throws IOException
  {    
    PathMatcher datMatcher = boptions.getPathMatcher();
    FolderScanner scanner = new FolderScanner(datMatcher, false);
    
    Set<Path> paths = scanner.scan(boptions.datFolder);
    
    Map<String, List<GameSet>> sets = new HashMap<>();
    Map<GameSet, Path> pathMapForOptionalDeletion = new HashMap<>();
    
    /* load DATs */
    for (Path cpath : paths)
    {
      Options coptions = new Options(options);
      coptions.datPath = cpath;
      
      /* check if the same file as xmdb exists and use it as clone set in case */
      Path xmdbPath = cpath.getParent().resolve(Paths.get(FileUtils.fileNameWithoutExtension(cpath) + ".xmdb"));
      coptions.cloneDatPath = Files.exists(xmdbPath) ? xmdbPath : null;

      final DatType format = boptions.forcedformat;
      
      try
      {
        GameSet set = Operations.loadGameSet(coptions, format);
        logger.i("Loaded %s (%s)", set.info().getName(), set.info().getVersion());
        sets.computeIfAbsent(set.info().getName(), n -> new ArrayList<>()).add(set);
        pathMapForOptionalDeletion.put(set, cpath);
      }
      catch (Exception e)
      {
        logger.e("Error parsing %s as %s, probably wrong format? (%s)", cpath, format.name, e.getMessage());
      }
    }
    
    logger.i("Found %d DATs for %d systems..", sets.values().stream().mapToInt(List::size).sum(), sets.size());
   
    /* if multiple DATs are found with same name we need a way to keep only most updated one */
    TreeSet<GameSet> finalSets = new TreeSet<>((s1, s2) -> s1.info().getName().compareToIgnoreCase(s2.info().getName()));
    for (Map.Entry<String, List<GameSet>> e : sets.entrySet())
    {
      List<GameSet> setsForPlatform = e.getValue();
      
      if (setsForPlatform.size() > 1)
      {
        logger.w("Multiple DATs found for name %s", e.getKey());
        
        /* let's classify DATs */
        BatchDatClassification classification = boptions.datClassifier.apply(setsForPlatform);
        
        for (GameSet set : classification.revisions)
          logger.i3("  > %s (revision)", set.info().getVersion());
        for (GameSet set : classification.notVersionable)
          logger.i3("  > %s (non versioned)", set.info().getVersion());
        
        if (classification.revisions.isEmpty())
        {
          logger.w("Only non versionable DATs found for %s, choosing a random one", e.getKey());
          finalSets.add(classification.notVersionable.get(0));
        }
        else
        {
          GameSet goodSet = classification.revisions.get(0);
          finalSets.add(goodSet);

          /* delete old DATs if they are versioned and older than most updated one found */
          if (boptions.deleteLessRecentDats)
          {
            Path goodPath = pathMapForOptionalDeletion.get(goodSet);
            for (int i = 1; i < classification.revisions.size(); ++i)
            {
              Path toDeletePath = pathMapForOptionalDeletion.get(classification.revisions.get(i));
              logger.i("Deleting DAT at %s (%s) because it's superseded by %s (%s)", 
                  toDeletePath.toString(),
                  classification.revisions.get(i).info().getVersion(),
                  goodPath.toString(),
                  goodSet.info().getVersion()
              );
              //TODO: add actual deletion
            }
          }
        }
      }
      else
        finalSets.add(setsForPlatform.get(0));    
    }
    
    logger.i("Pruned %d sets from batch loading", sets.values().stream().mapToInt(List::size).sum() - finalSets.size());
    return finalSets;
  }
  
  private static void printStatisticsOnGameSets(Set<GameSet> sets)
  {
    logger.i("Loaded %d game sets:", sets.size());
    
    long uniqueGames = 0L, totalGames = 0L, totalRoms = 0L, totalBytes = 0L;
    
    for (GameSet set : sets)
    {
      set.load();
      
      uniqueGames += set.info().uniqueGameCount();
      totalGames += set.info().gameCount();
      totalRoms += set.info().romCount();
      totalBytes += set.info().sizeInBytes();
      
      String countString = set.hasFeature(Feature.CLONES) ? 
          String.format("%d/%d%d roms/games/unique", set.info().romCount(), set.info().gameCount(), set.info().uniqueGameCount())
          : String.format("%d/%d roms/games", set.info().romCount(), set.info().gameCount());
          
      logger.i("  %s (%s) (%s) (%s)", 
          set.info().getName(), 
          set.info().getVersion(), 
          countString,
          StringUtils.humanReadableByteCount(set.info().sizeInBytes())
      );
    }
    
    logger.i("Total: %d/%d/%d roms/games/unique in %s", totalRoms, totalGames, uniqueGames, StringUtils.humanReadableByteCount(totalBytes));
  }

  private static void printStatisticsOnResults(Set<BatchVerifyResult> results)
  {
    long verified = results.stream().filter(r -> !r.skipped).count();
    logger.i("Verified %d sets: ", results.stream().filter(r -> !r.skipped).count());
    for (BatchVerifyResult r : results)
    {
      if (r.skipped)
        continue;
      
      GameSet set = r.set;
      
      logger.i("  %s (%s): found %d/%d roms (%2.2f%%) %s", 
          set.info().getName(),
          set.info().getVersion(),
          r.getFoundRomCount(),
          set.info().romCount(),
          r.getFoundRomCount()*100.0 / set.info().romCount(),
          r.getFoundRomCount() == set.info().romCount() ? " (complete)" : ""
      );
    }
    
    long skipped = results.size() - verified;
    if (skipped > 0)
    {
      logger.i("Skipped %d sets:", skipped);
      for (BatchVerifyResult r : results)
      {
        if (r.skipped)
        {
          logger.i(" %s (%s)", r.set.info().getName(), r.set.info().getVersion());
        }
      }
    }
  }
  
  private static void printMissingRoms(Set<BatchVerifyResult> results)
  {
    for (BatchVerifyResult r : results)
    {
      GameSet set = r.set;
      if (!r.skipped && r.getFoundRomCount() != set.info().romCount())
      {
        logger.i("Missing %d roms for %s (%s):", set.info().romCount() - r.getFoundRomCount(), set.info().getName(), set.info().getVersion());
        for (Rom rom : r.missingRoms)
        {
          logger.i("  %s (%s)", rom.name, Long.toHexString(rom.crc32));
        }
        
      }
    }
  }
  
  private static BatchVerifyResult scanAndVerifySet(GameSet set, BatchOptions boptions, Options options) throws IOException, NoSuchAlgorithmException
  {
    Path path = boptions.getRomPath(set);
    boolean skipped = true;
    
    if (path == null)
      logger.w("Skipping set %s because no path to scan is specified", set.info().getName());
    else if (!Files.exists(path))
      logger.w("Skipping set %s because path %s doesn't exist", set.info().getName(), path.toString());
    else
    {
      skipped = false;
      logger.i("Scanning entries for set %s" , set.info().getName());
 
      ScannerOptions soptions = new ScannerOptions();          
      HandleSet handles = Operations.scanEntriesForGameSet(set, Collections.singletonList(path), soptions, true);
      
      Operations.verifyGameSet(set, handles, options);
    }
    
    return new BatchVerifyResult(set, path, skipped);
  }

  
  
  public static void batchScanAndVerify(BatchOptions boptions, Options options) throws IOException, NoSuchAlgorithmException
  {
    Set<GameSet> sets = batchLoadGameSetsFromFolder(boptions, options);
    Set<BatchVerifyResult> results = new TreeSet<BatchVerifyResult>((r1, r2) -> r1.set.info().getName().compareToIgnoreCase(r2.set.info().getName()));
    
    printStatisticsOnGameSets(sets);

    for (GameSet set : sets)
    {
      BatchVerifyResult result = scanAndVerifySet(set, boptions, options);
      
      if (result != null)
      {
        result.computeStats();
        results.add(result);
      }
    }
    
    printStatisticsOnResults(results);
    printMissingRoms(results);
  }
}
