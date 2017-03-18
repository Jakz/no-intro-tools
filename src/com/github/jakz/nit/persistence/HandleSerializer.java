package com.github.jakz.nit.persistence;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.pixbits.lib.io.archive.handles.ArchiveHandle;
import com.pixbits.lib.io.archive.handles.BinaryHandle;
import com.pixbits.lib.io.archive.handles.Handle;

public class HandleSerializer
{
  public final static int BINARY = 0;
  public final static int ARCHIVE = 1;
  public final static int NESTED_ARCHIVE = 2;

  class RomHandleSerializer implements JsonDeserializer<Handle>, JsonSerializer<Handle>
  {
    @Override
    public Handle deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
    {

      return null;
    }

    @Override
    public JsonElement serialize(Handle handle, Type type, JsonSerializationContext context)
    {
      JsonArray array = new JsonArray();
      
      if (handle instanceof BinaryHandle)
      {
        array.add(new JsonPrimitive(BINARY));
        array.add(context.serialize(handle.path()));
      }
      else if (handle instanceof ArchiveHandle)
      {
        ArchiveHandle ahandle = (ArchiveHandle)handle;
        
        array.add(new JsonPrimitive(ARCHIVE));
        array.add(context.serialize(ahandle.path()));
        array.add(context.serialize(ahandle.format));
        array.add(new JsonPrimitive(ahandle.internalName));
        array.add(new JsonPrimitive(ahandle.indexInArchive));
        array.add(new JsonPrimitive(ahandle.size));
        array.add(new JsonPrimitive(ahandle.compressedSize));
      }
    
      return array;
    }
    
  }
  
  public String serialize(Handle handle) throws IOException
  {
    try
    (
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(bos);
        DataOutputStream dos = new DataOutputStream(zos);
    )
    {
      Base64.getEncoder();
      
      if (handle instanceof BinaryHandle)
      {
        
      }
    }
    
    return null;
    
  }
}
