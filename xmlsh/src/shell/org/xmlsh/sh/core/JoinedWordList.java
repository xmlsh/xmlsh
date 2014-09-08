/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;


/* 
 * A list of word expressions that should be joined together as a StringWord
 */
@SuppressWarnings("serial")
public class JoinedWordList extends ListWord  {

	 
	public JoinedWordList(Token t)
    {
	    super(t);
    }


	public boolean add( Word word ) {
		if( word instanceof JoinedWordList ) 
			return mList.addAll(((JoinedWordList)word).mList);
		else
			return super.add( word );
	}


	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException,
	CoreException
	{
	  env = evalEnv(env);

		// Combine and wild card expand
		for( Word w : mList ) {
			result = w.expandToResult(shell, env, loc, result);
		}

		return result;

	}



  /* (non-Javadoc)
   * @see org.xmlsh.sh.core.Word#evalEnv(org.xmlsh.core.EvalEnv)
   */
  @Override
  public EvalEnv evalEnv(EvalEnv env)
  {
    // TODO Auto-generated method stub
    return env.withFlagSet(EvalFlag.JOIN_VALUES);
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
