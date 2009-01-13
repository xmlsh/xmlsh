/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;

import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class CaseClause  extends CompoundCommand {
	String		mWord;
	CaseList	mList;
	public CaseClause(String word, CaseList list) {
		super();
		mWord = word;
		mList = list;
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#print(java.io.PrintWriter)
	 */
	@Override
	public void print(PrintWriter out, boolean bExec) {
		out.println("case " + mWord + " in ");
		mList.print(out,bExec);
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.xmlsh.sh.core.Command#exec(org.xmlsh.sh.shell.Shell)
	 */
	@Override
	public int exec(Shell shell) throws Exception {
		
		XIOEnvironment io = shell.getEnv().saveIO();
		try {
			applyRedirect(shell);
			
			String word =shell.expandString(mWord,false);
			
			for( CaseItem item : mList ){
				
				if( item.matches( word )){
					return item.exec( shell );
					
				}
				
			}
			return 1;
		} finally {
			shell.getEnv().restoreIO(io);
		}
		
		
	}
	
	
}


//
//
//Copyright (C) 2008, David A. Lee.
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
