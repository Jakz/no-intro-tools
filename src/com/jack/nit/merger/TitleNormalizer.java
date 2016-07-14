package com.jack.nit.merger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TitleNormalizer
{
  public final static Set<String> words = new HashSet<>();
  
  static
  {
    words.addAll(Arrays.asList(
      "(Australia)", 
      "(Beta)", 
      "(Canada)", 
      "(China)", 
      "(En)", 
      "(En,Es)", 
      "(En,Fr,De)", 
      "(En,Fr,De,Es)", 
      "(En,Fr,De,Es,It)", 
      "(En,Fr,De,Es,It,Nl)", 
      "(En,Fr,De,Es,It,Nl,Pt,Sv)", 
      "(En,Fr,De,Es,It,Nl,Sv)", 
      "(En,Fr,De,It)", 
      "(En,Fr,De,It,Nl)", 
      "(En,Fr,De,Nl)", 
      "(En,Ja)", 
      "(Europe)", 
      "(Fr,De)", 
      "(France)", 
      "(GBC,SGB Enhanced)", 
      "(Germany)", 
      "(Italy)",
      "(Japan)", 
      "(Japan, Europe)",
      "(Japan, USA)", 
      "(Proto)", 
      "(Rev 1)", 
      "(Rev 2)", 
      "(Rev 3)", 
      "(Rev A)", 
      "(Rev AB)", 
      "(Rev B)", 
      "(SGB Enhanced)",
      "(Spain)", 
      "(Sweden)", 
      "(USA)", 
      "(USA, Europe)", 
      "(Unknown)", 
      "(Unl)", 
      "(World)", 
      "(v1.0)", 
      "(v2.1)", 
      "(v3.6)"      
    )); 
    
    words.addAll(Arrays.asList(
      "(Alternate)",
      "(early)",
      "(En,De)",
      "(En,De,Es,It)",
      "(En,Es,It)",
      "(En,Es,Nl)",
      "(En,Fr)",
      "(En,Fr,De,Es,It,Nl,Ca)",
      "(En,Fr,De,Es,It,Nl,Da)",
      "(En,Fr,De,Es,It,Nl,Pt)",
      "(En,Fr,De,Es,It,Nl,Pt,Sv,Da)",
      "(En,Fr,De,Es,It,Nl,Pt,Sv,No,Da)",
      "(En,Fr,De,Es,It,Nl,Pt,Sv,No,Da,Fi)",
      "(En,Fr,De,Es,It,Nl,Sv,Da)",
      "(En,Fr,De,Es,It,Nl,Sv,No,Da)",
      "(En,Fr,De,Es,It,Pt)",
      "(En,Fr,De,Es,Nl)",
      "(En,Fr,De,Es,Sv)",
      "(En,Fr,De,It,Nl,Sv)",
      "(En,Fr,Es)",
      "(En,Fr,Es,It)",
      "(En,Fr,Es,Pt)",
      "(En,Fr,It)",
      "(En,Ja,Fr,De,Es)",
      "(En,Ja,Fr,De,Es,Zh)",
      "(En,Nl,Sv,No,Da)",
      "(En,Sv,No,Da,Fi)",
      "(En,Zh)",
      "(Fr,De,Es)",
      "(Fr,De,Nl)",
      "(Fr,Nl)",
      "(Korea)",
      "(NP)",
      "(NP, SGB Enhanced)",
      "(Netherlands)",
      "(No Copyright)",
      "(Promo)",
      "(Rumble Version)",
      "(Sample)",
      "(Taiwan)",
      "(USA, Australia)"  
    ));
  }
  
  
  public String normalize(String name)
  {
    for (String text : words)
      name = name.replace(text, "");
    name = name.replaceAll(" +", " ");
    return name.trim();
  }
}
