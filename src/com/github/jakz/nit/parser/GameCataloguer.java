package com.github.jakz.nit.parser;

import com.github.jakz.nit.data.Game;

@FunctionalInterface
public interface GameCataloguer
{
  public void catalogue(Game game);
}
