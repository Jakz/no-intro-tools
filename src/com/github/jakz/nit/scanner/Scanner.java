package com.github.jakz.nit.scanner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.github.jakz.nit.Settings;
import com.github.jakz.nit.config.ScannerOptions;
import com.github.jakz.nit.data.GameSet;
import com.github.jakz.nit.exceptions.RomPathNotFoundException;
import com.github.jakz.nit.handles.ArchiveHandle;
import com.github.jakz.nit.handles.BinaryHandle;
import com.github.jakz.nit.handles.MemoryArchive;
import com.github.jakz.nit.handles.NestedArchiveHandle;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.functional.StreamException;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Scanner
{
  private static final Logger logger = Log.getLogger(Scanner.class);
  
  final public static PathMatcher archiveMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{zip,rar,7z}");
  
  final private GameSet set;
  final private ScannerOptions options;
  
  public Scanner(GameSet set, ScannerOptions options)
  {
    this.set = set;
    this.options = options;
  }
      
  private IInArchive openArchive(Path path, boolean keepOpen) throws FormatUnrecognizedException
  {
    try
    {
      RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r"));
      ArchiveFormat format = Settings.guessFormatForFilename(path.getFileName().toString());
      
      IInArchive archive = SevenZip.openInArchive(format, rfile);

      if (archive.getArchiveFormat() == null)
        throw new FormatUnrecognizedException(path, "Archive format unrecognized");
      
      if (!keepOpen)
        rfile.close();
      return archive;
    }
    catch (IOException e)
    {
      throw new FormatUnrecognizedException(path, "Archive format unrecognized");
    }
  }
  
  private Set<Path> computeFileList() throws IOException
  {
    Set<Path> files = new TreeSet<>();
    
    final FolderScanner scanner = new FolderScanner(options.includeSubfolders);
    
    Stream<Path> paths = options.paths.stream();
    
    if (options.multithreaded)
      paths = paths.parallel();
    
    paths.map(StreamException.rethrowFunction(p -> {
      try 
      {
        if (Files.isDirectory(p))
          return scanner.scan(p);
        else
          return Collections.singleton(p);
      }
      catch (FileNotFoundException e)
      {
        throw new RomPathNotFoundException(p);
      }
        
    })).forEach(files::addAll);
    
    logger.i1("found %d files to scan in %d paths", files.size(), options.paths.size());
    
    return files;
  }

  private List<List<NestedArchiveHandle>> scanNestedArchives(Map<Path, Set<Integer>> archives) throws IOException
  {
    List<List<NestedArchiveHandle>> handles = new ArrayList<>();
    
    archives.entrySet().stream().forEach(StreamException.rethrowConsumer(entry -> {
      Path archivePath = entry.getKey();
      Set<Integer> indices = entry.getValue();
      
      IInArchive archive = openArchive(archivePath, true);
      
      for (int index : indices)
      {
        /* extract archive in memory */
        int size = (int)(long)archive.getProperty(index, PropID.SIZE);
        String fileName = (String)archive.getProperty(index, PropID.PATH);

        MemoryArchive memoryArchive = MemoryArchive.load(archive, index, size);

        ArchiveFormat format = Settings.guessFormatForFilename(fileName);
        IInArchive marchive = memoryArchive.open(format);
        
        int itemCount = marchive.getNumberOfItems();
        
        List<NestedArchiveHandle> handlesForArchive = new ArrayList<>();      
  
        for (int i = 0; i < itemCount; ++i)
        {
          ArchiveEntryData data = scanArchive(marchive, i, archivePath, null, null);
          if (data != null)
          { 
            logger.i3("Found nested entry in memory nested inside %s: %s", fileName, data.fileName);
            
            handlesForArchive.add(new NestedArchiveHandle(archivePath, archive.getArchiveFormat(), fileName, index, 
                marchive.getArchiveFormat(), data.fileName, i, data.size, data.compressedSize, data.crc));
          }
        }
        
        marchive.close();
        
        if (!handlesForArchive.isEmpty())
          handles.add(handlesForArchive);
      }
      
      archive.close();
      

    }));
    
    return handles;
  }
  
  private class ArchiveEntryData
  {
    final String fileName;
    final long compressedSize;
    final long size;
    final long crc;
    
    ArchiveEntryData(String fileName, long size, long compressedSize, long crc)
    {
      this.fileName = fileName;
      this.size = size;
      this.compressedSize = compressedSize;
      this.crc = crc;
    }
  }
  
  public ArchiveEntryData scanArchive(IInArchive archive, int i, Path path, Map<Path,Set<Integer>> nested, Set<String> skipped) throws IOException
  {
    long size = (long)archive.getProperty(i, PropID.SIZE);
    Long lcompressedSize = (Long)archive.getProperty(i, PropID.PACKED_SIZE);
    long compressedSize = lcompressedSize != null ? lcompressedSize : -1;
    String fileName = (String)archive.getProperty(i, PropID.PATH);
    
    Boolean isFolder = (Boolean)archive.getProperty(i, PropID.IS_FOLDER);
    
    if (isFolder != null && isFolder)
      return null;
    
    /* if file ends with archive extension */
    if (nested != null && Arrays.asList(Settings.archiveExtensions).stream().anyMatch(f -> fileName.endsWith("."+f.extension)))
    {                
      logger.i3("Found a nested archive inside %s: %s ", path.getFileName(), FileUtils.lastPathComponent(fileName));
      nested.computeIfAbsent(path, p -> new TreeSet<>()).add(i);
    }
    else
    {
      /* if header is null then we can compute crc and check size to filter out elements */
      if (set.header == null)
      {           
        //System.out.println("PATH: "+fileName);
        
        long crc = Integer.toUnsignedLong((Integer)archive.getProperty(i, PropID.CRC));
        
        /* if rom has a size valid for the current set or we are not verifying size match */
        if (set.cache().isValidSize(size) || !options.discardUnknownSizes)
          return new ArchiveEntryData(fileName, size, compressedSize, crc); 
        /* otherwise skip the entry */
        else if (skipped != null)
          skipped.add(fileName+" in "+path.getFileName());
      }
      /* otherwise we're out of luck we must delay the checks */
      else
      {
        return new ArchiveEntryData(fileName, size, compressedSize, -1); 
      }
    }
    
    return null;
  }

  public RomHandleSet computeHandles() throws IOException
  {
    Set<Path> paths = computeFileList();
    
    Set<Path> faultyArchives = new HashSet<>();
    Set<String> skipped = new HashSet<>();
    
    logger.startProgress(Log.INFO2, "Finding files...");
    final float count = paths.size();
    final AtomicInteger current = new AtomicInteger(0);
    
    List<BinaryHandle> binaryHandles = new ArrayList<>();
    List<ArchiveHandle> archiveHandles = new ArrayList<>();
    Map<Path, Set<Integer>> nestedArchiveHandles = new HashMap<>();

    paths.stream().forEach(StreamException.rethrowConsumer(path -> {
      logger.updateProgress(current.getAndIncrement() / count, "");
      
      boolean shouldBeArchive = archiveMatcher.matches(path.getFileName());
            
      if (shouldBeArchive)
      {      
        try (IInArchive archive = openArchive(path, false))
        {
          int itemCount = archive.getNumberOfItems();
          
          if (true)
          {   
            for (int i = 0; i < itemCount; ++i)
            {
              /* TODO: check extension of file? */
              ArchiveEntryData data = scanArchive(archive, i, path, nestedArchiveHandles, skipped);
              if (data != null)
                archiveHandles.add(new ArchiveHandle(path, archive.getArchiveFormat(), data.fileName, i, data.size, data.compressedSize, data.crc));              
            }
          }
        }
        catch (FormatUnrecognizedException e)
        {
          faultyArchives.add(path);
        }
      }
      else
      {
        /* if size of the file is compatible with the romset or if set has special rules add it to potential roms */
        if (set.header != null || set.cache().isValidSize(Files.size(path)) || !options.discardUnknownSizes)
          binaryHandles.add(new BinaryHandle(path));
        else
          skipped.add(path.getFileName().toString());    
      }    
    }));
    
    logger.endProgress();
    
    List<List<NestedArchiveHandle>> nestedHandles = scanNestedArchives(nestedArchiveHandles);
    
    faultyArchives.forEach(p -> logger.w("File "+p.getFileName()+" is not a valid archive."));

    logger.i1("Found %d potential matches (%d binary, %d inside archives, %d nested inside archives).", 
        binaryHandles.size()+archiveHandles.size()+nestedHandles.size(), binaryHandles.size(), archiveHandles.size(), nestedHandles.size());
    
    if (skipped.size() > 0)
      logger.i1("Skipped %d entries:", skipped.size());
    
    skipped.forEach(s -> logger.i3("> %s", s));

    return new RomHandleSet(binaryHandles, archiveHandles, nestedHandles);
  }
}
