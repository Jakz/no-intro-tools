package com.jack.nit.data;

import java.util.List;

import com.jack.nit.data.xmdb.CloneSet;

public class GameSetStatus
{
  final public GameSet set;
  final public CloneSet clones;
  
  public GameSetStatus(GameSet set, CloneSet clones)
  {
    this.set = set;
    this.clones = clones;
  }
}
