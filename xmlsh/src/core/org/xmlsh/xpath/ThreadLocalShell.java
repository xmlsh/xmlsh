/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xpath;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.xmlsh.sh.shell.Shell;



/*
 * Thread local instance of a Shell
 */

public class ThreadLocalShell {

	private static Logger mLogger = LogManager.getLogger();
	static ThreadLocal<Shell>		sInstance = new ThreadLocal<Shell>()
			{
		@Override
		protected synchronized Shell initialValue() {
			return null;
		}
			}
	;

	public static Shell set( Shell shell )
	{
		mLogger.entry(shell);
		Shell old = sInstance.get();
		sInstance.set(shell);
		if( shell == null )
			ThreadContext.remove("tshell" );
		else
		    ThreadContext.put( "tshell",  shell.toString() );
		return mLogger.exit(old);

	}

	public static Shell get()
	{
		return sInstance.get();

	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
