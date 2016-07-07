package com.jack.nit.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import com.jack.nit.data.GameSet;
import com.jack.nit.data.header.Header;
import com.jack.nit.data.header.Rule;
import com.jack.nit.data.header.Test;
import com.jack.nit.data.header.TestData;
import com.jack.nit.data.xmdb.CloneSet;
import com.jack.nit.data.xmdb.GameClone;
import com.pixbits.io.XMLHandler;

public class XMDBParser extends XMLHandler<CloneSet>
{
  List<GameClone> clones;
  GameSet set;
  
  XMDBParser(GameSet set)
  {
    this.set = set;
  }
  
  @Override protected void end(String name)
  {
    /*if (name.equals("name") || name.equals("author") || name.equals("version"))
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
    }*/
  }
  
  @Override protected void start(String name, Attributes attr)
  {
    /*if (name.equals("bias"))
    
    
    if (name.equals("detector"))
    {
      rules = new ArrayList<>();
    }
    else if (name.equals("rule"))
    {
      tests = new ArrayList<>();
      
      map("start_offset", longHexAttributeOrDefault(attr, "start_offset", 0));
      map("end_offset", longHexAttributeOrDefault(attr, "end_offset", Rule.EOF));
      map("operation", Rule.Type.valueOf(this.stringAttributeOrDefault(attr, "operation", "none")));
    }
    else if (name.equals("data"))
    {
      map("offset", longHexAttributeOrDefault(attr, "offset", 0));
      map("value", hexByteArray(attr, "value"));
      map("result", boolOrDefault(attr, "result", true));
    }*/
  }

  @Override public CloneSet get()
  {
    return new CloneSet(clones.toArray(new GameClone[clones.size()]));
  }

}
