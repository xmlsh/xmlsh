/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public class StringWord extends Word
{
	private String	mString; // String value

	public StringWord(String s)
	{
		mString = s;
	}

	public void print(PrintWriter out)
	{
		out.print(mString);
	}

	public List<XValue> expandToList(Shell shell, boolean bExpandSequences, boolean bExpandWild, boolean bExpandWords,
	                           boolean bTongs, SourceLocation loc) throws IOException, CoreException
	{
		return shell.expandToList(mString, EvalEnv.newInstance(bExpandSequences, bExpandWild, bExpandWords, bTongs), loc);
	}

	@Override
	public XValue expand(Shell shell, boolean bExpandWild, boolean bExpandWords, boolean bTongs,
	                     SourceLocation loc) throws IOException, CoreException
	{
		return shell.expandToValue(mString, EvalEnv.newInstance(bExpandWild, bExpandWords, bTongs), loc);
	}

	public String expandString(Shell shell, boolean bExpandWild, SourceLocation loc) throws IOException, CoreException
	{
		return shell.expandToString(mString, EvalEnv.newInstance(bExpandWild,false,false), loc);
	}

	public boolean isEmpty()
	{
		return mString.isEmpty();
	}

	public String toString()
	{
		return mString;
	}
}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
