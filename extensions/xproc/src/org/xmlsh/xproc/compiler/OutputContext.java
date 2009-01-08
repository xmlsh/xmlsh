/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.io.PrintWriter;

/*
 * Stores temporary streams of data so that preamble and body data
 * can be serialized together
 */
public class OutputContext 
{
	private		StringBuffer	mPreamble = new StringBuffer();
	private		StringBuffer	mBody= new StringBuffer();
	private		String			mStepPrefix = "xps";
	
	
	public OutputContext()
	{
		addPreambleLine("import module " + mStepPrefix + "=org.xmlsh.xproc.steps");
		
	}
	/*
	 * Writes a line to the preamble
	 */
	
	public void	addPreambleLine( String s ){
		mPreamble.append(s ).append('\n');
	}
	
	
	public void	addBodyLine(String s ){
		mBody.append(s ).append('\n');
	}


	public void addBody(String s) {
		mBody.append(s );
		
	}


	public void serialize(PrintWriter w) {
		w.print(mPreamble.toString());
		w.println();
		w.print(mBody.toString());
		w.println();
		
	}
	
	public String getStepPrefix(){
		return mStepPrefix;
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
