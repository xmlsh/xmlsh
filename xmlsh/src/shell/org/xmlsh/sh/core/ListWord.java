/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/* 
 * A list of word expressions that are treatable as a single word  for expression purposes
 * but expand to a list/sequence on expansion
 */
@SuppressWarnings("serial")
public class ListWord extends Word  {

	protected ListWord(Token t)
    {
	    super(t);
    }

	protected WordList mList = new WordList();

	@Override
	public void print(PrintWriter out) {
	    mList.print(out);;
	}
	 
	public boolean add( Word word ) {
		if( word instanceof ListWord ) 
			return mList.addAll(((ListWord)word).mList);
		else
			return mList.add( word );
	}



    @Override
	public boolean isEmpty()
	{
		return mList.isEmpty();
	}

	@Override
	public
	String getSimpleName()
	{

		// Temporary Hack
		StringBuilder sb = new StringBuilder();
		for( Word w : mList ) {

			if( sb.length() > 0 )
				sb.append(" ");
			sb.append( w.getSimpleName());
		}
		return sb.toString();
	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException,
	CoreException
	{

		// Combine and wild card expand
		for( Word w : mList ) {
			result.delim();
		    result = w.expandToResult(shell, env, loc, result);
		}

		return result;

	}
   

}



//
//
//Copyright (C) 2008-2014    David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
