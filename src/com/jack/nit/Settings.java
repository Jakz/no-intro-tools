package com.jack.nit;

import java.util.Arrays;
import java.util.Optional;

import net.sf.sevenzipjbinding.ArchiveFormat;

public class Settings
{
  public static int PIPED_BUFFER_SIZE = 1024*128;
  public static int DIGEST_BUFFER_SIZE = 1024*32;
  
  public static class ArchiveType
  {
    public final String extension;
    public final ArchiveFormat format;
    private ArchiveType(String extension, ArchiveFormat format)
    {
      this.extension = extension;
      this.format = format;
    }
  }
  
  public static final ArchiveType[] archiveExtensions = new ArchiveType[] {
    new ArchiveType("zip", ArchiveFormat.ZIP),
    new ArchiveType("7z", ArchiveFormat.SEVEN_ZIP),
    new ArchiveType("rar", ArchiveFormat.RAR)
  };
  
  public static ArchiveFormat guessFormatForFilename(String filename)
  {
    Optional<ArchiveType> ai = Arrays.stream(Settings.archiveExtensions).filter(f -> filename.endsWith(f.extension)).findFirst();
    return ai.isPresent() ? ai.get().format : null;
  }
}
