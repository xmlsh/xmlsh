/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import net.sf.saxon.s9api.XdmEmptySequence;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/* 
 * A list of word expressions that should be joined together as a StringWord
 */
@SuppressWarnings("serial")
public class JoinedWordList extends Word  {

	private List<Word> mList = new ArrayList<>();
	
	public void print(PrintWriter out) {
		for( Word s : mList ){
			s.print( out );
			out.print( " ");
		}
	}

	public void add( Word word ) {
		if( word instanceof JoinedWordList ) 
			mList.addAll(((JoinedWordList)word).mList);
		else
		   mList.add( word );
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
