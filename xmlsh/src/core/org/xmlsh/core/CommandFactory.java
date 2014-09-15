/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.xmlsh.builtin.commands.colon;
import org.xmlsh.builtin.commands.declare;
import org.xmlsh.builtin.commands.echo;
import org.xmlsh.builtin.commands.eval;
import org.xmlsh.builtin.commands.exit;
import org.xmlsh.builtin.commands.jobs;
import org.xmlsh.builtin.commands.log;
import org.xmlsh.builtin.commands.printvar;
import org.xmlsh.builtin.commands.read;
import org.xmlsh.builtin.commands.require;
import org.xmlsh.builtin.commands.set;
import org.xmlsh.builtin.commands.shift;
import org.xmlsh.builtin.commands.source;
import org.xmlsh.builtin.commands.test;
import org.xmlsh.builtin.commands.trap;
import org.xmlsh.builtin.commands.unset;
import org.xmlsh.builtin.commands.wait;
import org.xmlsh.builtin.commands.xbreak;
import org.xmlsh.builtin.commands.xcd;
import org.xmlsh.builtin.commands.xcontinue;
import org.xmlsh.builtin.commands.xecho;
import org.xmlsh.builtin.commands.xfalse;
import org.xmlsh.builtin.commands.ximport;
import org.xmlsh.builtin.commands.xmkpipe;
import org.xmlsh.builtin.commands.xmlsh;
import org.xmlsh.builtin.commands.xmlshui;
import org.xmlsh.builtin.commands.xread;
import org.xmlsh.builtin.commands.xthrow;
import org.xmlsh.builtin.commands.xtrue;
import org.xmlsh.builtin.commands.xtype;
import org.xmlsh.builtin.commands.xversion;
import org.xmlsh.builtin.commands.xwhich;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.java.commands.jset;
import org.xmlsh.json.commands.jsonread;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.ModuleHandle;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.text.commands.readconfig;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

public abstract class CommandFactory {
	public static final String kCOMMANDS_HELP_XML = "/org/xmlsh/resources/help/commands.xml";
	public static final String kFUNCTIONS_HELP_XML = "/org/xmlsh/resources/help/functions.xml";

	private static Logger mLogger = org.apache.logging.log4j.LogManager
			.getLogger(CommandFactory.class);
	private static CommandFactory _instance = null;

	private static HashMap<String, Class<? extends AbstractCommand>> mBuiltinCommands = new HashMap<>();

	private static void addBuiltinCommand(String name,
			Class<? extends AbstractCommand> cls) {
		mBuiltinCommands.put(name, cls);
	}

    static  {
		addBuiltinCommand("cd", xcd.class);
		addBuiltinCommand("xecho", xecho.class);
		addBuiltinCommand("echo", echo.class);
		addBuiltinCommand("false", xfalse.class);
		addBuiltinCommand("true", xtrue.class);
		addBuiltinCommand("set", set.class);
		addBuiltinCommand(".", source.class);
		addBuiltinCommand("source", source.class);
		addBuiltinCommand("exit", exit.class);
		addBuiltinCommand(":", colon.class);
		addBuiltinCommand("[", test.class);
		addBuiltinCommand("test", test.class);
		addBuiltinCommand("shift", shift.class);
		addBuiltinCommand("read", read.class);
		addBuiltinCommand("xread", xread.class);
		addBuiltinCommand("unset", unset.class);
		addBuiltinCommand("xwhich", xwhich.class);
		addBuiltinCommand("xversion", xversion.class);
		addBuiltinCommand("jobs", jobs.class);
		addBuiltinCommand("wait", wait.class);
		addBuiltinCommand("break", xbreak.class);
		addBuiltinCommand("continue", xcontinue.class);
		addBuiltinCommand("eval", eval.class);
		addBuiltinCommand("declare", declare.class);
		addBuiltinCommand("import", ximport.class);
		addBuiltinCommand("xmlsh", xmlsh.class);
		addBuiltinCommand("throw", xthrow.class);
		addBuiltinCommand("log", log.class);
		addBuiltinCommand("xtype", xtype.class);
		addBuiltinCommand("require", require.class);
		addBuiltinCommand("jset", jset.class);
		addBuiltinCommand("xmlshui", xmlshui.class);
		addBuiltinCommand("xmkpipe", xmkpipe.class);
		addBuiltinCommand("printvar", printvar.class);
		addBuiltinCommand("jsonread", jsonread.class);
		addBuiltinCommand("propread", readconfig.class);
		addBuiltinCommand("trap", trap.class);

	}


	public static ICommand getCommand(Shell shell, String name, SourceLocation loc)
			throws IOException, CoreException {
		mLogger.entry(shell, name, loc);

		ICommand cmd = getCommandFromFunction(shell, name, loc);
		if (cmd == null)
			cmd = getBuiltin(shell, name, loc);
		if (cmd == null)
			cmd = getModuleCommand(shell, name, loc);
		if (cmd == null)
			cmd = getScript(shell, name, SourceMode.RUN, loc);
		if (cmd == null)
			cmd = getExternal(shell, name, loc);

		return mLogger.exit(cmd);

	}

	/*
	 * Gets an External command of given name by looking through the External
	 * Path
	 */

	private static ICommand getCommandFromFunction(Shell shell, String name,
			SourceLocation loc) {

		mLogger.entry(shell, name);
		IFunctionDecl func = shell.getFunctionDecl(name);
		
		if (func != null)
			return mLogger.exit(new FunctionCommand(func.getModule(), func
					.getName(), func.getBody(), loc));
		return mLogger.exit(null);
	}

	private static ICommand getExternal(Shell shell, String name, SourceLocation loc)
			throws IOException {
		mLogger.entry(shell, name);

		File cmdFile = null;

		if (Util.hasDirectory(name)) {

			cmdFile = shell.getExplicitFile(name, true,true);
			if (cmdFile == null && !name.endsWith(".exe"))
				cmdFile = shell.getExplicitFile(name + ".exe", true);
		}

		if (cmdFile == null) {
			Path path = shell.getExternalPath();
			cmdFile = path.getFirstFileInPath(shell, name,true);
			if (cmdFile == null && !name.endsWith(".exe"))
				cmdFile = path.getFirstFileInPath(shell, name + ".exe",true);

		}

		if (cmdFile == null)
			return mLogger.exit(null);

		return mLogger
				.exit(new ExternalCommand(cmdFile, loc, shell.getModule()));

	}

	private static ICommand getModuleCommand(Shell shell, String name,
			SourceLocation loc) throws IOException {

		mLogger.entry(shell, name);

		StringPair pair = new StringPair(name, ':');

		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			ModuleHandle m ;
			mLogger.trace("found prefix - trying command by prefix: ", pair);
			
			if( Util.isBlank(pair.getLeft()) ){
				m = shell.getModule()	;
				mLogger.trace("blank prefix - use current module",m);
			} else {
					m = shell.getModuleByPrefix(pair.getLeft());
					mLogger.debug("Preix module : " , m );
			}
			
			// Allow C:/xxx/yyy to work
			// May look like a namespace but isnt
	
			if (m != null) {
				mLogger.trace("Found module - try getting command" , m , pair.getRight());
				ICommand cls = m.get().getCommand(pair.getRight());
				if (cls != null) {
					mLogger.debug("Command Class found: " , cls );
					cls.setLocation(loc);
					return cls;
				}
	
				return mLogger.exit(null);
			}
		}

		/*
		 * Try all default modules
		 */
	   mLogger.debug("Try default modules");
		for (ModuleHandle m : shell.getDefaultModules() ) {
			    assert( ! m.isNull() );
				ICommand cls = m.get().getCommand(name);
				if (cls != null) {
					cls.setLocation(loc);
					return mLogger.exit(cls);
				}
			}

		return mLogger.exit(null);

	}
	

	private static IFunction getModuleFunction(Shell shell, String name,
			SourceLocation loc) throws IOException {

		mLogger.entry(shell, name);

		StringPair pair = new StringPair(name, ':');

		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			ModuleHandle m ;
			mLogger.trace("found prefix - trying command by prefix: ", pair);
			
			if( Util.isBlank(pair.getLeft()) ){
				m = shell.getModule()	;
				mLogger.trace("blank prefix - use current module",m);
			} else {
					m = shell.getModuleByPrefix(pair.getLeft());
					mLogger.debug("Preix module : " , m );
			}
			
			// Allow C:/xxx/yyy to work
			// May look like a namespace but isnt
			if (m != null) {
	
				mLogger.trace("Found module - try getting command" , m , pair.getRight());
				IFunction cls = m.get().getFunction(pair.getRight());
				if (cls != null) {
					mLogger.debug("Command Class found: " , cls );
					return cls;
				}
	
				return mLogger.exit(null);
			}
		}

		/*
		 * Try all default modules
		 */
	   mLogger.debug("Try default modules");
		for (ModuleHandle m : shell.getDefaultModules() ) {
			    assert( ! m.isNull() );
				IFunction cls = m.get().getFunction(name);
				if (cls != null) {
					return mLogger.exit(cls);
				}
			}

		return mLogger.exit(null);

	}
	
	

	/*
	 * public ScriptCommand getScript(Shell shell, String name , InputStream is
	 * , SourceMode sourceMode , SourceLocation loc ) throws CoreException { if(
	 * is == null ) return null;
	 * 
	 * return new ScriptCommand( name , is , sourceMode , null );
	 * 
	 * }
	 */

	public static ScriptSource getScriptSource(Shell shell, String name,
			SourceMode sourceMode, XValue at) throws IOException, CoreException {
		mLogger.entry(shell, name,sourceMode);

		File scriptFile = null;

		// If name has a scheme try that first
		URL url = Util.tryURL(name);
		if (url != null){
			mLogger.debug("script has URL {} ", url );
			return mLogger.exit(getScriptSource(shell, url, name));
		}
		// If ends with .xsh OR we are in source mode try it
		if (name.endsWith(".xsh") || (sourceMode == SourceMode.SOURCE || sourceMode == SourceMode.IMPORT))
			scriptFile = shell.getExplicitFile(name, true,true);
		
		
		if (scriptFile == null && Util.hasDirectory(name)) {
			// try adding a .xsh
			if ( !name.endsWith(".xsh"))
				scriptFile = shell.getExplicitFile(name + ".xsh", true,true );
			
		} else if (scriptFile == null) {

			// searh in XPATH for include/source and XMODPATH for modules 
			Path path = null ;
			switch (sourceMode) {
			case IMPORT:
				path = shell.getPath(ShellConstants.XMODPATH, true);
				break;
			case RUN:
			case SOURCE:
			case VALIDATE:
			default:
				path = shell.getPath(ShellConstants.XPATH, true);
				break ;
			}
			
			scriptFile = path.getFirstFileInPath(shell, name,true);
			if (scriptFile == null && !name.endsWith(".xsh"))
				scriptFile = path.getFirstFileInPath(shell, name + ".xsh",true);
		}
		if (scriptFile == null)
			return mLogger.exit(null);
		mLogger.debug("getting script source from file {}" , scriptFile);
		ScriptSource ss = getScriptSource(shell, scriptFile.toURI().toURL(),
				scriptFile.getPath());
		return mLogger.exit(ss);

	}

	public static ScriptCommand getScript(Shell shell, String name,
			SourceMode sourceMode, SourceLocation loc) throws IOException,
			CoreException {
		mLogger.entry(shell, name,sourceMode);

		ScriptSource source = getScriptSource(shell, name, sourceMode,null);
		if (source == null)
			return mLogger.exit( null);
		return mLogger.exit( new ScriptCommand(source, sourceMode, loc, shell.getModule()));

	}

	private static ICommand getBuiltin(Shell shell, String name, SourceLocation loc) {
		mLogger.entry(shell, name);

		Class<?> cls = mBuiltinCommands.get(name);
		if (cls != null) {
			mLogger.debug("Creating Command from class {} ",cls);
			try {
				AbstractCommand b = (AbstractCommand) cls.newInstance();
				b.setLocation(loc);
				return b;
			} catch (Exception e) {
				mLogger.debug("caught exception" ,  e );
				return mLogger.exit(  null	 	);
			}
		} else
			return mLogger.exit(  null);
	}

	public static URL getHelpURL(Shell shell, String name) {

		URL url = null;

		if (url == null)
			url = getBuiltinHelpURL(shell, name);

		if (url == null)
			url = getNativeHelpURL(shell, name);

		return url;

	}

	private static URL getNativeHelpURL(Shell shell, String name) {

		StringPair pair = new StringPair(name, ':');

		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			ModuleHandle m = Util.isBlank(pair.getLeft()) ? shell.getModule()
					: shell.getModuleByPrefix(pair.getLeft());
			// Allow C:/xxx/yyy to work
			// May look like a namespace but isnt

			if (m != null && m.get().hasHelp(pair.getRight()))
				return m.get().getHelpURL();
			return null;
		}

		/*
		 * Try all default modules
		 */
		for (ModuleHandle m : shell.getDefaultModules()) {
			assert( ! m.isNull());
			if (m.get().hasHelp(name))
				return m.get().getHelpURL();
		}

		return null;

	}

	private static URL getBuiltinHelpURL(Shell shell, String name) {
		if (mBuiltinCommands.containsKey(name))
			return shell.getResource(kCOMMANDS_HELP_XML);
		else
			return null;
	}

	public static IFunction getBuiltinFunction(Shell shell, String name) {

		mLogger.entry(shell, name);

		StringPair pair = new StringPair(name, ':');

		
		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			ModuleHandle m;
			mLogger.trace("found prefix - trying command by prefix: ", pair);
		
			if( Util.isBlank(pair.getLeft()) ){
				m = shell.getModule()	;
				mLogger.trace("blank prefix - use current module",m);
			} else {
					m = shell.getModuleByPrefix(pair.getLeft());
					mLogger.debug("Preix module : " , m );
			}
			// Allow C:/xxx/yyy to work
			// May look like a namespace but isnt
			
			if (m != null) {

				mLogger.debug("Found module",m);
				IFunction cls = m.get().getFunction(pair.getRight());
				if (cls != null) {
					return mLogger.exit(cls);
				}

			}
			return mLogger.exit( null);
		}

		/*
		 * Try all default modules
		 */
		
		 mLogger.debug("Try default modules");
		for ( ModuleHandle m : shell.getDefaultModules() ) {
			assert( !m.isNull());
			IFunction cls = m.get().getFunction(name);
				if (cls != null) 
					return mLogger.exit(cls);

		}

		return mLogger.exit(null);
			
	}

	public static ScriptSource getScriptSource(Shell shell, URL url, String name)
			throws CoreException, IOException {
		mLogger.entry(shell, name,url);

		return new ScriptSource(name, url, shell.getInputTextEncoding());

	}

	private static ScriptSource getScriptSource(Shell shell, File file, String name)
			throws CoreException, IOException {
		mLogger.entry(shell, name,file);

		return new ScriptSource(name, file.toURI().toURL(),
				shell.getInputTextEncoding());

	}

	public static ScriptCommand getScript(Shell shell, URL url, String name,
			SourceMode sourceMode, SourceLocation loc) throws CoreException,
			IOException {
		mLogger.entry(shell, url);

		return new ScriptCommand(getScriptSource(shell, url, name), sourceMode,
				loc, shell.getModule());

	}

	public static ScriptCommand getScript(Shell shell, File file, String name,
			SourceMode sourceMode, SourceLocation loc) throws CoreException,
			IOException {
		mLogger.entry(shell, name , file , sourceMode );

		return new ScriptCommand(getScriptSource(shell, file, name),
				sourceMode, loc, shell.getModule());

	}

	public static IFunction getFunction(Shell shell, String name, SourceLocation loc) throws IOException {
		
		 
		mLogger.entry(shell, name);
	
	  // Try builtin functions first
	  IFunction func = getBuiltinFunction(shell, name);
	
	  // global scope shell functions 
	  if(func == null){
			IFunctionDecl funcdecl = shell.getFunctionDecl(name);
			if( funcdecl != null )
			  func = funcdecl.getFunction();
		}
	  // Module scope shell functions
		if( func == null ){
			func = getModuleFunction(shell,name,loc);
		}
		
		
		
		return mLogger.exit(func );
	}
	
	

}
//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
