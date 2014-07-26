/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import org.apache.log4j.Logger;
import org.xmlsh.commands.builtin.colon;
import org.xmlsh.commands.builtin.declare;
import org.xmlsh.commands.builtin.echo;
import org.xmlsh.commands.builtin.eval;
import org.xmlsh.commands.builtin.exit;
import org.xmlsh.commands.builtin.jobs;
import org.xmlsh.commands.builtin.log;
import org.xmlsh.commands.builtin.printvar;
import org.xmlsh.commands.builtin.read;
import org.xmlsh.commands.builtin.require;
import org.xmlsh.commands.builtin.set;
import org.xmlsh.commands.builtin.shift;
import org.xmlsh.commands.builtin.source;
import org.xmlsh.commands.builtin.test;
import org.xmlsh.commands.builtin.tie;
import org.xmlsh.commands.builtin.trap;
import org.xmlsh.commands.builtin.unset;
import org.xmlsh.commands.builtin.wait;
import org.xmlsh.commands.builtin.xbreak;
import org.xmlsh.commands.builtin.xcd;
import org.xmlsh.commands.builtin.xcontinue;
import org.xmlsh.commands.builtin.xecho;
import org.xmlsh.commands.builtin.xfalse;
import org.xmlsh.commands.builtin.ximport;
import org.xmlsh.commands.builtin.xmkpipe;
import org.xmlsh.commands.builtin.xmlsh;
import org.xmlsh.commands.builtin.xmlshui;
import org.xmlsh.commands.builtin.xread;
import org.xmlsh.commands.builtin.xthrow;
import org.xmlsh.commands.builtin.xtrue;
import org.xmlsh.commands.builtin.xtype;
import org.xmlsh.commands.builtin.xversion;
import org.xmlsh.commands.builtin.xwhich;
import org.xmlsh.commands.java.jset;
import org.xmlsh.commands.json.jsonread;
import org.xmlsh.sh.core.FunctionDeclaration;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Modules;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class CommandFactory 
{
	public static final String kCOMMANDS_HELP_XML = "/org/xmlsh/resources/help/commands.xml";
	public static final String kFUNCTIONS_HELP_XML = "/org/xmlsh/resources/help/functions.xml";

	private static Logger mLogger =  Logger.getLogger( CommandFactory.class);
	private static CommandFactory _instance = null ;
	
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
		addBuiltin( "source" , source.class);
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
		addBuiltin("import" , ximport.class);
		addBuiltin("xmlsh" , xmlsh.class);
		addBuiltin("throw" , xthrow.class);
		addBuiltin("tie" , tie.class);
		addBuiltin("log",log.class);
		addBuiltin("xtype", xtype.class);
		addBuiltin("require", require.class);
		addBuiltin("jset" , jset.class );
		addBuiltin("xmlshui" , xmlshui.class);
		addBuiltin("xmkpipe" , xmkpipe.class);
		addBuiltin("printvar" , printvar.class);
		addBuiltin("jsonread" , jsonread.class);
		addBuiltin("trap" , trap.class);
		
		

	}
	
	
	public synchronized static CommandFactory getInstance()
	{
		if( _instance == null )
			_instance = new CommandFactory();
		return _instance ;
	}
	
	
		
	
	
	public ICommand		getCommand(Shell shell , String name, SourceLocation loc ) throws IOException, CoreException
	{
		
		
		
		ICommand cmd = 
			getFunction( shell , name , loc  );
		if( cmd == null )
			cmd = getBuiltin(shell, name , loc );
		if( cmd == null )
			cmd = getModuleCommand(shell,name , loc );
		if( cmd == null )
			cmd = getScript( shell , name , false , loc  );
		if( cmd == null )
			cmd = getExternal(shell,name , loc );
		
		return cmd ;
		
	}

	
	
	
	/*
	 * Gets an External command of given name
	 * by looking through the External Path
	 */

	private ICommand getFunction(Shell shell, String name,  SourceLocation loc) {
		
		FunctionDeclaration func = shell.getFunction( name );
		if( func != null )
			return new FunctionCommand( func.getName() , func.getBody()  , loc );
		return null;
	}

	private ICommand getExternal(Shell shell, String name, SourceLocation loc ) throws IOException 
	{
		File	cmdFile = null;
		
		if( Util.hasDirectory(name)){
	
			cmdFile = shell.getExplicitFile( name , true );
			if( cmdFile == null && ! name.endsWith(".exe"))
				cmdFile = shell.getExplicitFile(name + ".exe", true);
		}
		
		if( cmdFile == null ){
			Path	path = shell.getExternalPath();
			cmdFile = path.getFirstFileInPath(shell,name);
			if( cmdFile == null && ! name.endsWith(".exe"))
				cmdFile = path.getFirstFileInPath( shell,name + ".exe");

		}
		
		
		if( cmdFile == null )
			return null;
		
		return new ExternalCommand( cmdFile, loc  );
		
		
		
		
	}



	private ICommand getModuleCommand(Shell shell,String name, SourceLocation loc) {

		
		
		
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
				if( cls != null ){
					cls.setLocation( loc );
				
					return cls ;
				}

			}
			return null;
		}
			
		/* 
		 * Try all default modules 
		 */
		for( Module m : modules ){
			if( m.isDefault() ){
				
				ICommand cls = m.getCommandClass( name);
				if( cls != null ){
					cls.setLocation(loc);
					return cls ;
				}
			}
		}
		
			
		return null  ;
		
		
	}
	
	public ICommand getScript(Shell shell, String name , InputStream is , boolean bSourceMode , SourceLocation loc ) throws CoreException {
		if( is == null )
			return null;
	
		return new ScriptCommand(  name , is , bSourceMode , null );
		
	}
	
	
	public ICommand		getScript( Shell shell , String name, boolean bSourceMode , SourceLocation loc ) throws IOException, CoreException
	{
		File scriptFile = null;
		
		// If name has a scheme try that first
		URL url =  Util.tryURL(name);
		if( url != null )
			return getScript( shell , name , url.openStream() , bSourceMode , loc );
		
		
		// If ends with .xsh try it
		if( name.endsWith(".xsh") || bSourceMode )
			scriptFile = shell.getExplicitFile(name,true);
		
		if( Util.hasDirectory(name)){
			// try adding a .xsh
			if( scriptFile == null  && ! name.endsWith(".xsh"))
				scriptFile = shell.getExplicitFile(name + ".xsh", true);
		}
		else
		if( scriptFile == null ) {
		
			Path path = shell.getPath(XVariable.XPATH, true );
			scriptFile = path.getFirstFileInPath(shell,name);
			if( scriptFile == null && ! name.endsWith(".xsh") )
				scriptFile = path.getFirstFileInPath(shell, name + ".xsh");
		}
		if( scriptFile == null )
			return null ;
		return getScript( shell , scriptFile , bSourceMode , loc );
		
	}
	

	private ICommand getBuiltin(Shell shell, String name, SourceLocation loc) {
		Class<?> cls =  mBuiltins.get(name);
		if( cls != null ){
			try {
				BuiltinCommand b =  (BuiltinCommand) cls.newInstance();
				b.setLocation( loc );
				return b;
			} catch (Exception e) {
				mLogger.error("Exception creating class: " + cls.toString() );
				return null;
			}
		} else
			return null;
	}


	
	public URL getHelpURL( Shell shell , String name )
	{
		
		URL url = null ;
		
		
		if( url == null )
			url = getBuiltinHelpURL(shell, name);
		
		if( url == null )
			url = getNativeHelpURL(shell,name);

		return url ;
		
	}

	private URL getNativeHelpURL(Shell shell, String name) {

		
		StringPair 	pair = new StringPair(name,':');
		Modules modules = shell.getModules();

		
		if( pair.hasLeft() ){ // prefix:name , prefix non-empty
			Module m   = 
				Util.isBlank(pair.getLeft()) ? 
						shell.getModule() : 
				modules.getModule(pair.getLeft());
			// Allow C:/xxx/yyy to work 
			// May look like a namespace but isnt

			if( m != null && m.hasCommand( pair.getRight() ) )
				return m.getHelpURL( );
			return null;
		}
			
		/* 
		 * Try all default modules 
		 */
		for( Module m : modules ){
			if( m.isDefault() ){
				
				if( m != null && m.hasCommand( name ) )
					return m.getHelpURL( );
			}
		}
		
			
		return null  ;
		
		
	}

	private URL getBuiltinHelpURL(Shell shell, String name) {
		if( mBuiltins.containsKey(name) )
			return shell.getResource(kCOMMANDS_HELP_XML);
		else
			return null ;
	}

	public IFunction getBuiltinFunction(Shell shell, String name,SourceLocation loc) {
		
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

				IFunction cls = m.getFunctionClass( pair.getRight() );
				if( cls != null ){
					// cls.setLocation( loc );
				
					return cls ;
				}

			}
			return null;
		}
			
		/* 
		 * Try all default modules 
		 */
		for( Module m : modules ){
			if( m.isDefault() ){
				
				IFunction cls = m.getFunctionClass( name);
				if( cls != null ){
					// cls.setLocation(loc);
					return cls ;
				}
			}
		}
		
			
		return null  ;	
	}

	public ICommand getScript(Shell shell, File script, boolean bSourceMode, SourceLocation loc) throws CoreException, IOException {
		return getScript( shell , script.getAbsolutePath() , new FileInputStream(script) , bSourceMode , loc );
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
