package com.jack.nit.data.xmdb;

import com.jack.nit.data.Game;
import com.jack.nit.data.Rom;

public class GameClone
{
  private final Game[] games;
  private final Game[] biases;
  
  GameClone(Game[] games)
  {
    this.games = games;
    this.biases = new Game[Zone.values().length];
  }
}
