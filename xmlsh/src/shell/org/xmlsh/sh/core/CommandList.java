/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.core;

import org.xmlsh.sh.shell.Shell;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


public class CommandList extends Command
{
	private List<Command>  mList = new ArrayList<>();

	public CommandList( Command cmd ) {
		add( cmd );
	}


	public void add( Command cmd ) {
		mList.add(cmd);
	}



	@Override
	public int exec(Shell shell) throws Exception {
		int ret = 0;
		for( Command c : mList ) {
			if( ! shell.keepRunning() )
				return ret ;
			ret = shell.exec(c, getLocation() );

		}
		return ret ;
	}


	@Override
	public void print(PrintWriter out, boolean bExec)
	{
		for( Command c : mList )
			c.print( out ,  bExec );
	}


	@Override
	public boolean isSimple()
	{
		return mList.size() == 1 && mList.get(0).isSimple();
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

