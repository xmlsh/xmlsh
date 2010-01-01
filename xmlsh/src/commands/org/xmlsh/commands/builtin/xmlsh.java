/**
 * $Id: colon.java 245 2009-05-29 11:44:01Z daldei $
 * $Date: 2009-05-29 07:44:01 -0400 (Fri, 29 May 2009) $
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.EvalScriptCommand;
import org.xmlsh.sh.shell.Shell;

public class xmlsh extends BuiltinCommand {

	
	public int run( List<XValue> args ) throws Exception {
			
		Options opts = new Options( "x,v,c:,rcfile:,e"  );
		opts.parse(args);
		
		Shell shell = mShell == null ? new Shell() : mShell.clone();
		
	    int ret = 0;
		try {
			if( opts.hasOpt("v") )
				shell.setOption("v", true);
	
			
	    	if(opts.hasOpt("x"))
				shell.setOption("x", true);
	    	
	    	if( opts.hasOpt("e"))
	    		shell.setOption("e", true);
	    	
	    	String command  = null ;
	    	if( opts.hasOpt("c"))
	    		command = opts.getOptStringRequired("c").toString();
		    
	
		    args = opts.getRemainingArgs();
		    
		    
		    
		    if(  args.size() == 0 && command == null ){
			    String rcfile = opts.getOptString("rcfile", null );
			    if( rcfile == null ){
			    	XValue home = shell.getEnv().getVarValue("HOME");
			    	if( home != null ){
			    		rcfile = home.toString() + "/.xmlshrc" ;
			    	}
			    }	
			    		
		    	ret = shell.interactive(rcfile);
		    	
		    } else {
	
		     	
			    // Run command
			    if(command != null)
			    {
	
	
			    	
			    	Command cmd = new EvalScriptCommand( command );
		    		ret = shell.exec(cmd);
			    	
	
			    }
			    else // Run script 
			    {
			    	
			    	String scmd = args.remove(0).toString();
			    	ICommand cmd = CommandFactory.getInstance().getScript( shell , scmd, true );
			    	if( cmd == null )
			    		shell.printErr( scmd + ": not found");
			    	else {
			    		
			    			// Run as sourced mode, in this shell ...
			    		// must set args ourselves
			    		
			    		shell.setArg0( scmd);
			    		shell.setArgs(args );
			    		ret = cmd.run( shell , scmd , null );
			    	}
			    	
			    	
			    }
		    	
		    }
		} finally {
			shell.close();
			
		}
	    return ret ;
				
	}



}
//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
