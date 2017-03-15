package com.github.jakz.nit.emitter;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jakz.nit.Args;
import com.github.jakz.nit.exceptions.FatalErrorException;
import com.github.jakz.nit.parser.DatFormat;
import com.pixbits.lib.io.archive.ArchiveFormat;

public class CreatorOptions
{
  public enum Mode
  {
    merged,
    multi;
  }
  
  public final boolean multiThreaded;
  
  public final List<Path> sourcePaths;
  public final Path destPath;
  public final DatFormat format;
  public final Mode mode;
  
  public final String[] binaryExtensions;
  
  public final PathMatcher archiveMatcher;
  public final PathMatcher binaryMatcher;
  
  public final boolean folderAsArchives;
  
  public final String name;
  public final String description;
  public final String version;
  public final String comment;
  public final String author;
  
  @SuppressWarnings("unchecked")
  public CreatorOptions(Map<String, Object> properties)
  {
    multiThreaded = !(boolean)properties.get(Args.NO_MULTI_THREAD);
    
    destPath = Paths.get((String)properties.get("outfile"));
    
    if (destPath == null || Files.isDirectory(destPath))
      throw new FatalErrorException("destination path for dat creation is invalid/missing: "+destPath);
    
    sourcePaths = ((List<String>)properties.get("infile")).stream().map(Paths::get).collect(Collectors.toList());
    
    if (sourcePaths.stream().anyMatch(p -> !Files.exists(p)))
      throw new FatalErrorException("some of source paths for DAT creation does not exists");
    
    format = (DatFormat)properties.get("format");
    mode = (Mode)properties.get("mode");
    
    archiveMatcher = ArchiveFormat.getReadableMatcher();
    
    List<String> extensions = (List<String>)properties.get("exts");
    binaryExtensions = extensions.toArray(new String[extensions.size()]);
    String matcher = extensions.stream().collect(Collectors.joining(",", "glob:*.{", "}"));
    binaryMatcher = FileSystems.getDefault().getPathMatcher(matcher);
    
    folderAsArchives = (boolean)properties.get("folder-as-archives");
    
    name = (String)properties.get("name");
    description = (String)properties.get("description");
    version = (String)properties.get("version");
    comment = (String)properties.get("comment");
    author = (String)properties.get("author");

  }
  
  public boolean shouldCalculateCRC() { return true; }
  public boolean shouldCalculateMD5() { return true; }
  public boolean shouldCalculateSHA1() { return true; }
}
