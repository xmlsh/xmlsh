/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.xmlsh.commands.builtin.colon;
import org.xmlsh.commands.builtin.declare;
import org.xmlsh.commands.builtin.echo;
import org.xmlsh.commands.builtin.eval;
import org.xmlsh.commands.builtin.exit;
import org.xmlsh.commands.builtin.jobs;
import org.xmlsh.commands.builtin.read;
import org.xmlsh.commands.builtin.set;
import org.xmlsh.commands.builtin.shift;
import org.xmlsh.commands.builtin.source;
import org.xmlsh.commands.builtin.test;
import org.xmlsh.commands.builtin.unset;
import org.xmlsh.commands.builtin.wait;
import org.xmlsh.commands.builtin.xbreak;
import org.xmlsh.commands.builtin.xcd;
import org.xmlsh.commands.builtin.xcontinue;
import org.xmlsh.commands.builtin.xecho;
import org.xmlsh.commands.builtin.xfalse;
import org.xmlsh.commands.builtin.ximport;
import org.xmlsh.commands.builtin.xread;
import org.xmlsh.commands.builtin.xreturn;
import org.xmlsh.commands.builtin.xtrue;
import org.xmlsh.commands.builtin.xversion;
import org.xmlsh.commands.builtin.xwhich;
import org.xmlsh.sh.core.FunctionDefinition;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Modules;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

public class CommandFactory 
{
	private static Logger mLogger =  Logger.getLogger( CommandFactory.class);
	
	private HashMap<String,Class<?>>		mBuiltins = new HashMap<String,Class<?>>();
	
	private void addBuiltin( String name , Class<?> cls)
	{
		mBuiltins.put( name , cls);
	}
	
	private CommandFactory()
	{
		addBuiltin(  "cd" , xcd.class );
		addBuiltin( "xecho" , xecho.class );
		addBuiltin( "echo" , echo.class );
		addBuiltin( "false" , xfalse.class );
		addBuiltin( "true" , xtrue.class  );
		addBuiltin( "set", set.class);
		addBuiltin( "." , source.class);
		addBuiltin("exit" , exit.class);
		addBuiltin( ":" , colon.class);
		addBuiltin( "[" , test.class );
		addBuiltin( "test" , test.class );
		addBuiltin( "shift" , shift.class );
		addBuiltin( "read" , read.class);
		addBuiltin( "xread" , xread.class);
		addBuiltin( "unset" , unset.class );
		addBuiltin( "xwhich" , xwhich.class );
		addBuiltin( "xversion" , xversion.class);
		addBuiltin("jobs" , jobs.class);
		addBuiltin("wait" , wait.class);
		addBuiltin("break" , xbreak.class);
		addBuiltin("continue", xcontinue.class );
		addBuiltin("eval", eval.class);
		addBuiltin("declare" , declare.class);
		addBuiltin("return" , xreturn.class);
		addBuiltin("import" , ximport.class);
	}
	
	
	
	public static CommandFactory getInstance()
	{
		return new CommandFactory();
	}
	
	
		
	
	
	public ICommand		getCommand(Shell shell , String name) throws IOException
	{
		
		
		
		ICommand cmd = 
			getFunction( shell , name );
		if( cmd == null )
			cmd = getBuiltin(shell, name);
		if( cmd == null )
			cmd = getNative(shell,name);
		if( cmd == null )
			cmd = getScript( shell , name , false );
		if( cmd == null )
			cmd = getExternal(shell,name);
		
		return cmd ;
		
	}

	
	
	
	/*
	 * Gets an External command of given name
	 * by looking through the External Path
	 */

	private ICommand getFunction(Shell shell, String name) {
		
		FunctionDefinition func = shell.getFunction( name );
		if( func != null )
			return new FunctionCommand( func );
		return null;
	}

	private ICommand getExternal(Shell shell, String name) throws IOException 
	{
		File	cmdFile = null;
		
		cmdFile = shell.getExplicitFile( name , true );
		if( cmdFile == null && ! name.endsWith(".exe"))
			cmdFile = shell.getExplicitFile(name + ".exe", true);
		
		
		if( cmdFile == null ){
			Path	path = shell.getExternalPath();
			cmdFile = path.getFirstFileInPath(shell,name);
			if( cmdFile == null && ! name.endsWith(".exe"))
				cmdFile = path.getFirstFileInPath( shell,name + ".exe");

		}
		
		
		if( cmdFile == null )
			return null;
		
		return new ExternalCommand( cmdFile );
		
		
		
		
	}



	private ICommand getNative(Shell shell,String name) {
		
		StringPair 	pair = new StringPair(name,':');
		
		

		Modules modules = shell.getModules();

		
		if( pair.hasLeft() ){ // prefix:name , prefix non-empty
			Module m   = 
				Util.isBlank(pair.getLeft()) ? 
						shell.getModule() : 
				modules.getModule(pair.getLeft());
			// Allow C:/xxx/yyy to work 
			// May look like a namespace but isnt

			if( m != null ){

				ICommand cls = m.getCommandClass( pair.getRight() );
				if( cls != null )
					return cls ;
			

			}
			return null;
		}
			
		/* 
		 * Try all default modules 
		 */
		for( Module m : modules ){
			if( m.isDefault() ){
				
				ICommand cls = m.getCommandClass( name);
				if( cls != null )
					return cls ;
			}
		}
		
			
		return null  ;
		
		
	}
	
	public ICommand getScript(Shell shell, File scriptFile , boolean bSourceMode) {
		if( scriptFile == null )
			return null;
		try {
			return new ScriptCommand( scriptFile , bSourceMode );
		} catch (FileNotFoundException e) {
			shell.printErr("File not found: " + scriptFile.getPath() , e);
			return null ;
		}
		
	}
	
	
	public ICommand		getScript( Shell shell , String name, boolean bSourceMode  ) throws IOException
	{
		File scriptFile = null;
		
		// If ends with .xsh try it
		if( name.endsWith(".xsh"))
			scriptFile = shell.getExplicitFile(name,true);
		
		
		// try adding a .xsh
		if( scriptFile == null  && ! name.endsWith(".xsh"))
			scriptFile = shell.getExplicitFile(name + ".xsh", true);
		
		
		if( scriptFile == null ) {
		
			Path path = shell.getPath("XPATH", true );
			scriptFile = path.getFirstFileInPath(shell,name);
			if( scriptFile == null && ! name.endsWith(".xsh") )
				scriptFile = path.getFirstFileInPath(shell, name + ".xsh");
		}
		return getScript( shell , scriptFile , bSourceMode );
		
	}
	

	private ICommand getBuiltin(Shell shell, String name) {
		Class<?> cls =  mBuiltins.get(name);
		if( cls != null ){
			try {
				return (ICommand) cls.newInstance();
			} catch (Exception e) {
				mLogger.error("Exception creating class: " + cls.toString() );
				return null;
			}
		} else
			return null;
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
