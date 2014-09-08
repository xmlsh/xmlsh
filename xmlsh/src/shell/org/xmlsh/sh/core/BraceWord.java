/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;


/*
 * BraceWord represents the syntax  { expr }  , {expr ...}  and  { expr : expr , expr : expr ... }
 * 
 * Evaluating produces either
 *   A) the result of expr exactly
 *   B) XList<> of exprs
 *   C) XList<> of  { expr or property }*
 *   D) XMap<> 
 *   
 *   
 * 
 */
public class BraceWord extends ListWord
{


	public BraceWord(WordList wl)
	{
		super( wl.getFirstToken() );
	    ListWord list = null ;
	    Word  simple;
	    for(Word  w : wl ) {
	        if( w.isDelim() ) {
	            super.add( list );
	            list = null ;
	        } else {
	            if( list == null )
	                list = new ListWord(w.getFirstToken());
	            list.add( w );
	        }
	    }
	    if( list != null )
	        super.add( list );
	    
	}

	@Override
	public void print(PrintWriter out)
	{
		out.print("{");
		String sep="";
		for( Word w : super.mList ) {
			w.print(out);
			out.print(sep);
			sep="";
		}
		out.print("}");
	}





	@Override
    public
	String getSimpleName()
	{
		return isEmpty() ? "{}" :mList.get(0).getSimpleName() + "...";
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Word#evalEnv(org.xmlsh.core.EvalEnv)
	 */
	@Override
	public EvalEnv evalEnv(EvalEnv parent)
	{
		return parent.withFlagsMasked( EvalFlag.preserveValueFlags());
	}

	/*
	 * 
	 *   Call ListWord to expand 
	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException,
	CoreException
	{
		if( mList.isEmpty() )
			return result  ;
		if( mList.size() == 1 )
		    return mList.get(0).expandToResult(shell, evalEnv(env), loc, result);
		

		// Sequence ... 
		XValue xv = null ;
		for( Word w :  mList ) {
            XValue wv = w.expand(shell , evalEnv(env), loc);
            if(  xv == null )
                xv = wv ;
            else
                xv  = xv.append( wv );
		}
		
		
		result.add( xv  );
		return result ;

	}
	*/


}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */