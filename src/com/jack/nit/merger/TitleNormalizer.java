package com.jack.nit.merger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TitleNormalizer
{
  public final static Set<String> words = new HashSet<>();
  //public final static Set<String> languages = new HashSet<>();
  
  static
  { 
    words.addAll(Arrays.asList(
      "(Asia)",
      "(Australia)", 
      "(Beta)", 
      "(Canada)", 
      "(China)", 
      "(En)", 
      "(En,It)",
      "(En,Es)", 
      "(Es,It)",
      "(En,Fr,De)", 
      "(En,Fr,Nl)",
      "(En,De,It)",
      "(En,Fr,De,Es)", 
      "(En,Fr,De,Es,It)", 
      "(En,Fr,De,Es,It,Nl)", 
      "(En,Ja,Fr,De)",
      "(En,Fr,De,Es,It,Da)",
      "(En,Ja,Fr,De,Es,It)",
      "(Fr,De,Es,It)",
      "(En,Fr,De,Da+En,De)",
      "(Es,It+It)",
      "(En+En,Fr,De,Es,It,Nl,Sv,Da)",
      "(En+En,Es,It,Sv,Da)",
      "(En,Fr,De,Es,It,Nl,Sv,Da+En)",
      "(En,Fr,De,Da)",
      "(En,Fr,De,Es,It,Sv)",
      "(En,Fr,De,Es,It,Sv,Da)",
      "(En,No,Da)",
      "(En,It,Sv,No,Da)",
      "(En,Fr,De,Es,It,Fi)",
      "(En,Fr,De,Es+En,Fr,De,Es,It,Nl)",
      "(En,Fr,De,Es,It,Nl+En,Fr,De,Es,It,Nl,Sv,No,Da,Fi)",
      "(En,Fr,De,Es,It,Nl,Pt,Sv)", 
      "(En,Fr,De,Es,It,Nl,Sv,No,Da,Fi)",
      "(En,Fr,De,Es,It,Nl+En,Fr,De,Es,It)",
      "(Es,It+En,Es,It,Sv,Da)",
      "(En,Fr,De,Es,It,Sv,No,Da,Fi)",
      "(It+En,Fr,De,Es,It,Nl,Sv,Da)",
      "(En,Fr,De+En,Fr,De,Es,It)",
      "(En,Fr,De,Es,It,Nl,Pt,Da)",
      "(En,Fr,Es,Nl)",
      "(It,Nl)",
      "(En,Es,It,Sv,Da)",
      "(En+En+En,Fr,De)",
      "(En,Fr,De,Es,It,Nl+En,Fr,De,Es,Nl)",
      "(En,Fr,De+En,Fr,De,Es)",
      "(En,Ja,Fr,De,Es,It+En)",
      "(En,Ja,Fr,De,Es,It+En,Ja,Fr,De,Es)",
      "(En,Ja,Fr,De,Es+En,Ja,Fr,De,Es,It)",
      "(En,Ja,Fr,De,Es+En,Ja,Fr,De,Es,It)",
      "(En,Fr,De,Es,It+En)",
      "(En,Fr,De,Es,It,Nl,Pt,Sv,No,Da,Fi,Pl)",
      "(En,Fr,Es,It,Nl,Pt,Da)",
      "(En,Fr,De,Es,It,Nl,Pt,Da)",
      "(En,Fr,De,Es,It,Nl,Sv)", 
      "(En+En,Ja,Fr,De,Es)",
      "(En,Fr,De,It)", 
      "(En,De,Sv,Fi)",
      "(En,Fr,De,It,Nl)", 
      "(En,Fr,De,Nl)", 
      "(En,Fr,It+Es,It)",
      "(En,De,Es,Nl)",
      "(En,Ja,Fr,De,Es,It)",
      "(Fr,De,Es,It,Nl,Sv,Da)",
      "(En,Fr,De+En)",
      "(En,Fr,Nl)",
      "(En,Ja)", 
      "(Europe)", 
      "(Fr,De)", 
      "(Fr,It)",
      "(France)", 
      "(Denmark)",
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
      "(v1.067)",
      "(v1.1)",
      "(v2.1)", 
      "(v3.3)",
      "(v3.6)",
      "(v5.0)",
      "(PAL)"
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
