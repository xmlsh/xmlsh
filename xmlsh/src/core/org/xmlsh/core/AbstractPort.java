/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;


import org.xmlsh.util.ReferenceCounter;

import java.io.File;


public abstract class AbstractPort implements  IPort {

	// volatile so it can start null 
	private	volatile ReferenceCounter mCounter = null;

	@SuppressWarnings("unchecked")
	public <T extends AbstractPort> ReferenceCountedHandle<T> newReference() {
		if( mCounter == null ) {
			synchronized( this ) {
				mCounter = new ReferenceCounter();
			}
		}
		else
			mCounter.addRef();
		return new ReferenceCountedHandle<T>( (T) this  , mCounter );
	}



	private String mSystemId = "";
	private boolean    mSystem;    // System port from original env

	protected void setSystem( boolean system ){
		mSystem = system ;
	}

	@Override
	public String getSystemId() {
		// TODO Auto-generated method stub
		return mSystemId;
	}

	@Override
	public void setSystemId(String systemId)
	{
		mSystemId = systemId;
	}


	@Override
	public boolean isSystem(){
		return mSystem;
	}


	@Override
	public File getFile() throws UnimplementedException {
		throw new UnimplementedException("getFile not implemented on this port");

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
