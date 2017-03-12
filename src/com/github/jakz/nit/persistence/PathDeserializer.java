package com.github.jakz.nit.persistence;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class PathDeserializer implements JsonDeserializer<Path>
{
  @Override
  public Path deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
  {
    String string = context.deserialize(element, String.class);
    return Paths.get(string);
  }
}