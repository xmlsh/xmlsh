package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;

public class DelimWord extends Word
{
    private String mDelim ;
    
    public DelimWord( Token delim )
    {
    	super(delim);
        mDelim = delim.toString() ;
    }
    @Override
    public void print(PrintWriter out) {
        out.print( getSimpleName() ) ;
    }

    @Override
    protected ParseResult expandToResult(Shell shell, EvalEnv env, ParseResult result)
            throws IOException, CoreException {
            
        result.delim();
        return result ;
    }

    @Override
    public boolean isEmpty() {
      return true ;
    }

    @Override
    String getSimpleName() {
        return "<" + mDelim + ">";
    }
    @Override
    public
    boolean isDelim() {
        return true ;
    }
    
    @Override
    public List<XValue> expandToList(Shell shell, EvalEnv env ) throws IOException, CoreException {
        return null ;
    }

        

}
