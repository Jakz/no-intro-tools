package com.jack.nit.parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import com.jack.nit.data.header.Header;
import com.jack.nit.data.header.Rule;
import com.jack.nit.data.header.Test;
import com.jack.nit.data.header.TestData;
import com.pixbits.lib.io.xml.XMLHandler;

public class HeaderParser extends XMLHandler<Header>
{
  Header header;
  List<Rule> rules;
  List<Test> tests;
  
  @Override protected void init()
  {
    
  }
  
  @Override protected void end(String name)
  {
    if (name.equals("name") || name.equals("author") || name.equals("version"))
      mapOuter(name, asString());
    else if (name.equals("detector"))
    {
      if (rules.size() == 0) throw new IllegalArgumentException("header must contain at least one rule");
      header = new Header(value("name"), valueOrDefault("author", ""), valueOrDefault("version", ""), rules.toArray(new Rule[rules.size()]));
    }
    else if (name.equals("rule"))
    {
      rules.add(new Rule(value("operation"), value("start_offset"), value("end_offset"), tests.toArray(new Test[tests.size()])));
    }
    else if (name.equals("data"))
    {
      tests.add(new TestData(value("offset"), value("value"), value("result")));
    }
  }
  
  @Override protected void start(String name, Attributes attr)
  {
    if (name.equals("detector"))
    {
      rules = new ArrayList<>();
    }
    else if (name.equals("rule"))
    {
      tests = new ArrayList<>();
      
      map("start_offset", longHexAttributeOrDefault("start_offset", 0));
      map("end_offset", longHexAttributeOrDefault("end_offset", Rule.EOF));
      map("operation", Rule.Type.valueOf(this.stringAttributeOrDefault("operation", "none")));
    }
    else if (name.equals("data"))
    {
      map("offset", longHexAttributeOrDefault("offset", 0));
      map("value", hexByteArray("value"));
      map("result", boolOrDefault("result", true));
    }
  }

  @Override public Header get()
  {
    return header;
  }

}
