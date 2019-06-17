package com.github.jakz.nit.emitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.github.jakz.romlib.data.set.GameSet;

import static com.pixbits.lib.lang.StringUtils.isEmpty;

public class ClrMameProEmitter implements Emitter
{
  private final HexBinaryAdapter hexConverter = new HexBinaryAdapter();
  
  @Override
  public void generate(CreatorOptions options, GameSet set) throws IOException
  {
    try (PrintWriter wrt = new PrintWriter(Files.newBufferedWriter(options.destPath)))
    {
      wrt.print("clrmamepro (\n");
      if (!isEmpty(set.info().getName()))
        wrt.printf("\tname \"%s\"\n", set.info().getName());
      if (!isEmpty(set.info().getDescription()))
        wrt.printf("\tdescription \"%s\"\n", set.info().getDescription());
      if (!isEmpty(set.info().getVersion()))
        wrt.printf("\tversion \"%s\"\n", set.info().getVersion());
      if (!isEmpty(set.info().getComment()))
        wrt.printf("\tcomment \"%s\"\n", set.info().getComment());
      if (!isEmpty(set.info().getAuthor()))
        wrt.printf("\tauthor \"%s\"\n", set.info().getAuthor());
      wrt.print(")\n\n");
      
      set.stream().sorted((g1,g2) -> g1.getTitle().compareToIgnoreCase(g2.getTitle())).forEach(game -> {
        wrt.print("game (\n");
        wrt.printf("\tname \"%s\"\n", game.getTitle());
        wrt.printf("\tdescription \"%s\"\n", game.getDescription());
        
        game.stream().sorted((r1,r2) -> r1.name.compareToIgnoreCase(r2.name)).forEach(rom -> {
          wrt.printf("\trom ( name \"%s\" size %d crc %s md5 %s sha1 %s )\n", rom.name, rom.size(), Integer.toHexString((int)rom.crc() & 0xFFFFFFFF).toUpperCase(), hexConverter.marshal(rom.md5()), hexConverter.marshal(rom.sha1()));    
        });

        wrt.print(")\n\n");
        
      });
    }
    
    if (set.clones().size() > 0)
    {
      set.clones();
    }
  }
}
