/**
 * $Id: NullCommand.java 388 2010-03-08 12:27:19Z daldei $
 * $Date: 2010-03-08 07:27:19 -0500 (Mon, 08 Mar 2010) $
 *
 */

package org.xmlsh.sh.core;

import java.io.PrintWriter;
import java.util.List;

import net.sf.saxon.s9api.XdmEmptySequence;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public class ReturnStatement extends Command {

	private		Word	mArg;
	public	boolean		isSimple() { return false ; }

	public ReturnStatement( Word arg )
	{
		mArg = arg ;
	}
	
	@Override
	public void print(PrintWriter out, boolean bExec) {
		out.print("return " );
		mArg.print(out);

	}

	@Override
	public int exec(Shell shell) throws Exception {
		
		XValue ret = null ;
		
		
		if( mArg == null )
			ret = new XValue(0);
		else	{
			List<XValue> vret = mArg.expand(shell, false , false , false , getLocation() );
			if( vret == null || vret.isEmpty() )
				ret = new XValue(XdmEmptySequence.getInstance() );
			else
				ret = vret.get(0);
		}
			
		shell.exec_return( ret );
		return 0;
		
	}

}
//
//
//Copyright (C) 2008-2012  David A. Lee.
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
