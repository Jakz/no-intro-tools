package com.github.jakz.nit.batch;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.xml.sax.SAXException;

import com.github.jakz.nit.DatType;
import com.github.jakz.nit.Operations;
import com.github.jakz.nit.Options;
import com.github.jakz.romlib.data.game.Rom;
import com.github.jakz.romlib.data.set.Feature;
import com.github.jakz.romlib.data.set.GameSet;
import com.github.jakz.romlib.data.set.GameSetInfo;
import com.github.jakz.romlib.parsers.ClrMameProProfileParser;
import com.github.jakz.romlib.parsers.LogiqxXMLHandler;
import com.github.jakz.romlib.parsers.LogiqxXMLHandler.Data;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.io.archive.HandleSet;
import com.pixbits.lib.io.archive.ScannerOptions;
import com.pixbits.lib.io.archive.VerifierEntry;
import com.pixbits.lib.io.xml.XMLParser;
import com.pixbits.lib.lang.StringUtils;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;

public class BatchOperations
{
  private final static Logger logger = Log.getLogger(BatchOperations.class);
  
  public static Set<GameSetInfo> loadExpectedGameSetList(BatchOptions boptions, Options options) throws IOException, SAXException
  {
    if (boptions.expectedSetsList != null)
    {
      ClrMameProProfileParser xparser = new ClrMameProProfileParser();
      XMLParser<Set<GameSetInfo>> parser = new XMLParser<>(xparser);
      parser.load(boptions.expectedSetsList);
      return xparser.get();
    }
    else
      return Collections.emptySet();
  }
  
  public static Set<GameSet> batchLoadGameSetsFromFolder(BatchOptions boptions, Options options) throws IOException
  {    
    PathMatcher datMatcher = boptions.getPathMatcher();
    FolderScanner scanner = new FolderScanner(datMatcher, false);
    
    Set<Path> paths = scanner.scan(boptions.datFolder);
    
    Map<String, List<GameSet>> sets = new HashMap<>();
    Map<GameSet, Path> pathMapForOptionalDeletion = new HashMap<>();
    
    /* load DATs */
    for (Path cpath : paths)
    {
      Options coptions = new Options(options);
      coptions.datPath = cpath;
      
      /* check if the same file as xmdb exists and use it as clone set in case */
      Path xmdbPath = cpath.getParent().resolve(Paths.get(FileUtils.fileNameWithoutExtension(cpath) + ".xmdb"));
      coptions.cloneDatPath = Files.exists(xmdbPath) ? xmdbPath : null;

      final DatType format = boptions.forcedformat;
      
      try
      {
        GameSet set = Operations.loadGameSet(coptions, format);
        logger.i("Loaded %s (%s)", set.info().getName(), set.info().getVersion());
        sets.computeIfAbsent(set.info().getName(), n -> new ArrayList<>()).add(set);
        pathMapForOptionalDeletion.put(set, cpath);
      }
      catch (Exception e)
      {
        logger.e("Error parsing %s as %s, probably wrong format? (%s)", cpath, format.name, e.getMessage());
      }
    }
    
    logger.i("Found %d DATs for %d systems..", sets.values().stream().mapToInt(List::size).sum(), sets.size());
   
    /* if multiple DATs are found with same name we need a way to keep only most updated one */
    TreeSet<GameSet> finalSets = new TreeSet<>((s1, s2) -> s1.info().getName().compareToIgnoreCase(s2.info().getName()));
    for (Map.Entry<String, List<GameSet>> e : sets.entrySet())
    {
      List<GameSet> setsForPlatform = e.getValue();
      
      if (setsForPlatform.size() > 1)
      {
        logger.w("Multiple DATs found for name %s", e.getKey());
        
        /* let's classify DATs */
        BatchDatClassification classification = boptions.datClassifier.apply(setsForPlatform);
        
        for (GameSet set : classification.revisions)
          logger.i3("  > %s (revision)", set.info().getVersion());
        for (GameSet set : classification.notVersionable)
          logger.i3("  > %s (non versioned)", set.info().getVersion());
        
        if (classification.revisions.isEmpty())
        {
          logger.w("Only non versionable DATs found for %s, choosing a random one", e.getKey());
          finalSets.add(classification.notVersionable.get(0));
        }
        else
        {
          GameSet goodSet = classification.revisions.get(0);
          finalSets.add(goodSet);

          /* delete old DATs if they are versioned and older than most updated one found */
          if (boptions.deleteLessRecentDats)
          {
            Path goodPath = pathMapForOptionalDeletion.get(goodSet);
            for (int i = 1; i < classification.revisions.size(); ++i)
            {
              Path toDeletePath = pathMapForOptionalDeletion.get(classification.revisions.get(i));
              logger.i("Deleting DAT at %s (%s) because it's superseded by %s (%s)", 
                  toDeletePath.toString(),
                  classification.revisions.get(i).info().getVersion(),
                  goodPath.toString(),
                  goodSet.info().getVersion()
              );
              Files.delete(toDeletePath);
              
              Path toDeleteClonePath = toDeletePath.getParent().resolve(FileUtils.fileNameWithoutExtension(toDeletePath) + ".xmdb");
              
              if (Files.exists(toDeleteClonePath))
                Files.delete(toDeleteClonePath);
            }
          }
        }
      }
      else
        finalSets.add(setsForPlatform.get(0));    
    }
    
    logger.i("Pruned %d sets from batch loading", sets.values().stream().mapToInt(List::size).sum() - finalSets.size());
    return finalSets;
  }
  
  private static void printStatisticsOnGameSets(Set<GameSet> sets, Set<GameSetInfo> expected)
  {
    logger.i("Loaded %d game sets:", sets.size());
    
    long uniqueGames = 0L, totalGames = 0L, totalRoms = 0L, totalBytes = 0L;
    
    boolean hasExpectedMap = expected != null && !expected.isEmpty();
    Map<String, String> versionMap = new TreeMap<>();
    
    if (hasExpectedMap)
      expected.forEach(g -> versionMap.put(g.getName(), g.getVersion()));
    
    for (GameSet set : sets)
    {
      set.load();
      
      uniqueGames += set.info().uniqueGameCount();
      totalGames += set.info().gameCount();
      totalRoms += set.info().romCount();
      totalBytes += set.info().sizeInBytes();
      
      String countString = set.hasFeature(Feature.CLONES) ? 
          String.format("%d/%d%d roms/games/unique", set.info().romCount(), set.info().gameCount(), set.info().uniqueGameCount())
          : String.format("%d/%d roms/games", set.info().romCount(), set.info().gameCount());
          
      boolean isUpdated = !hasExpectedMap || 
          (versionMap.get(set.info().getName()) != null && versionMap.get(set.info().getName()).equals(set.info().getVersion()));
          
      logger.i("  %s (%s) (%s) (%s)%s", 
          set.info().getName(), 
          set.info().getVersion(), 
          countString,
          StringUtils.humanReadableByteCount(set.info().sizeInBytes()),
          isUpdated ? "" : " (outdated)"
      );
    }
    
    logger.i("Total: %d/%d/%d roms/games/unique in %s", totalRoms, totalGames, uniqueGames, StringUtils.humanReadableByteCount(totalBytes));
    
    if (hasExpectedMap)
    {
      final Map<String, GameSet> byNameMap = sets.stream().collect(Collectors.toMap(s -> s.info().getName(), s -> s, (u,v) -> u, TreeMap::new));

      {
        long outdatedCount = sets.stream()
            .filter(s -> versionMap.containsKey(s.info().getName()))
            .filter(s -> !s.info().getVersion().equals(versionMap.get(s.info().getName())))
            .count();
        
        logger.i("Found %d outdated sets:", outdatedCount);

        for (GameSet set : sets)
        {
          String version = versionMap.get(set.info().getName());
          
          if (version != null && !version.equals(set.info().getVersion()))
            logger.i("  %s %s (last: %s)", set.info().getName(), set.info().getVersion(), version);        
        }
      }
      
      
      {
        long missingCount = expected.stream().filter(s -> !byNameMap.containsKey(s.getName())).count();
        
        logger.i("Missing %d sets from expected sets:", missingCount);
        
        for (GameSetInfo set : expected)
        {
          if (!byNameMap.containsKey(set.getName()))
            logger.i("  %s (%s)", set.getName(), set.getVersion());
        }
      }
    }
  }

  private static void printStatisticsOnResults(Set<BatchVerifyResult> results)
  {    
    long verified = results.stream().filter(r -> !r.skipped).count();
    logger.i("Verified %d sets: ", results.stream().filter(r -> !r.skipped).count());
    for (BatchVerifyResult r : results)
    {
      if (r.skipped)
        continue;
      
      GameSet set = r.set;
      
      logger.i("  %s (%s): found %d/%d roms (%2.2f%%) %s", 
          set.info().getName(),
          set.info().getVersion(),
          r.getFoundRomCount(),
          set.info().romCount(),
          r.getFoundRomCount()*100.0 / set.info().romCount(),
          r.getFoundRomCount() == set.info().romCount() ? " (complete)" : ""
      );
    }
    
    long skipped = results.size() - verified;
    if (skipped > 0)
    {
      logger.i("Skipped %d sets:", skipped);
      for (BatchVerifyResult r : results)
      {
        if (r.skipped)
        {
          logger.i(" %s (%s)", r.set.info().getName(), r.set.info().getVersion());
        }
      }
    }
  }
  
  private static void printMissingRoms(Set<BatchVerifyResult> results)
  {
    for (BatchVerifyResult r : results)
    {
      GameSet set = r.set;
      if (!r.skipped && r.getFoundRomCount() != set.info().romCount())
      {
        logger.i("Missing %d roms for %s (%s):", set.info().romCount() - r.getFoundRomCount(), set.info().getName(), set.info().getVersion());
        for (Rom rom : r.missingRoms)
        {
          logger.i("  %s (%s)", rom.name, Long.toHexString(rom.crc()));
        }
        
      }
    }
  }
  
  private static class ASCIITablePrinter
  {
    private final PrintWriter wrt;
    
    private enum Padding
    {
      LEFT,
      RIGHT,
      CENTER
    };
    
    private class ColumnSpec
    {
      String name;
      int width;
      int margin;
      
      Padding titlePadding;
      Padding rowPadding;
    }
    
    private List<ColumnSpec> columns;
    private List<List<String>> rows;
    
    public ASCIITablePrinter(Writer wrt)
    { 
      columns = new ArrayList<>();
      rows = new ArrayList<>();
      
      this.wrt = new PrintWriter(wrt);
    }
   
    private void recomputeWidths()
    {
      final int cols = columns.size();
      
      for (int i = 0; i < cols; ++i)
      {
        int j = i;
               
        Stream<List<String>> measurer = Stream.concat(Stream.of(columns.stream().map(c -> c.name).collect(Collectors.toList())), rows.stream());
        columns.get(j).width = measurer.map(row -> row.get(j)).mapToInt(String::length).max().getAsInt();
      }
    }
    
    public void addRow(List<String> row)
    {
      rows.add(row);
    }
    
    public void addColumn(String... names)
    {
      for (String name : names)
      {
        ColumnSpec column = new ColumnSpec();
        column.name = name;
        column.margin = 1;
        column.titlePadding = Padding.LEFT;
        column.rowPadding = Padding.RIGHT;
        columns.add(column);
      }
    }
    
    public void setRowPadding(Padding... paddings)
    {
      if (paddings.length > columns.size())
        throw new IndexOutOfBoundsException("column index is over total amount of columns");
      
      for (int i = 0; i < paddings.length; ++i)
        columns.get(i).rowPadding = paddings[i];
    }
    
    public void clear()
    {
      columns.clear();
      rows.clear();
    }
    
    public void printSeparator()
    {
      wrt.write('+');
      for (int i = 0; i < columns.size(); ++i)
      {
        ColumnSpec col = columns.get(i);
        
        for (int j = 0; j < col.width + col.margin*2; ++j)
          wrt.write("-");
        wrt.write('+');
      }
      wrt.write('\n');
    }
    
    final private Function<Integer, String> rightPadder = i -> "%1$" + i + "s";
    final private Function<Integer, String> leftPadder = i -> "%1$-" + i + "s";
    
    private void pad(int width) { for (int m = 0; m < width; ++m) wrt.write(" "); }
    private void ln() { wrt.write("\n"); }

    
    public void printTableRow(List<String> data, boolean isHeader) throws IOException
    {
      
      
      wrt.write("|");
      for (int c = 0; c < columns.size(); ++c)
      {
        final ColumnSpec column = columns.get(c);
        final Padding padding = isHeader ? column.titlePadding : column.rowPadding;
        final int margin = column.margin;
        final int width = column.width;

        pad(margin);
        
        if (padding != Padding.CENTER)
          wrt.write(String.format(padding == Padding.LEFT ? leftPadder.apply(width) : rightPadder.apply(width), data.get(c)));
        else
        {
          int leftOver = width - data.get(c).length();
          int leftPad = leftOver / 2 + (leftOver % 2 != 0 ? 1 : 0);
          int rightPad = leftOver / 2;
          pad(leftPad);
          wrt.write(data.get(c));
          pad(rightPad);
        }
        
        pad(margin);
        
        wrt.write("|");
      }
      ln();
    }
    
    public void printTable() throws IOException
    {
      recomputeWidths();
      printSeparator();
      printTableRow(columns.stream().map(c -> c.name).collect(Collectors.toList()), true);
      printSeparator();
      for (List<String> row : rows)
        printTableRow(row, false);
      printSeparator();
    }
  }
  
  private static class StatsWriter
  {
    private final PrintWriter wrt;
    
    StatsWriter(Writer writer)
    {
      this.wrt = new PrintWriter(writer);
    }

    void write(String s, Object... args) throws IOException
    {
      wrt.write(String.format(s, args));
    }
    
    void writeln(String s, Object... args) throws IOException
    {
      wrt.write(String.format(s, args)+"\n");
    }
    
    void ln() throws IOException { wrt.write("\n"); }
  }
  
  private static void saveStatisticsOnFile(Set<GameSet> sets, Set<GameSetInfo> expected, Set<BatchVerifyResult> results, Path path) throws IOException
  {
    try (BufferedWriter wrt = Files.newBufferedWriter(path))
    {
      StatsWriter w = new StatsWriter(wrt);
      ASCIITablePrinter tablePrinter = new ASCIITablePrinter(wrt);
      
      w.writeln("Statistics for scan done on "+DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(ZonedDateTime.now()));

      w.writeln("\n\nSets: %d found, %d expected, %d missing, %d additional\n", sets.size(), expected.size(), 0, 0);
      
      tablePrinter.clear();
      
      tablePrinter.addColumn("NAME", "VERSION", "SIZE", "UNIQUE", "GAMES", "ROMS");
      tablePrinter.setRowPadding(
          ASCIITablePrinter.Padding.LEFT, 
          ASCIITablePrinter.Padding.LEFT, 
          ASCIITablePrinter.Padding.RIGHT,
          ASCIITablePrinter.Padding.RIGHT,
          ASCIITablePrinter.Padding.RIGHT,
          ASCIITablePrinter.Padding.RIGHT
      );
      
      sets.stream().map(set -> Arrays.asList(new String[] {
          set.info().getName(),
          set.info().getVersion(),
          StringUtils.humanReadableByteCount(set.info().sizeInBytes()),
          Integer.toString(set.info().uniqueGameCount()),
          Integer.toString(set.info().gameCount()),
          Integer.toString(set.info().romCount())
      })).forEach(tablePrinter::addRow);
      
      tablePrinter.printTable();
      

      /*for (List<String> row : table)
      {
        w.write("|");
        for (int c = 0; c < cols; ++c)
        {
          w.write(" %1$-" + (columnWidth[c]-2) +"s |" , row.get(c));
        }
        w.ln();
      }*/
    }
  }
  
  private static BatchVerifyResult scanAndVerifySet(GameSet set, BatchOptions boptions, Options options) throws IOException, NoSuchAlgorithmException
  {
    Path path = boptions.getRomPath(set);
    boolean skipped = true;
    
    if (path == null)
      logger.w("Skipping set %s because no path to scan is specified", set.info().getName());
    else if (!Files.exists(path))
      logger.w("Skipping set %s because path %s doesn't exist", set.info().getName(), path.toString());
    else
    {
      skipped = false;
      logger.i("Scanning entries for set %s" , set.info().getName());
      
      Function<VerifierEntry, ? extends VerifierEntry> transformer = boptions.handleTransformers.get(set.info().getName());
      options.verifier.setTransformer(transformer);
 
      ScannerOptions soptions = new ScannerOptions();          
      HandleSet handles = Operations.scanEntriesForGameSet(set, Collections.singletonList(path), soptions, transformer == null);
      
      Operations.verifyGameSet(set, handles, options);
    }
    
    return new BatchVerifyResult(set, path, skipped);
  }

  
  
  public static Set<GameSet> batchScanAndVerify(BatchOptions boptions, Options options) throws IOException, NoSuchAlgorithmException, SAXException
  {
    Set<GameSet> sets = batchLoadGameSetsFromFolder(boptions, options);
    Set<BatchVerifyResult> results = new TreeSet<BatchVerifyResult>((r1, r2) -> r1.set.info().getName().compareToIgnoreCase(r2.set.info().getName()));
    
    Set<GameSetInfo> expected = loadExpectedGameSetList(boptions, options);

    printStatisticsOnGameSets(sets, expected);

    for (GameSet set : sets)
    {
      BatchVerifyResult result = scanAndVerifySet(set, boptions, new Options(options));
      
      if (result != null)
      {
        result.computeStats();
        results.add(result);
      }
    }
    
    printStatisticsOnResults(results);
    printMissingRoms(results);
    
    saveStatisticsOnFile(sets, expected, results, boptions.datFolder.resolve("statistics.txt"));
        
    return sets;
  }
}
