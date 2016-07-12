package com.jack.nit.creator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.Rom;
import com.jack.nit.data.xmdb.CloneSet;

public class ClrMameProGenerator implements Generator
{
  private final HexBinaryAdapter hexConverter = new HexBinaryAdapter();
  
  @Override
  public void generate(CreatorOptions options, GameSet set) throws IOException
  {
    try (PrintWriter wrt = new PrintWriter(Files.newBufferedWriter(options.destPath)))
    {
      wrt.print("clrmamepro (\n");
      wrt.printf("\tname \"%s\"\n", set.name);
      wrt.printf("\tdescription \"%s\"\n", set.description);
      wrt.printf("\tversion \"%s\"\n", set.version);
      wrt.printf("\tcomment \"%s\"\n", set.comment);
      wrt.print(")\n\n");
      
      set.stream().sorted((g1,g2) -> g1.name.compareToIgnoreCase(g2.name)).forEach(game -> {
        wrt.print("game (\n");
        wrt.printf("\tname \"%s\"\n", game.name);
        wrt.printf("\tdescription \"%s\"\n", game.description);
        
        game.stream().sorted((r1,r2) -> r1.name.compareToIgnoreCase(r2.name)).forEach(rom -> {
          wrt.printf("\trom ( name \"%s\" size %d crc %s md5 %s sha1 %s )\n", rom.name, rom.size, Integer.toHexString((int)rom.crc32 & 0xFFFFFFFF).toUpperCase(), hexConverter.marshal(rom.md5), hexConverter.marshal(rom.sha1));    
        });

        wrt.print(")\n\n");
        
      });
    }
    
    if (set.clones().size() > 0)
    {
      CloneSet clones = set.clones();
    }
  }
}
