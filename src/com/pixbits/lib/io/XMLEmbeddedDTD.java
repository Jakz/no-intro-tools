package com.pixbits.lib.io;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.xml.sax.SAXException;

public class XMLEmbeddedDTD implements EntityResolver
{
  private final String dtdPath;
  private final String dtdName;
  
  public XMLEmbeddedDTD(String dtdName, String dtdPath)
  {
    this.dtdName = dtdName;
    this.dtdPath = dtdPath;
  }
  
  @Override public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
  {    
    if (systemId.endsWith(dtdName))
      return new InputSource(this.getClass().getClassLoader().getResourceAsStream(dtdPath));
    
    return null;
  }
}
