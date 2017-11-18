package com.github.jakz.nit.batch;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.jakz.nit.DatType;
import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.io.archive.VerifierEntry;

public class BatchOptions
{
  public final Path datFolder;
  public final DatType forcedformat;
  
  public final Function<List<GameSet>, BatchDatClassification> datClassifier;
  
  public final Map<String, Function<VerifierEntry, VerifierEntry>> handleTransformers;
  
  public final boolean deleteLessRecentDats;
  
  private SetToPathMapper setToPathMapper;
  
  public final Path expectedSetsList;

  
  public PathMatcher getPathMatcher()
  { 
    return FileSystems.getDefault().getPathMatcher("glob:*.{dat,xml}");
  }
  
  public Path getRomPath(GameSet set)
  {
    return setToPathMapper.getRompath(set);
  }
  
  public BatchOptions()
  {
    datFolder = Paths.get("dats3/");
    forcedformat = DatType.LOGIQX;

    expectedSetsList = Paths.get("dats2/profile.xml");
    
    deleteLessRecentDats = true;

    datClassifier = sets -> {
      BatchDatClassification classification = new BatchDatClassification();
      
      Function<GameSet, Long> getVersion = s -> {
        try
        {
          String version = s.info().getVersion();
          if (version == null) return -1L;
          return Long.parseLong(version.substring(0, 8));
        }
        catch (NumberFormatException|StringIndexOutOfBoundsException e)
        {
          return -1L;
        }
      };
      
      for (GameSet set : sets)
      {
        if (getVersion.apply(set) != -1L)
          classification.revisions.add(set);
        else
          classification.notVersionable.add(set);
      }
 
      Comparator<GameSet> comparator = (s1, s2) -> Long.compare(getVersion.apply(s1), getVersion.apply(s2));
      Collections.sort(classification.revisions, comparator.reversed());
           
      return classification;
    };
    
    Map<String, Path> romPaths = new HashMap<>();
    
    Path base = Paths.get("/Volumes/Vicky/Roms/sets/No Intro/new nointro/");

    setToPathMapper = SetToPathMapper.ofDefaultNamingInFolder(base);
    
    handleTransformers = new HashMap<>();
  }
}
