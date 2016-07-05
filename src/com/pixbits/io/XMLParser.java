package com.pixbits.io;

import java.io.IOException;
import java.nio.file.Path;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class XMLParser<T>
{
  XMLHandler<T> handler;
  
  public XMLParser(XMLHandler<T> handler)
  {
    this.handler = handler;
  }
  
  public T load(Path file) throws IOException, SAXException
  {
    XMLReader reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(handler);
    reader.parse(file.toString());
    return handler.get();
  }
}
