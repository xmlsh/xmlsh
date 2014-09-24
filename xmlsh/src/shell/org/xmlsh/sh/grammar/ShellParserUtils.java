package org.xmlsh.sh.grammar;
import static org.xmlsh.sh.grammar.ShellParserConstants.WORD;

import org.xmlsh.sh.grammar.ParserState.TokenEnum;


public class ShellParserUtils
{

    
    
    
    
    
    
    static class TokenValue {
        public int kind;
        public String value ;
        public TokenValue(int kind, String value) {
            this.kind = kind;
            this.value = value;
        }
        public boolean equalsToken( Token t ) {
           return kind == t.kind && 
                    (  value == null || value.equals( t.image));
        }
    }
    
    
    // Find the token enum for a given token or null 
     static TokenEnum tokenEnum( Token t ) {
        for( TokenEnum e : TokenEnum.values()) {
            TokenValue v = e.getValue() ;
            if( v.equalsToken( t ) )
                return e ;
        }
        return null ;
    }
     
     // Find the token enum for a given token or null 
     static TokenEnum tokenEnum( int kind , String value  ) {
        for( TokenEnum e : TokenEnum.values()) {
            TokenValue v = e.getValue() ;
            if( v.kind == kind && 
                    ( v.value == value || v.value.equals(value) )  )
                return e ;
        }
        return null ;
    }
     
    public static boolean isWord( Token t  , String name )
    {
      return tokenInList( t , WORD , name );
    }

    public static boolean kindInList( Token t , int... kinds )
    {
      int k = t.kind;
      for( int kind : kinds )
      {
        if( k == kind )
         return true ;
       }
       return false ;
     }
     
    
     public static boolean tokenInList( Token t , int kind , String... names ){
      if( t.kind != kind )
        return false;
      if( names == null || names.length == 0 )
        return true ;
         for( String s : names )
          if( s.equals(t.image) )
            return true;
       return false ;
      }
      
}
