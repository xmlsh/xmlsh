/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
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
import org.xmlsh.core.AbstractCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ExternalCommand;
import org.xmlsh.core.FunctionCommand;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.ScriptSource;
import org.xmlsh.core.SearchPath;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.java.commands.jset;
import org.xmlsh.json.commands.jsonread;
import org.xmlsh.modules.types.config.readconfig;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.PathMatchOptions;
import org.xmlsh.util.StringPair;

import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.*;

import org.xmlsh.util.Util;

public abstract class CommandFactory {
	public static final String kCOMMANDS_HELP_XML = "/org/xmlsh/resources/help/commands.xml";
	public static final String kFUNCTIONS_HELP_XML = "/org/xmlsh/resources/help/functions.xml";

	public static Logger mLogger = org.apache.logging.log4j.LogManager
			.getLogger(CommandFactory.class);
	private static CommandFactory _instance = null;

	private static HashMap<String, Class<? extends AbstractCommand>> mBuiltinCommands = new HashMap<>();
	
	private static PathMatchOptions sExecutablePath  = new PathMatchOptions().withFlagsHidden( DIRECTORIES,HIDDEN_NAME,HIDDEN_SYS, SYSTEM ).
			withFlagsMatching(FILES,EXECUTABLE,READABLE);

	private static PathMatchOptions sExplicitPath  = new PathMatchOptions().withFlagsHidden( DIRECTORIES ).
			withFlagsMatching(FILES,READABLE);
	
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
			throws IOException, CoreException, URISyntaxException {
		mLogger.entry(shell, name, loc);

		ICommand cmd = getCommandFromFunction(shell, name, loc);
		if (cmd == null)
			cmd = getBuiltin(shell, name, loc);
		if (cmd == null)
			cmd = getModuleCommand(shell, name);
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
		IFunctionDefiniton func = shell.getFunctionDecl(name);
		
		if (func != null)
			return mLogger.exit(new FunctionCommand(func.getModule(), func
					.getName(), func.getBody(), loc));
		return mLogger.exit(null);
	}

	private static ICommand getExternal(Shell shell, String name, SourceLocation loc)
			throws IOException {
		mLogger.entry(shell, name);
		
		

		File cmdFile = null;

		if (FileUtils.hasDirectory(name)) {
			cmdFile = shell.getExplicitFile(name, true,true);
			if (cmdFile == null && !name.endsWith(".exe")  && Util.isWindows())
				cmdFile = shell.getExplicitFile(name + ".exe", sExecutablePath);
			if (cmdFile == null && !name.endsWith(".bat")  && Util.isWindows())
				cmdFile = shell.getExplicitFile(name + ".bat", sExecutablePath);
			if (cmdFile == null && !name.endsWith(".cmd")  && Util.isWindows())
				cmdFile = shell.getExplicitFile(name + ".cmd", sExecutablePath);
		}

		if (cmdFile == null) {
			SearchPath path = shell.getExternalPath();
			cmdFile = path.getFirstFileInPath(shell, name,sExecutablePath);
			if (cmdFile == null && !name.endsWith(".exe") && Util.isWindows()  )
				cmdFile = path.getFirstFileInPath(shell, name + ".exe",sExecutablePath);
			if (cmdFile == null && !name.endsWith(".bat") && Util.isWindows()  )
				cmdFile = path.getFirstFileInPath(shell, name + ".bat",sExecutablePath);
			if (cmdFile == null && !name.endsWith(".cmd") && Util.isWindows()  )
				cmdFile = path.getFirstFileInPath(shell, name + ".cmd",sExecutablePath);

		}

		if (cmdFile == null)
			return mLogger.exit(null);

		return mLogger
				.exit(new ExternalCommand(cmdFile, loc, shell.getModule()));

	}

	private static ICommand getModuleCommand(Shell shell, String name) throws IOException, URISyntaxException {

		mLogger.entry(shell, name);

		StringPair pair = new StringPair(name, ':');

		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			IModule m ;
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
				ICommand cls = m.getCommand(pair.getRight());
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
		for (IModule m : shell.getDefaultModules() ) {
			    assert( m != null );
				ICommand cls = m.getCommand(name);
				if (cls != null) {
					return mLogger.exit(cls);
				}
			}

		return mLogger.exit(null);

	}
	

	private static IFunctionExpr getModuleFunction(Shell shell, String name) throws IOException {

		mLogger.entry(shell, name);

		StringPair pair = new StringPair(name, ':');

		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			IModule m ;
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
				IFunctionExpr cls = m.getFunction(pair.getRight());
				if (cls != null) {
					mLogger.debug("Command Class found: " , cls );
					return cls;
				}

			}
			
			// Has prefix but cant find - 
			return mLogger.exit(null);
		}

		/*
		 * Try all default modules
		 */
	   mLogger.debug("Try default modules");
		for (IModule m : shell.getDefaultModules() ) {
				IFunctionExpr cls = m.getFunction(name);
				if (cls != null) {
					return mLogger.exit(cls);
				}
			}

		return mLogger.exit(null);

	}
	
	/*
	 * 
	 * Try each Path in turn, using each pathExts (or exactly if no extenions) 
	 * paths and exts may be null 
	 * If paths not null 
	 *    If exts is not null try each extension in turn (may be "" ) in the in each path 
	 * If paths is null 
	 *    Try file in curdir , with each ext (may be "") 
	 * If paths is [] do not try any path
	 * 
	 */
	public static File  findFirstFileInPaths(Shell shell  , String name , String[] exts , SearchPath [] paths )
	{		
	    mLogger.entry(shell, name, Util.traceArray(exts),  Util.traceArray(paths) );
	
		if( exts == null )
			exts = Util.toArray( "" );
		
		File file = null ;
		if( paths == null ){
			for( String ext : exts ){ 
				file = tryFile( shell , name + ext );
				if( file != null )
					return mLogger.exit(file);
			}
			return mLogger.exit(null );
		}
		for( SearchPath path : paths ){
			for( String ext : exts ){
				try {
					file = path.getFirstFileInPath(shell, name  + ext , sExplicitPath);
				} catch (IOException e) {
					mLogger.catching(e);
					continue;
				}
				if( file != null )
					return mLogger.exit(file);
			}
		}
		return mLogger.exit(null);
	}

	protected static File tryFile(Shell shell, String name) {
		mLogger.entry(shell, name);
		File file = null ;
		try {
		   file = shell.getExplicitFile(name, sExplicitPath);
	
		} 
		catch( IOException e ) {
			mLogger.catching(e);
		}
		return mLogger.exit(file);
	}

	/*
	 * Search for script in the following 
	 * If name is a URL try opening the URL as 
	 * If name ends with .xsh and in SOURCE or IMPORT 
	 * 
	 */

	public static ScriptSource getScriptSource(Shell shell, PName pname,
			SourceMode sourceMode, List<URL> at) throws IOException, CoreException, URISyntaxException {
		mLogger.entry(shell, pname,sourceMode,at);

		File scriptFile = null;

		// If at - try to use it explicitly ignoring the name for location
		URL url = null;
		ScriptSource src = null ;
		if( at != null ){
            Exception caught = null;
			for( URL u : at  ){
			   try {
					src = getScriptSource(shell, u, pname.getName());
				} catch (Exception e) {
				    caught = e ;
					mLogger.catching(e);
					continue ;
				}
			}
			if( src != null )
				return  mLogger.exit(src) ;
			mLogger.throwing( caught != null  ? caught : new FileNotFoundException(pname.toString()) );
		}
		
		// If name has a scheme try that first
		boolean hasPrefix = pname.hasPrefix(true);
		String psname = pname.toString();
		if( hasPrefix ){
			url = Util.tryURL(psname);
			if (url != null){
				mLogger.debug("script has URL {} ", url );
				return mLogger.exit(getScriptSource(shell, url, pname.getName() ));
			}
		
			// Prefix might be a drive leter -- if so try an absolute file
			if( Util.isWindows() && FileUtils.rootPathLength(FileUtils.toJavaPath(psname)) > 0 ){
				mLogger.trace("Trying name as windows file: {} " , pname );
				scriptFile = shell.getExplicitFile(pname.toString(), true );
				if( scriptFile != null && ! scriptFile.isFile())
					scriptFile = null ;
				
			} else
				mLogger.exit(null); // 
			
			/*
			 * TODO Resolve against module prefix
			 */
			/*
			if( scriptFile == null )
				return mLogger.exit(null);
	        */
		}
		
		if( scriptFile == null ){
		
			String name = psname;
			
			String ext = FileUtils.getExt( name );
			boolean bIsXsh = ".xsh".equals(ext);
			
	        if( sourceMode == SourceMode.SOURCE || sourceMode == SourceMode.IMPORT || FileUtils.hasDirectory(name)   ) {
	        	scriptFile = findFirstFileInPaths( shell , name , getExtensions( ext , ".xsh"  ) , null);
	        }
			
			
		   	if (scriptFile == null) {
	            SearchPath[] paths;
				
				// searh in XPATH for include/source and XMODPATH for modules 
				SearchPath path = null ;
				switch (sourceMode) {
				case IMPORT:
					paths = new SearchPath[] { shell.getPath(ShellConstants.XMODPATH, true) , shell.getPath(ShellConstants.XPATH, true) } ;
					break;
				case RUN:
				case SOURCE:
				case VALIDATE:
				default:
					paths =new SearchPath[] { shell.getPath(ShellConstants.XPATH, true) , shell.getPath(ShellConstants.PATH, true) } ;
					break ;
				}
				
	        	scriptFile = findFirstFileInPaths( shell , name , getExtensions( ext , ".xsh"  ) , paths );
			}
		}
		if (scriptFile == null)
			return mLogger.exit(null);
		mLogger.debug("getting script source from file {}" , scriptFile);
		ScriptSource ss = getScriptSource(shell, scriptFile.toURI().toURL(),
				scriptFile.getPath());
		return mLogger.exit(ss);

	}
	/*
	 * Return a list of extensions to try
	 * Excluding the current extension (convert to ""
	 */
	private static String[] getExtensions(String ext, String... exts) {
      List<String> list = new ArrayList<>();
      if( Util.contains( exts , ext ))
    	   list.add("");
      
      for( String e : exts )
    	  if( ! e.equals(ext) )
             list.add(e);
      return list.toArray( new String[0] );
	
	}

	public static ScriptCommand getScript(Shell shell, String name,
			SourceMode sourceMode, SourceLocation loc) throws IOException,
			CoreException, URISyntaxException {
		mLogger.entry(shell, name,sourceMode);

		ScriptSource source = getScriptSource(shell, new PName(name), sourceMode,null);
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
			IModule m = Util.isBlank(pair.getLeft()) ? shell.getModule()
					: shell.getModuleByPrefix(pair.getLeft());
			// Allow C:/xxx/yyy to work
			// May look like a namespace but isnt

			if (m != null && m.hasHelp(pair.getRight()))
				return m.getHelpURL();
			return null;
		}

		/*
		 * Try all default modules
		 */
		for (IModule m : shell.getDefaultModules()) {
			if (m.hasHelp(name))
				return m.getHelpURL();
		}

		return null;

	}

	private static URL getBuiltinHelpURL(Shell shell, String name) {
		if (mBuiltinCommands.containsKey(name))
			return shell.getResource(kCOMMANDS_HELP_XML);
		else
			return null;
	}

	public static IFunctionExpr getBuiltinFunction(Shell shell, String name) {

		mLogger.entry(shell, name);

		StringPair pair = new StringPair(name, ':');

		
		if (pair.hasLeft()) { // prefix:name , prefix non-empty
			IModule m;
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
				IFunctionExpr cls = m.getFunction(pair.getRight());
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
		for ( IModule m : shell.getDefaultModules() ) {
			IFunctionExpr cls = m.getFunction(name);
				if (cls != null) 
					return mLogger.exit(cls);

		}

		return mLogger.exit(null);
			
	}

	public static ScriptSource getScriptSource(Shell shell, URL url, String name)
			throws CoreException, IOException, URISyntaxException {
		mLogger.entry(shell, name,url);

		return new ScriptSource(name, url, shell.getInputTextEncoding());

	}

	private static ScriptSource getScriptSource(Shell shell, File file, String name)
			throws CoreException, IOException, URISyntaxException {
		mLogger.entry(shell, name,file);

		return new ScriptSource(name, file.toURI().toURL(),
				shell.getInputTextEncoding());

	}

	public static ScriptCommand getScript(Shell shell, URL url, String name,
			SourceMode sourceMode, SourceLocation loc) throws CoreException,
			IOException, URISyntaxException {
		mLogger.entry(shell, url);

		return new ScriptCommand(getScriptSource(shell, url, name), sourceMode,
				loc, shell.getModule());

	}

	public static ScriptCommand getScript(Shell shell, File file, String name,
			SourceMode sourceMode, SourceLocation loc) throws CoreException,
			IOException, URISyntaxException {
		mLogger.entry(shell, name , file , sourceMode );

		return new ScriptCommand(getScriptSource(shell, file, name),
				sourceMode, loc, shell.getModule());

	}

	public static IFunctionExpr getFunction(Shell shell, String name) throws IOException {
		
		 
		mLogger.entry(shell, name);
	
	  // Try builtin functions first
	  IFunctionExpr func = getBuiltinFunction(shell, name);
	
	  // global scope shell functions 
	  if(func == null){
			IFunctionDefiniton funcdecl = shell.getFunctionDecl(name);
			if( funcdecl != null )
			  func = funcdecl.getFunction();
		}
	  // IModule scope shell functions
		if( func == null ){
			func = getModuleFunction(shell,name);
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
