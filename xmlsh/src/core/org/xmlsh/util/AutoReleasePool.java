/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.IReleasable;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class AutoReleasePool extends ArrayList<IReleasable> implements AutoCloseable {
	private Logger  mLogger= LogManager.getLogger(AutoReleasePool.class );
	@Override
	protected void finalize() throws Exception 
	{
		close();
	}
	@Override
	public void	close() throws Exception {
		Exception et = null ;

		try {
			for( IReleasable obj : this )
				try {
					obj.release();
				} catch(Exception e) {
					if( et == null )
						et=e;
					else
						et.addSuppressed(e);
					mLogger.debug("Exception closing object autorelease object: " , e);
				}
		}
		finally { 
			this.clear();
		}
		if( et != null )
			throw et;
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
