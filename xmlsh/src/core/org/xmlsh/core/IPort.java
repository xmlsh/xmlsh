/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.SaxonApiException;

import org.apache.log4j.LogManager;

import org.xmlsh.util.IManagedObject;

import java.io.File;


public abstract class IPort implements IManagedObject {
	private	int	mRef = 1;
	private String mSystemId = "";
	
	public String getSystemId() {
		// TODO Auto-generated method stub
		return mSystemId;
	}

	public void setSystemId(String systemId)
	{
		mSystemId = systemId;
	}
	public final synchronized void addRef() {
		mRef++;

	}

	public synchronized void flush() throws  CoreException, SaxonApiException {};
	
	public final synchronized void release()
	{		
		try {
			if( --mRef <= 0 ) {
				flush();
				close();
			}
			
			} catch (Exception e) {
				LogManager.getLogger(getClass()).error("Exception closing port",e);
			}
	}
	
	abstract void close() throws CoreException ;
	
	public	boolean	  isFile() { return false ; }
	
	public File		getFile() throws UnimplementedException
	{
		throw new UnimplementedException("IPort.getFile() is not implmented() in class: " + this.getClass().getName() );
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
