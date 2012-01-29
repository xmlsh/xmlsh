/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;

public class ShellOpts 
{
	public boolean	 mVerbose 		= false;		// -v
	public boolean mExec	 		= false;		// -x
	public boolean	 mXPipe	 		= false;		// -xpipe
	public	boolean	 mThrowOnError 	= false;		// -e


	
	SerializeOpts	 mSerialize;
	
	public ShellOpts( ) {
		mSerialize = new SerializeOpts();
	}
	public ShellOpts( ShellOpts that )
	{
	
		mVerbose = that.mVerbose;
		mExec=  that.mExec ;
		mXPipe = that.mXPipe;
		mThrowOnError = that.mThrowOnError;

		mSerialize = new SerializeOpts( that.mSerialize );
		
		
	}
	
	
	public void setOption( String opt , boolean on)
	{
		if( opt.equals("x"))
			mExec = on;
		else
		if( opt.equals("v"))
			mVerbose = on;
		else
		if( opt.equals("xpipe"))
			mXPipe = on;
		else
		if( opt.equals("e"))
			mThrowOnError = on ;
		else
			mSerialize.setOption( opt , on);
		
	}
	public void setOption(String opt, XValue value) throws InvalidArgumentException {
		
		// No shell options take a string value so just defer to serialization options
		
		mSerialize.setOption( opt , value );
		
	}
	public void setOption(OptionValue ov) throws InvalidArgumentException {
		
		
		if( ov.getOptionDef().hasArgs )
			setOption( ov.getOptionDef().name , ov.getValue() );
		else	
			setOption( ov.getOptionDef().name , ov.getFlag() );
		
	}
	
	
	
}


//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
