package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.XVariableExpr;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * Variable Expansion detected at parser time
 * 
 * ${ [#|!] varname [ '[' index ']' ] [ ':' expr }
 * 
 */
public class VarExpansion extends Word
{
	private final static EnumSet<EvalFlag> _indMskFlags = EnumSet.of( EvalFlag.EXPAND_VAR , EvalFlag.PARSE_QUOTES);
	private String mPrefix ; //"#" | "!"
    private String mVarname ;
    private Word   mIndex ; 
    private Word  mField;
    
	// Single token expansion 
    public VarExpansion(Token t, String simplevar )
    {
    	super(t);
	    mVarname = simplevar;
    }

    public VarExpansion(Token t, String prefix , String var , Word ind , Word field   )
    {
    	super(t);
    	mPrefix = prefix ;
	    mVarname = var;
	    mIndex = ind ;
	    mField = field ;
    }


    @Override
    public void print(PrintWriter out) {
        out.print("${");
        if( mPrefix != null )
            out.print(mPrefix);
        out.print( mVarname );
        if( mIndex != null ) {
            out.print("[");
            mIndex.print(out);
            out.print("]");
        }
        if( mField != null ) {
            out.print(":" );
            mField.print(out);
        }
        out.print("}");
    }

    @Override
    protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException, CoreException
    {
        String ind = mIndex != null ? mIndex.expandString(shell, indEnv(env), loc) : null ;
        String field = mField != null ?  mField.expandString(shell, env, loc) : null ;
        XVariableExpr expr = new XVariableExpr( mPrefix , mVarname , ind , field );
        return EvalUtils.evalVarToResult(shell, expr, env, env.asCharAttr() , result);
    }

    private EvalEnv indEnv(EvalEnv env)
    {
	   return env.withFlagsMasked( _indMskFlags );
    }
	@Override
    public boolean isEmpty() {

    	return Util.isEmpty(mVarname);
    	
    }

    @Override
    String getSimpleName() {
       return mVarname ;
    }

}
