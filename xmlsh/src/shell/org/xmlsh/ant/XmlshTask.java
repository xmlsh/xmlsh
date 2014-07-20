/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.ant;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import java.util.ArrayList;
import java.util.List;


public class XmlshTask extends Task {
	private static Logger mLogger = LogManager.getLogger( XmlshTask.class);
	private String mScript;		// script file
	private	 String mText;		// embedded script or attribute

	static {
		// Pre-initialize shell so logging can work before executing first task
		Shell.initialize();
	
	}

	
	
	public static class Arg {
		String mValue ;
		public void setValue(String value){ mValue = value ; }
		
	}
	
	private List<Arg>	mArgs = null ;
	
	
	public XmlshTask()
	{
		mLogger.debug("Initializing xmlsh ant task");
	}
	
	public void addArg( Arg arg ){
		if( mArgs == null )
			mArgs = new ArrayList<Arg>();
		mArgs.add(arg);
		
		
	}
	
    // The method executing the task
    public void execute() throws BuildException {
    	mLogger.info("executing xmlsh ant task");
    	// System.out.println("property test: " + getProject().getProperty("test"));

		Shell shell = null ;
    	try {
    		shell = new Shell();
    		
    		
	     	List<XValue> vargs = new ArrayList<XValue>();
		 	if( mScript != null )
		 		vargs.add( new XValue(mScript));
		 	else {
		 		vargs.add( new XValue("-c"));
		 		vargs.add( new XValue(mText));
		 	}
			
		 	if( mArgs != null ){
		 		for( Arg arg : mArgs )
		 			vargs.add( new XValue( arg.mValue));
		 	}
		 	
			org.xmlsh.commands.builtin.xmlsh cmd = new org.xmlsh.commands.builtin.xmlsh(true);
			

			@SuppressWarnings("unused")
			int ret = cmd.run(shell, "xmlsh" , vargs);
			
    	}
    	catch(  Exception e )
    	{
    		
    		throw new BuildException(e);
    		
    	} finally {
    		Util.safeClose(shell);
    	}
    	

    	
        
    }

    // The setter for the "message" attribute
    public void setScript(String script) {
        this.mScript = script;
    }
    
    public void addText( String text )
    {
    	if( mText != null )
    		text = mText + text ;
    	mText = text ;
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
