package com.github.jakz.nit.scanner;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.jakz.nit.config.ScannerOptions;
import com.github.jakz.nit.data.GameSet;
import com.pixbits.lib.io.FileUtils;
import com.pixbits.lib.io.FolderScanner;
import com.pixbits.lib.io.archive.ArchiveFormat;
import com.pixbits.lib.io.archive.FormatUnrecognizedException;
import com.pixbits.lib.io.archive.handles.ArchiveHandle;
import com.pixbits.lib.io.archive.handles.BinaryHandle;
import com.pixbits.lib.io.archive.handles.MemoryArchive;
import com.pixbits.lib.io.archive.handles.NestedArchiveHandle;
import com.pixbits.lib.log.Log;
import com.pixbits.lib.log.Logger;
import com.pixbits.lib.log.ProgressLogger;
import com.pixbits.lib.functional.StreamException;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Scanner
{
  private static final Logger logger = Log.getLogger(Scanner.class);
  private ProgressLogger progressLogger = Log.getProgressLogger(Scanner.class);
  
  final public static PathMatcher archiveMatcher = ArchiveFormat.getReadableMatcher();
  
  //final private GameSet set;
  final private ScannerOptions options;
  
  public void setProgressLogger(ProgressLogger logger) { this.progressLogger = logger; }
  
  public Scanner(ScannerOptions options)
  {
    this.options = options;
  }
      
  private IInArchive openArchive(Path path, boolean keepOpen) throws FormatUnrecognizedException
  {
    try
    {
      RandomAccessFileInStream rfile = new RandomAccessFileInStream(new RandomAccessFile(path.toFile(), "r"));
      ArchiveFormat format = ArchiveFormat.guessFormat(path);
      
      IInArchive archive = SevenZip.openInArchive(format.nativeFormat, rfile);

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

        ArchiveFormat format = ArchiveFormat.guessFormat(fileName);
        IInArchive marchive = memoryArchive.open(format.nativeFormat);
        
        int itemCount = marchive.getNumberOfItems();
        
        List<NestedArchiveHandle> handlesForArchive = new ArrayList<>();      
  
        for (int i = 0; i < itemCount; ++i)
        {
          ScannerEntry data = scanArchive(marchive, i, archivePath, null, null);
          if (data != null)
          { 
            logger.i3("Found nested entry in memory nested inside %s: %s", fileName, data.fileName);
            
            handlesForArchive.add(new NestedArchiveHandle(archivePath, ArchiveFormat.formatForNative(archive.getArchiveFormat()), fileName, index, 
                ArchiveFormat.formatForNative(marchive.getArchiveFormat()), data.fileName, i, data.size, data.compressedSize, data.crc));
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
  
  public ScannerEntry scanArchive(IInArchive archive, int i, Path path, Map<Path,Set<Integer>> nested, Set<String> skipped) throws IOException
  {
    long size = (long)archive.getProperty(i, PropID.SIZE);
    Long lcompressedSize = (Long)archive.getProperty(i, PropID.PACKED_SIZE);
    long compressedSize = lcompressedSize != null ? lcompressedSize : -1;
    String fileName = (String)archive.getProperty(i, PropID.PATH);
    
    Boolean isFolder = (Boolean)archive.getProperty(i, PropID.IS_FOLDER);
    
    if (isFolder != null && isFolder)
      return null;
    
    /* if file ends with archive extension */
    if (nested != null && ArchiveFormat.guessFormat(fileName) != null)
    {                
      // TODO: if archived file has archive extension but it's binary then it's skipped, maybe move check to outside condition
      if (options.scanNestedArchives)
      {
        logger.i3("Found a nested archive inside %s: %s ", path.getFileName(), FileUtils.lastPathComponent(fileName));
        nested.computeIfAbsent(path, p -> new TreeSet<>()).add(i);
      }
    }
    else
    {
      /* if crc is considered valid then we can get it and check size to filter out elements */
      long crc = options.assumeCRCisCorrect ? Integer.toUnsignedLong((Integer)archive.getProperty(i, PropID.CRC)) : -1;
      ScannerEntry entry = new ScannerEntry(fileName, size, compressedSize, crc);;
      
      /* if predicate tells that entry should be skipped */
      if (options.shouldSkip.test(entry))
      {
        if (skipped != null)
          skipped.add(fileName + " in " + path.getFileName());
      }
      /* otherwise return the entry */
      else
        return entry;
    }
    
    return null;
  }

  public HandleSet computeHandles(List<Path> spaths) throws IOException
  {
    FolderScanner folderScanner = new FolderScanner(options.includeSubfolders);
    
    Set<Path> paths = folderScanner.scan(spaths);
    
    progressLogger.startProgress(Log.INFO2, "Finding files...");
    final float count = paths.size();
    final AtomicInteger current = new AtomicInteger(0);
        
    List<BinaryHandle> binaryHandles = new ArrayList<>();
    List<ArchiveHandle> archiveHandles = new ArrayList<>();
    Map<Path, Set<Integer>> nestedArchiveHandles = new HashMap<>();
    Set<Path> faultyArchives = new HashSet<>();
    Set<String> skipped = new HashSet<>();
    
    paths.stream().forEach(StreamException.rethrowConsumer(path -> {
      progressLogger.updateProgress(current.getAndIncrement() / count, "");
      
      boolean shouldBeArchive = archiveMatcher.matches(path.getFileName());
            
      if (options.scanArchives && shouldBeArchive)
      {      
        try (IInArchive archive = openArchive(path, false))
        {
          int itemCount = archive.getNumberOfItems();
          
          if (true)
          {   
            for (int i = 0; i < itemCount; ++i)
            {
              /* TODO: check extension of file? */
              ScannerEntry data = scanArchive(archive, i, path, nestedArchiveHandles, skipped);
              if (data != null)
                archiveHandles.add(new ArchiveHandle(path, ArchiveFormat.formatForNative(archive.getArchiveFormat()), data.fileName, i, data.size, data.compressedSize, data.crc));              
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
        long size = Files.size(path);
        ScannerEntry entry = new ScannerEntry(path.getFileName().toString(), size, size, -1);
       
        /* check if file should be skipped according to predicate */
        if (options.shouldSkip.test(entry))
          skipped.add(path.getFileName().toString());    
        else
          binaryHandles.add(new BinaryHandle(path));   
      }    
    }));
    
    progressLogger.endProgress();
    
    List<List<NestedArchiveHandle>> nestedHandles = scanNestedArchives(nestedArchiveHandles);
    
    faultyArchives.forEach(p -> logger.w("File "+p.getFileName()+" is not a valid archive."));

    logger.i1("Found %d potential matches (%d binary, %d inside archives, %d nested inside archives).", 
        binaryHandles.size()+archiveHandles.size()+nestedHandles.size(), binaryHandles.size(), archiveHandles.size(), nestedHandles.size());
    
    if (skipped.size() > 0)
      logger.i1("Skipped %d entries:", skipped.size());
    
    skipped.forEach(s -> logger.i3("> %s", s));

    return new HandleSet(binaryHandles, archiveHandles, nestedHandles, faultyArchives);
  }
}
