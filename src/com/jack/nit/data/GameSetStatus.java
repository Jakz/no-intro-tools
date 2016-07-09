package com.jack.nit.data;

import java.util.List;

import com.jack.nit.data.xmdb.CloneSet;

public class GameSetStatus
{
  final public GameSet set;
  final public CloneSet clones;
  final public RomFoundReference[] found;
  
  public GameSetStatus(GameSet set, CloneSet clones, RomFoundReference[] found)
  {
    this.set = set;
    this.clones = clones;
    this.found = found;
  }
}
