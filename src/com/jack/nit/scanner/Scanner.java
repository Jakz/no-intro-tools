package com.jack.nit.scanner;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.jack.nit.Options;
import com.jack.nit.Settings;
import com.jack.nit.data.GameSet;
import com.jack.nit.log.Log;
import com.jack.nit.log.Logger;
import com.pixbits.io.FolderScanner;
import com.pixbits.stream.StreamException;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.PropertyInfo;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Scanner
{
  final private GameSet set;
  final private PathMatcher archiveMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{zip,rar,7z}");
  
  final private Options options;
  
  public Scanner(GameSet set, Options options)
  {
    this.set = set;
    this.options = options;
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
  
  
  public Set<Path> computeFileList() throws IOException
  {
    Set<Path> files = new TreeSet<>();
    Path[] paths = options.dataPath;
    
    boolean includeSubfolders = true;
    final FolderScanner scanner = new FolderScanner(includeSubfolders);

    Arrays.stream(paths).parallel().map(StreamException.rethrowFunction(scanner::scan)).forEach(files::addAll);
    
    Logger.log(Log.INFO1, "found %d files to scan in %d paths", files.size(), paths.length);
    
    return files;
  }

  public RomHandlesSet computeHandles() throws IOException
  {
    Set<Path> paths = computeFileList();
    
    Set<Path> faultyArchives = new HashSet<>();
    Set<String> skipped = new HashSet<>();
    
    Logger.logger.startProgress("[INFO] Finding files...");
    final float count = paths.size();
    final AtomicInteger current = new AtomicInteger(0);
    
    List<BinaryHandle> binaryHandles = new ArrayList<>();
    List<ArchiveHandle> archiveHandles = new ArrayList<>();

    paths.stream().forEach(StreamException.rethrowConsumer(path -> {
      //Logger.logger.updateProgress(current.getAndIncrement() / count, "");
      
      boolean shouldBeArchive = archiveMatcher.matches(path.getFileName());
      
      if (shouldBeArchive)
      {      
        try (IInArchive archive = openArchive(path))
        {
          int itemCount = archive.getNumberOfItems();

          //boolean isSolid = (boolean)archive.getArchiveProperty(PropID.SOLID);
          //System.out.println("Solid: "+isSolid);

          /*System.out.println("Archive: "+path.getFileName());
          for (int i = 0; i < archive.getNumberOfArchiveProperties(); ++i)
          {
            System.out.println(archive.getArchivePropertyInfo(i).propID + ": " + archive.getArchiveProperty(archive.getArchivePropertyInfo(i).propID));
          }
          
          System.out.println("Item prop");
          for (int i = 0; i < archive.getNumberOfProperties(); ++i)
          {
            System.out.println(archive.getPropertyInfo(i).propID + ": " + archive.getProperty(0, archive.getPropertyInfo(i).propID));
          }*/

          if (true)
          {   
            for (int i = 0; i < itemCount; ++i)
            {
              long size = (long)archive.getProperty(i, PropID.SIZE);
              Long lcompressedSize = (Long)archive.getProperty(i, PropID.PACKED_SIZE);
              long compressedSize = lcompressedSize != null ? lcompressedSize : -1;
              String fileName = (String)archive.getProperty(i, PropID.PATH);
              long crc = (Integer)archive.getProperty(i, PropID.CRC);
              
              if (set.cache().isValidSize(size) || !options.matchSize)
                archiveHandles.add(new ArchiveHandle(path, archive.getArchiveFormat(), fileName, i, size, compressedSize, crc));
              else
                skipped.add(fileName+" in "+path.getFileName());
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
        /* if size of the file is compatible with the romset add it to potential roms */
        if (set.cache().isValidSize(Files.size(path)) || !options.matchSize)
          binaryHandles.add(new BinaryHandle(path));
        else
          skipped.add(path.getFileName().toString());
          
      }    
    }));
    
    Logger.logger.endProgress();
    
    faultyArchives.forEach(p -> Logger.log(Log.WARNING, "File "+p.getFileName()+" is not a valid archive."));

    Logger.log(Log.INFO1, "Found %d potential matches (%d binary, %d inside archives).", binaryHandles.size()+archiveHandles.size(), binaryHandles.size(), archiveHandles.size());
    if (skipped.size() > 0)
      Logger.log(Log.INFO3, "Skipped %d entries:", skipped.size());
    skipped.forEach(s -> Logger.log(Log.INFO3, "> %s", s));

    return new RomHandlesSet(binaryHandles, archiveHandles);
  }
}
