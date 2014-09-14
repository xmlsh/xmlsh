/**
 * $Id: colon.java 245 2009-05-29 11:44:01Z daldei $
 * $Date: 2009-05-29 07:44:01 -0400 (Fri, 29 May 2009) $
 *
 */

package org.xmlsh.builtin.commands;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.EvalScriptExpr;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;

public class xmlsh extends BuiltinCommand {

	static Logger mLogger = LogManager.getLogger();

	boolean mTopShell = false ;


	public xmlsh()
	{

		
		mLogger.entry();
		
	}


	/*
	 * Special constructor for a top level shell which doesnt clone
	 */
	public xmlsh( boolean bTopShell )
	{
		mLogger.entry(bTopShell);

		mTopShell = bTopShell ;
	}



	@Override
	public int run( List<XValue> args ) throws Exception {
		mLogger.entry(args);
		Options opts = new Options( "x,v,c:,rcfile:,e,norc,+location,redirect-output:,redirect-input:,redirect-error:"  );
		opts.parse(args);
		Shell shell = getShell();

		if( ! mTopShell )
			shell = shell.clone();


		int ret = 0;
		try {
			if( opts.hasOpt("v") )
				shell.setOption("v", true);
			if(opts.hasOpt("x"))
				shell.setOption("x", true);

			if( opts.hasOpt("e"))
				shell.setOption("e", true);
			if( opts.hasOpt("location"))
				shell.setOption("location" , opts.getOptFlag("location",true));

			String command  = null ;
			if( opts.hasOpt("c"))
				command = opts.getOptStringRequired("c").toString();

			boolean bNoRc = opts.hasOpt("norc");
			args = opts.getRemainingArgs();

			String rcfile =  opts.getOptString("rcfile", null );
			if( rcfile == null ){
				XValue xrc = shell.getEnv().getVarValue(ShellConstants.XMLSHRC);
				if( xrc != null )
					rcfile = xrc.toString();
				if( rcfile == null ){
					XValue home = shell.getEnv().getVarValue(ShellConstants.HOME);
					if( home != null ){
						rcfile = home.toString() + "/.xmlshrc" ;
					}
				}
			}	


			if( ! bNoRc && rcfile != null )
				shell.runRC(rcfile);




			if(  args.size() == 0 && command == null ){


				ret = shell.interactive();

			} else {


				// Run command
				if(command != null)
				{

					ICommandExpr cmd = new EvalScriptExpr( command );
					ret = shell.exec(cmd);

				}
				else // Run script 
				{

					String scmd = args.remove(0).toString();
					ICommand cmd = CommandFactory.getScript( shell , scmd, SourceMode.SOURCE,getLocation() );
					if( cmd == null ){
						shell.printErr( scmd + ": not found",getLocation());
					}
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
			if( ! mTopShell )
				shell.close();

		}
		return mLogger.exit(ret) ;

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
