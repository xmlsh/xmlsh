/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueProperty;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

import java.io.IOException;
import java.io.PrintWriter;

public class PropertyWord extends Word
{

	private String mName ;
	private Word mValue ;
	
	public PropertyWord(String name, Word value)
    {
	    mName = name;
	    mValue = value;
    }

	@Override
	public void print(PrintWriter out)
	{
		out.print(mName);
		out.print(":=");
		mValue.print(out);

	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException, CoreException
	        
	{
		XValue xv = mValue.expand(shell, env, loc);
		result.add( new XValue( TypeFamily.XTYPE , new XValueProperty( mName ,xv )) );
		return result ;
		
	}

	@Override
	public boolean isEmpty()
	{
		return mName == null && mValue == null ;
	}

	@Override
	String getSimpleName()
	{
		return mName ;
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