package org.xmlsh.sh.grammar;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xmlsh.sh.core.DelimWord;
import org.xmlsh.sh.core.JoinedWordList;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.core.StringWord;
import org.xmlsh.sh.core.Word;
import org.xmlsh.sh.grammar.ParserState.StateEnum;
import org.xmlsh.sh.shell.Shell;



class ParserContext
{
    private Deque<ParserState>  mStateStack = new ArrayDeque<ParserState>();
    private String mSource ;
    private SourceLocation mParentLocation  ;
    private ShellParser mParser;
    private Shell mShell;
    
    
    public Shell getShell() {
        return mShell;
    }

    ParserContext( Shell shell,ShellParser parser , String source ){
        mParser = parser ;
        mShell = shell ;
        setSource(source) ;
        mStateStack.push( ParserState.instanceOf(StateEnum.START) );

    }
    
    public SourceLocation getLocation(Token t)
    {
      SourceLocation loc = new SourceLocation(getSource(), t == null ? mParser.token : t);
      
      if( mParentLocation != null )
          loc.refineLocation( mParentLocation );   
         
      return loc ;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        mSource = source;
    }


    public SourceLocation setParentLocation( SourceLocation loc )
    {
      SourceLocation r = mParentLocation ;
      mParentLocation = loc ;
      return r ;
    }
    

    // Create a string word or string list 
    public Word newJoinedWord( Token t , Word next )
    {
      if( isDelim( t ) )
          return newJoinedWord( new DelimWord( t) , next );
      else
         return newJoinedWord( new StringWord( t) , next );
    }
    // Create a string word or string list 
    public Word newJoinedWord( Token t )
    {
        return  isDelim( t )  ?  new DelimWord( t) 
    	  : new StringWord( t);
    }
    
    public Word newJoinedWord( Word w , Word next ){
       if( next == null )
         return w ;
       
       JoinedWordList l = new JoinedWordList(w.getFirstToken());

       // adds will flatten out any child joined word lists 
       l.add(w );
       l.add(next);
       return l;
    }

    public boolean isJoinable( Token t )
    {
      return t.specialToken == null && 
          ! isDelim( t ) ;
    }
    public boolean isJoinable( Word w )
    {
      return w.isJoinable() ;
    }
    public boolean isDelim( Token t )
    {
       assert( ! mStateStack.isEmpty() );
       return mStateStack.peek().isDelim(t);
    }

    
    public void pushState( ParserState state  )
    {
        mStateStack.push(state);
    }

    public void popState ( )
    {
         
        
      assert( ! mStateStack.isEmpty() );
      mStateStack.pop();
      assert( ! mStateStack.isEmpty() );
      if( mStateStack.isEmpty() )
          mStateStack.push( ParserState.instanceOf(StateEnum.START) );
    }
     
  

}
