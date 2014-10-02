package org.xmlsh.sh.grammar;

import static org.xmlsh.sh.grammar.ParserState.TokenEnum.COLON;
import static org.xmlsh.sh.grammar.ParserState.TokenEnum.COMMA;

import java.util.EnumMap;
import java.util.EnumSet;

import org.xmlsh.sh.grammar.ShellParserUtils.TokenValue;

class ParserState {
    
    
    public static enum StateEnum {
        START ,
        BRACE_GROUP,
        FUNCTION_CALL,
        FUNCTION_DECL,
        ARRAY_LIST ,
        SEQUENCE_LIST ,
        BRACE_WORD ;
    }
    
    
    private static EnumMap<StateEnum, ParserState > mStateMap = new EnumMap<StateEnum,ParserState>(StateEnum.class);
    

    public static 
    enum TokenEnum {
        COMMA( new TokenValue( ShellParserConstants.WORD , ",")) , 
        NL( new TokenValue( ShellParserConstants.NL , null )), 
        SEMI( new TokenValue( ShellParserConstants.SEMI , null)),
        RPAREN( new TokenValue( ShellParserConstants.RPAREN , null)),
        COLON(  new TokenValue( ShellParserConstants.WORD , ":"))
        ;
        
        TokenValue value ;
        TokenEnum( TokenValue v ){
            value  = v ;
        }
        
        TokenValue getValue() {
            return value ;
        }
    }

    private EnumSet<TokenEnum> mDelims ;
    
    
    private static ParserState newInstance( StateEnum s ) {
        switch( s )  {
        case BRACE_GROUP :
        case FUNCTION_CALL : 
        case ARRAY_LIST : 
        case SEQUENCE_LIST :
        case FUNCTION_DECL :
            return new ParserState( EnumSet.of( COMMA ));

        case BRACE_WORD :
          return new ParserState( EnumSet.of( COMMA  , COLON ));

        default :
        case START :
            return new ParserState( EnumSet.noneOf(TokenEnum.class));
        }
    }
    

    
    public static ParserState instanceOf( StateEnum s ) {
        ParserState state = mStateMap.get(s);
        if( state == null ) 
            mStateMap.put( s , state = newInstance(s));
        return state ;
    }
    
    
    private ParserState( ) {
        mDelims = EnumSet.noneOf(TokenEnum.class);
    }

    private ParserState( EnumSet<TokenEnum> delims ) {
        mDelims = delims ;
    }
    
    public boolean isDelim( Token t ) {
        TokenEnum te = ShellParserUtils.tokenEnum(t);
        return mDelims.contains(te);
    }
}