package com.jack.nit.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.jack.nit.data.System;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jack.nit.exceptions.FatalErrorException;
import com.jack.nit.persistence.PathDeserializer;

public class Config
{
  public static class DatEntry
  {
    public System system;
    public Path datFile;
    public Path xmdbFile;
    public List<Path> romsetPaths;
  }
  
  public static class CfgOptions
  {
    boolean multiThreaded;
  }
  
  public List<DatEntry> dats;
  public CfgOptions options;
  
  private void verifyThatPathExists(Path path, boolean allowNull, String message)
  {
    if ((allowNull || path != null) && !Files.exists(path))
      throw new FatalErrorException(new FileNotFoundException(path.toString()+": "+message));
  }
  
  public void verify()
  {
    for (DatEntry entry : dats)
    {
      verifyThatPathExists(entry.datFile, false, "dat file doesn't exist");
      verifyThatPathExists(entry.xmdbFile, true, "xmdb file doesn't exist");
      if (entry.romsetPaths != null)
        for (Path romsetPath : entry.romsetPaths)
          verifyThatPathExists(romsetPath, true, "romset path doesn't exist");
    }
  }
  
  
  
  static class SystemDeserializer implements JsonDeserializer<com.jack.nit.data.System>
  {
    @Override
    public System deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
    {
      String string = context.deserialize(element, String.class);
      return com.jack.nit.data.System.forIdent(string);
    }    
  }
  
  public static Config load(Path fileName)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(Path.class, new PathDeserializer());
    builder.registerTypeAdapter(com.jack.nit.data.System.class, new SystemDeserializer());
    Gson gson = builder.create();
    
    try (BufferedReader rdr = Files.newBufferedReader(fileName))
    {
      Config config = gson.fromJson(rdr, Config.class);
      config.verify();
      return config;
    }
    catch (JsonSyntaxException e)
    {
      throw new FatalErrorException(e);
    }
    catch (IOException e)
    {
      throw new FatalErrorException(e);
    }
  }
}
