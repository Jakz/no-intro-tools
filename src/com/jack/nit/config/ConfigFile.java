package com.jack.nit.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.jack.nit.exceptions.FatalErrorException;

public class ConfigFile
{
  public static class DatEntry
  {
    String system;
    Path datFile;
    Path xmdbFile;
    Path romsetPath;
  }
  
  public static class CfgOptions
  {
    boolean multiThreaded;
  }
  
  List<DatEntry> dats;
  CfgOptions options;
  
  private void verifyThatPathExists(Path path, String message)
  {
    if (path != null && !Files.exists(path))
      throw new FatalErrorException(new FileNotFoundException(path.toString()+": "+message));
  }
  
  public void verify()
  {
    for (DatEntry entry : dats)
    {
      verifyThatPathExists(entry.datFile, "dat file doesn't exist");
      verifyThatPathExists(entry.xmdbFile, "xmdb file doesn't exist");
      verifyThatPathExists(entry.romsetPath, "romset path doesn't exist");
    }
  }
  
  
  
  
  
  static class PathDeserializer implements JsonDeserializer<Path>
  {
    @Override
    public Path deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
    {
      String string = context.deserialize(element, String.class);
      return Paths.get(string);
    }
  }
  
  public static ConfigFile load(Path fileName)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Path.class, new PathDeserializer());
    Gson gson = builder.create();
    
    try (BufferedReader rdr = Files.newBufferedReader(fileName))
    {
      return gson.fromJson(rdr, ConfigFile.class);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }
}
