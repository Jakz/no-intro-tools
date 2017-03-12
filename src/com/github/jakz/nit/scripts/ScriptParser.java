package com.github.jakz.nit.scripts;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.pattern.Patterns;

/*

select "is:favorite"
apply sram patch
apply sleephack not on
  "kuru paradise loc:europ"
  "advance wars"
apply ips patch on
  "yoshi universal loc:europe" absolute "/Volumes/Win bla bla"
  "wario twisted"
trim 0x00 0xff
consolidate in "path" with ez4



 */


/*
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
*/

//@RunWith(Enclosed.class)
public class ScriptParser
{
  static final Parser<Void> WHITESPACE = Scanners.WHITESPACES.skipMany();
  
  public Parser<Script> script()
  {
    Parser<Statement> statement = statement().between(WHITESPACE, WHITESPACE.next(Patterns.among(";\n").toScanner("").skipMany()));
    return statement.atLeast(1).map( l -> new Script(l));
  }
  
  private Parser<Statement> statement()
  {
    //return Parsers.or(
    return exitStatement();
    //);
  }
  
  private class ExitStatement implements Statement
  {

    @Override
    public void execute(ScriptEnvironment env)
    {
      System.exit(0);
    }
    
  }
  
  private Parser<Statement> exitStatement()
  {
    return Patterns.stringCaseInsensitive("exit").toScanner("exit").cast().map(v -> (env -> System.exit(0)));
  }
  
  /*private Parser<SelectStatement> selectStatement()
  {
    return Scanners.stringCaseInsensitive("select")
        .next(WHITESPACE)
        .next(queryExpression())
        .map(query -> new SelectStatement(RomSet.current.getSearcher().search(query.substring(1, query.length()-1))));
  }
  
  private Parser<FindStatement> findStatement()
  {
    return Scanners.stringCaseInsensitive("find")
        .next(WHITESPACE)
        .next(queryExpression())
        .map(query -> new FindStatement(RomSet.current.getSearcher().search(query.substring(1, query.length()-1))));
  }
  
  private Parser<Statement> statement()
  {
    return Parsers.or(
      selectStatement(),
      findStatement()
    );
  }


  
  private Parser<String> queryExpression() { 
    return Parsers.or(
      Scanners.SINGLE_QUOTE_STRING
    ); }*/
  
  /*
  @Test
  public void testPredicate()
  {
    assertThat(selectStatement().parse("select 'is:fav save:\"eeprom 111\"'"), notNullValue());
  }
  
  @Test
  public void testStatement()
  {
    assertThat(statement().parse("find 'is:fav'"), notNullValue());
    assertThat(statement().parse("select 'is:fav'"), notNullValue());
  }
  
  @Test
  public void testStatementSequence()
  {
    assertThat(script().parse("find 'is:fav';select 'is:fav'").length(), is((Object)2));
    assertThat(script().parse("find 'is:fav'; select 'is:fav'").length(), is((Object)2));
    assertThat(script().parse("find 'is:fav';").length(), is((Object)1));
    assertThat(script().parse("find 'is:fav'\n select 'is:fav'").length(), is((Object)2));
  }*/
}
