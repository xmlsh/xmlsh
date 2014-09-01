/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperty;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

import java.io.IOException;
import java.io.PrintWriter;

public class PropertyWord extends Word
{

	private Word mName ;
	private Word mValue ;
	
	public PropertyWord(Word name, Word value)
    {
		super(name.getFirstToken());
	    mName = name;
	    mValue = value;
    }

	@Override
	public void print(PrintWriter out)
	{
		mName.print( out );
		out.print(" : ");
		mValue.print(out);

	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException, CoreException
	        
	{
	  String name = mName.expandString(shell, mName.evalEnv(nameEnv(env)), loc);
		XValue xv = mValue.expand(shell , mValue.evalEnv(valueEnv(env)) , loc );
		result.add( XValue.asXValue(new XValueProperty( name ,xv )) );
		return result ;
		
	}

	private EvalEnv valueEnv(EvalEnv env) {
        return env.withFlagsSet( EvalFlag.PARSE_QUOTES , EvalFlag.EXPAND_VAR , EvalFlag.JOIN_VALUES , EvalFlag.OMIT_NULL );

    }

    private EvalEnv nameEnv(EvalEnv env) {
        return env.withFlagsSet( EvalFlag.PARSE_QUOTES , EvalFlag.EXPAND_VAR , EvalFlag.JOIN_VALUES , EvalFlag.OMIT_NULL );
    }

    @Override
	public boolean isEmpty()
	{
		return mName == null && mValue == null ;
	}

	@Override
	String getSimpleName()
	{
		return mName.getSimpleName() ;
	}

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */