package com.jack.nit.emitter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jack.nit.data.Game;
import com.jack.nit.data.GameSet;
import com.jack.nit.data.GameSetInfo;
import com.jack.nit.data.Rom;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.data.xmdb.GameClone;
import com.jack.nit.digest.DigestInfo;
import com.jack.nit.digest.DigestOptions;
import com.jack.nit.digest.Digester;
import com.jack.nit.handles.Archive;
import com.jack.nit.handles.ArchiveHandle;
import com.jack.nit.handles.BinaryHandle;
import com.jack.nit.log.Log;
import com.jack.nit.scanner.FormatUnrecognizedException;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.stream.StreamException;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class GameSetCreator
{
  private final CreatorOptions options;
  private final Digester digester;
  
  private Set<CreatorEntry> entries;
  
  private final List<Game> games;
  private final List<GameClone> clones;
  
  private final AtomicInteger count;
  private float total;
  
  public GameSetCreator(CreatorOptions options)
  {
    this.options = options;
    digester = new Digester(new DigestOptions(options.shouldCalculateCRC(), options.shouldCalculateMD5(), options.shouldCalculateSHA1(), options.multiThreaded));
    
    entries = new HashSet<>();
    
    games = Collections.synchronizedList(new ArrayList<>());
    clones = Collections.synchronizedList(new ArrayList<>());
    
    count = new AtomicInteger();
  }
  
  private abstract class CreatorEntry
  {   
    final Path path;
    CreatorEntry(Path path) { this.path = path; }
  }
  
  private class MultipleEntry extends CreatorEntry
  {
    final List<Rom> roms;
    final List<Game> clones;
    
    MultipleEntry(Path path)
    {
      super(path);
      roms = new ArrayList<>();
      clones = new ArrayList<>();
    }
  }

  private class BinaryEntry extends CreatorEntry
  {
    BinaryEntry(Path path) { super(path); }
    public String toString() { return path.getFileName().toString(); }
  }
  
  private class ArchiveEntry extends MultipleEntry
  {
    ArchiveEntry(Path path)
    {
      super(path);
    }
    
    public String toString() { return path.getFileName().toString(); }
  }
  
  private class FolderAsArchiveEntry extends MultipleEntry
  {
    List<CreatorEntry> entries;
    List<Rom> roms;
    
    FolderAsArchiveEntry(Path path)
    { 
      super(path);
      entries = new ArrayList<>();
      roms = new ArrayList<>();
    }
    
    public String toString() { return path.getFileName().toString() + entries.stream().map(p -> p.toString()).collect(Collectors.joining(", ", " [", "]")); }
  }
  
  private IInArchive openArchive(Path path) throws FormatUnrecognizedException
  {
    try (RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r")))
    {
      IInArchive archive = SevenZip.openInArchive(null, rfile);

      if (archive.getArchiveFormat() == null)
        throw new FormatUnrecognizedException(path, "Archive format unrecognized");
      
      return archive;
    }
    catch (IOException e)
    {
      throw new FormatUnrecognizedException(path, "Archive format unrecognized");
    }
  }
    
  private void prescanFiles() throws IOException
  {    
    Stream<Path> paths = options.sourcePaths.stream();
    
    if (options.multiThreaded)
      paths = paths.parallel();
    
    paths.forEach(StreamException.rethrowConsumer(p -> new FileScanner().scan(p)));
  }
  
  private void analyze() throws IOException
  {
    Stream<CreatorEntry> entries = this.entries.stream();
    
    if (options.multiThreaded)
      entries = entries.parallel();
    
    entries.forEach(StreamException.rethrowConsumer(e -> {
      Log.logger.updateProgress(count.getAndIncrement()/total, e.path.getFileName().toString());
      analyzeEntry(null, e); 
    }));
  }
    
  private void analyzeEntry(MultipleEntry parent, CreatorEntry e) throws IOException, NoSuchAlgorithmException
  {
    /* binary entry, single rom which is a game */
    if (e instanceof BinaryEntry)
    {
      String name = FileUtils.fileNameWithoutExtension(e.path);
      long size = Files.size(e.path);
      BinaryHandle handle = new BinaryHandle(e.path);
      DigestInfo info = digester.digest(handle, handle.getInputStream(), true);
      
      Rom rom = new Rom(e.path.getFileName().toString(), size, info);
      rom.setHandle(handle);
      
      /* if rom has a parent then add it to the parent, otherwise generate a new game */
      if (parent != null)
        parent.roms.add(rom);
      else
      {
        games.add(new Game(name, name, new Rom[] { rom }));
      }
    }
    else if (e instanceof ArchiveEntry)
    {
      try
      {
        ArchiveEntry ae = (ArchiveEntry)e;
        Archive archive = new Archive(e.path, true);
        
        /* if archive have more than 1 file and it's in a subfolder and we're treating folders as archives then skip the file, it should't contain more than 1 file
         * TODO: log error/warning?
         */
        if (archive.size() > 1 && parent != null && options.folderAsArchives)
          return;
        
        Stream<Archive.Item> istream = archive.stream();
        
        if (options.multiThreaded)
          istream = istream.parallel();
        
        istream.filter(item -> Arrays.stream(options.binaryExtensions).anyMatch(ext -> item.path.endsWith(ext)))
        .forEach(StreamException.rethrowConsumer(item -> {
          ArchiveHandle handle = item.handle();
          DigestInfo info = digester.digest(handle, handle.getInputStream(), false);
          Rom rom = new Rom(item.path, item.size, info);

          /* merged mode: each entry in the archive is a clone of the game identifier by the archive itself */
          if (options.mode == CreatorOptions.Mode.merged)
          {
            String gameName = FileUtils.trimExtension(item.path);
            Game game = new Game(gameName, gameName, new Rom[] { rom } );
            
            /* if parent is null then we're in root, this is a single game or part of game set according to mode */
            if (parent == null)
              games.add(game);
            ae.clones.add(game);
          }
          /* multi mode: each entry is a rom file for the same game */
          else if (options.mode == CreatorOptions.Mode.multi)
          {
            ae.roms.add(rom);
          }     
        }));
        
        if (parent == null)
        {
          /* currentClone should contain all the games identifier by the archive */
          if (options.mode == CreatorOptions.Mode.merged && ae.clones.size() > 0)
          {
            clones.add(new GameClone(ae.clones.toArray(new Game[ae.clones.size()])));
          }
          /* if we're in multi mode then all roms have been added to currentRoms for a single game with the name of the archive */
          else if (options.mode == CreatorOptions.Mode.multi && ae.roms.size() > 0)
          {
            String folderName = FileUtils.fileNameWithoutExtension(e.path.getFileName());
            Game game = new Game(folderName, folderName, ae.roms.toArray(new Rom[ae.roms.size()]));
            games.add(game);
          }
        }
        
      }
      catch (FormatUnrecognizedException exc)
      {
        // skip silently
      }
    }
    
    else if (e instanceof FolderAsArchiveEntry)
    {      
      FolderAsArchiveEntry ce = (FolderAsArchiveEntry)e;
      String name = e.path.getFileName().toString();
      ce.entries.forEach(StreamException.rethrowConsumer(p -> {
        analyzeEntry(ce, p);
      }));
      
      if (options.mode == CreatorOptions.Mode.multi && ce.roms.size() > 0)
        games.add(new Game(name, name, ce.roms.toArray(new Rom[ce.roms.size()])));
      else if (ce.clones.size() > 0)
        clones.add(new GameClone(ce.clones.toArray(new Game[ce.clones.size()])));
    }
  }
  
  public GameSet create() throws IOException
  {
    entries.clear();
    games.clear();
    clones.clear();
    count.set(0);
    
    prescanFiles();
    Log.logger.startProgress(String.format("Found %s files to analyze for DAT creation", entries.size()));
    total = entries.size();
    analyze();
    Log.logger.endProgress();
    
    GameSet set = new GameSet(new GameSetInfo(options.name, options.description, options.version, options.comment, options.author), null, games.toArray(new Game[games.size()]));
    set.setClones(new CloneSet(set, clones.toArray(new GameClone[clones.size()])));
    return set;
  }

  public class FileScanner
  {
    private FolderAsArchiveEntry folderArchive;
    private GameSetCreator creator;
    
    public FileScanner()
    {
      folderArchive = null;
      creator = GameSetCreator.this;
    }

    public void scan(Path root) throws IOException
    {
      if (!Files.exists(root))
        throw new FileNotFoundException("unable to find path: "+root.toString());
      else if (Files.isDirectory(root))
        innerScan(root);
    }
    
    private void innerScan(Path folder) throws IOException
    {
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder))
      {
        stream.forEach(StreamException.rethrowConsumer(e ->
        {                
          //boolean isInRoot = folderArchive == null;
          boolean isArchive = GameSetCreator.this.options.archiveMatcher.matches(e.getFileName());
          boolean isBinary = GameSetCreator.this.options.binaryMatcher.matches(e.getFileName());
          boolean isDirectory = Files.isDirectory(e);
                    
          if (isDirectory)
          {
            if (options.folderAsArchives)
            {
              if (folderArchive == null)
              {
                folderArchive = new FolderAsArchiveEntry(e);
                innerScan(e);
                entries.add(folderArchive);
                folderArchive = null;
              }
              else
              {
                /* ignore depth > 1 from relative parent if foldersAsArchives is true because
                 * if folders are treaded archives then they are considered only at root level
                 */             
                return;
              } 
            }
            else
              innerScan(e);
          }
          /* if folderArchive is null then we are in root or folders are not considered archives so just treat as a normal entry */   
          else
          {
            CreatorEntry entry = null;
            
            if (isArchive)
              entry = new ArchiveEntry(e);
            else if (isBinary)
              entry = new BinaryEntry(e);
            
            if (entry != null)
            {
              if (folderArchive != null)
                folderArchive.entries.add(entry);
              else
                entries.add(entry);
            } 
          }
        }));
      }
      catch (AccessDeniedException e)
      {
        // silently kill
      }
    }
  }
  
}
