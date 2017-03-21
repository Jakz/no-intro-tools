package com.github.jakz.nit.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

import com.github.jakz.romlib.data.game.GameClone;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.game.attributes.Attribute;
import com.github.jakz.romlib.data.game.attributes.GameAttributeInterface;
import com.github.jakz.romlib.data.game.attributes.GameInfo;

public class Game implements Iterable<Rom>, GameAttributeInterface
{
  public final GameInfo info;
  
  private final Rom[] roms;
  private GameClone<Game> clone;
  
  public Game(String title, Rom[] roms)
  {
    this.info = new GameInfo();
    this.roms = roms;
    this.clone = null;
    Arrays.stream(this.roms).forEach(r -> r.setGame(this));
    setTitle(title);
  }
  
  public GameInfo info() { return info; }
    
  public boolean isComplete() { return Arrays.stream(roms).allMatch(r -> r.handle() != null); }
  public Rom get(int index) { return roms[index]; }
  public int size() { return roms.length; }
  
  public Iterator<Rom> iterator() { return Arrays.asList(roms).iterator(); }
  public Stream<Rom> stream() { return Arrays.stream(roms); }
  
  public void setClone(GameClone<Game> clone) { this.clone = clone; }
  public GameClone<Game> getClone() { return clone; }   
  
  public boolean hasEquivalentRom(Rom rom)
  {
    return Arrays.stream(roms).anyMatch(irom -> irom.isEquivalent(rom));
  }
  
  public boolean isEquivalent(Game game)
  {
    return Arrays.stream(roms).allMatch(rom -> game.hasEquivalentRom(rom));
  }

  @Override public void setAttribute(Attribute key, Object value) { info.setAttribute(key, value); }
  @Override public <T> T getAttribute(Attribute key) { return info.getAttribute(key); }
  @Override public void setCustomAttribute(Attribute key, Object value) { info.setCustomAttribute(key, value); }
  @Override public boolean hasAttribute(Attribute key) { return info.hasAttribute(key); }
}
