package com.jack.nit.gui;

import javax.swing.ImageIcon;

public enum Icon
{
  FLAG_AUSTRALIA("flag_australia"),
  FLAG_BRAZIL("flag_brazil"),
  FLAG_CHINA("flag_china"),
  FLAG_DENMARK("flag_denmark"),
  FLAG_EUROPE("flag_europe"),
  FLAG_FINLAND("flag_finland"),
  FLAG_FRANCE("flag_france"),
  FLAG_GERMANY("flag_germany"),
  FLAG_ITALY("flag_italy"),
  FLAG_JAPAN("flag_japan"),
  FLAG_KOREA("flag_korea"),
  FLAG_NETHERLANDS("flag_netherlands"),
  FLAG_NORWAY("flag_norway"),
  FLAG_POLAND("flag_poland"),
  FLAG_PORTUGAL("flag_portugal"),
  FLAG_SPAIN("flag_spain"),
  FLAG_SWEDEN("flag_sweden"),
  FLAG_UNITED_KINGDOM("flag_united_kingdom"),
  FLAG_USA("flag_usa"),
  FLAG_USA_EUROPE("flag_usa_europe"),
  FLAG_JAPAN_USA("flag_japan_usa"),
  FLAG_WORLD("flag_usa"),
  STATUS_ALL("status_all"),
  STATUS_BADLY_NAMED("status_badly_named"),
  STATUS_CORRECT("status_correct"),
  STATUS_NOT_FOUND("status_not_found"),
  FAVORITE("favorite"),
  EDIT("edit"),
  DELETE("delete"),
  ADD("add"),
  ARROW_UP("arrow-up"),
  ARROW_DOWN("arrow-down"),
  ARROW_UP_DOWN("arrow-up-down"),

  SYSTEM_GAME_GEAR("systems/game-gear"),
  SYSTEM_GAMEBOY("systems/gameboy"),
  SYSTEM_GAMEBOY_ADVANCE("systems/gameboy-advance"),
  SYSTEM_GAMEBOY_COLOR("systems/gameboy-color"),
  SYSTEM_NINTENDO_DS("systems/nintendo-ds"),
  
  SYSTEM_NES("systems/nes"),
  SYSTEM_SUPER_NINTENDO("systems/super-nintendo"),
  SYSTEM_NINTENDO_64("systems/nintendo-64"),
  
  SYSTEM_SEGA_MASTER_SYSTEM("systems/sega-master-system"),
  
  SYSTEM_COMMODORE_64("systems/commodore-64"),
  
  SYSTEM_WONDERSWAN("systems/wonderswan"),
  SYSTEM_NEO_GEO_POCKET("systems/neo-geo-pocket"),
  SYSTEM_MISSING("systems/missing"),
  ;
  
  private final String name;
  private ImageIcon icon;
  
  Icon(String name)
  {
    this.name = name;
  }
  
  public ImageIcon getIcon()
  {    
    if (icon == null)
      icon = new ImageIcon(this.getClass().getClassLoader().getResource("com/jack/nit/gui/resources/"+name+".png"));
    
    return icon;
  }
}
