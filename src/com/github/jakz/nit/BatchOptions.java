package com.github.jakz.nit;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.github.jakz.romlib.data.set.GameSet;

public class BatchOptions
{
  public static class BatchDatClassification
  {
    List<GameSet> revisions;
    List<GameSet> notVersionable;
    
    BatchDatClassification()
    {
      this.revisions = new ArrayList<GameSet>();
      this.notVersionable = new ArrayList<GameSet>();
    }
  }
  
  
  public final Path datFolder;
  public final DatType forcedformat;
  
  public final Function<List<GameSet>, BatchDatClassification> datClassifier;
  
  public final boolean deleteLessRecentDats;
  
  public PathMatcher getPathMatcher()
  { 
    return FileSystems.getDefault().getPathMatcher("glob:*.{dat,xml}");
  }
  
  public BatchOptions()
  {
    datFolder = Paths.get("dats/");
    forcedformat = DatType.LOGIQX;

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
  }
}
