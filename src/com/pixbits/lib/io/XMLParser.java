package com.pixbits.lib.io;

import java.io.IOException;
import java.nio.file.Path;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLParser<T>
{
  private final XMLHandler<T> handler;
  private final EntityResolver resolver;
  
  public XMLParser(XMLHandler<T> handler)
  {
    this(handler, null);
  }
  
  public XMLParser(XMLHandler<T> handler, EntityResolver resolver)
  {
    this.handler = handler;
    this.resolver = resolver;
  }
  
  public T load(Path file) throws IOException, SAXException
  {
    XMLReader reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(handler);
    if (resolver != null)
      reader.setEntityResolver(resolver);
    reader.parse(file.toString());
    return handler.get();
  }
  
  
}
