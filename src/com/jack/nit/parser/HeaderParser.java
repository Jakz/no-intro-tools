package com.jack.nit.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.jack.nit.data.header.Header;
import com.pixbits.io.XMLHandler;

public class HeaderParser extends XMLHandler<Header>
{
  Header header;
  
  @Override public void endElement(String namespaceURI, String localName, String qName) throws SAXException
  {
    
  }
  
  @Override public void startElement(String namespaceURI, String localName, String qName, Attributes attr) throws SAXException
  {
    
  }


  @Override public Header get()
  {
    
    return null;
  }

}
