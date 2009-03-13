/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.nio.charset.Charset;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;

public class ShellOpts 
{
	boolean	mVerbose = false;		// -v
	boolean mExec	 = false;		// -x

	
	SerializeOpts	 mSerialize;
	
	public ShellOpts( ) {
		mSerialize = new SerializeOpts();
	}
	public ShellOpts( ShellOpts that )
	{
	
		mVerbose = that.mVerbose;
		mExec=  that.mExec ;

		mSerialize = new SerializeOpts( that.mSerialize );
		
		
	}
	
	
	public void set( String opt , boolean on)
	{
		if( opt.equals("x"))
			mExec = on;
		else
		if( opt.equals("v"))
			mVerbose = on;
		else
			mSerialize.set( opt , on);
		
	}
	public void set(String opt, XValue value) throws InvalidArgumentException {

		mSerialize.set( opt , value );
		
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
