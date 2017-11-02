package com.github.jakz.nit.batch;

import java.util.ArrayList;
import java.util.List;

import com.github.jakz.romlib.data.set.GameSet;

public class BatchDatClassification
{
  public List<GameSet> revisions;
  public List<GameSet> notVersionable;
  
  BatchDatClassification()
  {
    this.revisions = new ArrayList<GameSet>();
    this.notVersionable = new ArrayList<GameSet>();
  }
}