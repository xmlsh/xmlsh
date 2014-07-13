/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;

public class CaseClause  extends CompoundCommand {
	Word		mWord;
	CaseList	mList;
	public CaseClause(Word word, CaseList list) {
		super();
		mWord = word;
		mList = list;
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintWriter)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		out.print("case ");
		mWord.print(out);
		out.println( " in ");
		mList.print(out,bExec);
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.sh.shell.Shell)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		
		shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			
			String word = mWord.expandString(shell,EvalEnv.newInstance(false , false, false),getLocation());
			
			for( CaseItem item : mList ){
				
				if( item.matches( shell,  word )){
					return item.exec( shell );
					
				}
				
			}
			return 1;
		} finally {
			shell.getEnv().restoreIO();
		}
		
		
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
