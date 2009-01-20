/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.sf.saxon.s9api.Processor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.Path;
import org.xmlsh.core.XDynamicVariable;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.EvalScriptCommand;
import org.xmlsh.sh.core.FunctionDefinition;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.grammar.ShellParser;
import org.xmlsh.sh.grammar.ShellParserReader;
import org.xmlsh.util.Util;

public class Shell {
	
	private static Logger mLogger = LogManager.getLogger(Shell.class);
	static class ShellOpts 
	{
		boolean	mVerbose = false;		// -v
		boolean mExec	 = false;		// -x
		public ShellOpts clone() {
			ShellOpts that = new ShellOpts();
			that.mVerbose = mVerbose;
			that.mExec= mExec ;
			return that;
		}
	}
	
	private 	ShellOpts	mOpts;
	
	private		FunctionDefinitions mFunctions = null;
	private		XEnvironment	mEnv  = null;
	private		List<XValue> 	mArgs = new ArrayList<XValue>();
	private		InputStream	mCommandInput = null;
	private		String	mArg0 = "xmlsh";
	
	// Set to non null until exit or EOF
	private 	Integer mExitVal = null;
	private		Integer mReturnVal = null;
	
	private		int	    mStatus = 0;	// $? variable
	private		static Processor	mProcessor = null;
	
	private 	String  mSavedCD = null;
	

	private		List<ShellThread>	mChildren = new ArrayList<ShellThread>();
	private boolean mIsInteractive = false ;
	private		long	mLastThreadId = 0;
	
	private		Stack<ControlLoop>  mControlStack = new Stack<ControlLoop>();
	

	private		Modules		mModules	= null;


	static {
		
		Logging.configureLogger();
		
		/*
	     * Workaround a saxon bug - pre-initialize processor
	     */
		 getProcessor();
	
		SystemEnvironment.getInstance().setProperty("user.dir", System.getProperty("user.dir"));
		System.setProperties( new SystemProperties(System.getProperties()));
		// PropertyConfigurator.configure(Shell.class.getResource("log4j.properties"));

	}

	/*
	 * New top level shell
	 */
	public Shell() throws IOException
	{
		mOpts = new ShellOpts();
		mSavedCD = System.getProperty("user.dir");
		mEnv =  new XEnvironment(this,true);
		mModules = new Modules();
		mModules.declare("xmlsh" , "org.xmlsh.commands");
		mModules.addDefault("xmlsh");
		
		setGlobalVars();
		
		
	}
	
	/*
	 * Populate the environment with any global variables
	 */
	
	private void setGlobalVars() {
	    
		
		Map<String,String> 	env = System.getenv();
		

		for( Map.Entry<String,String > entry : env.entrySet() ){
			String name = entry.getKey();
			if( name.equals("PATH") )
				continue ;
			
			getEnv().setVar( new XVariable( name , new XValue(entry.getValue()) , EnumSet.of(XVarFlag.EXPORT )));
			
			
		}
		
		
		// Export path to shell path
	    String path = Util.toJavaPath(System.getenv("PATH"));
	    getEnv().setVar( new XVariable("PATH", new XValue(path)));
	
		
		
		getEnv().setVar(
				new XDynamicVariable("PWD" , EnumSet.of( XVarFlag.READONLY , XVarFlag.XEXPR )) { 
					public XValue getValue() 
					{
						return new XValue( Util.toJavaPath(getEnv().getCurdir().getAbsolutePath()) ) ;
					}
					
				}
				
				
		);
		
		getEnv().setVar("TMPDIR" , Util.toJavaPath(System.getProperty("java.io.tmpdir")));
	}

	/*
	 * Cloned shell for sub-thread execution
	 */
	private Shell( Shell that ) throws IOException
	{
		mOpts = that.mOpts.clone();
		mEnv = that.getEnv().clone(this) ;
		mCommandInput = that.mCommandInput;
		mArg0 = that.mArg0;
		
		// clone $1..$N
		mArgs = new ArrayList<XValue>();
		mArgs.addAll(that.mArgs);
		
		mSavedCD = System.getProperty("user.dir");
		
		if( that.mFunctions != null )
			mFunctions = new FunctionDefinitions(that.mFunctions);
		

		
		mModules = new Modules(that.mModules );
		
	}
	
	public Shell clone()
	{
		try {
			return new Shell( this );
		} catch (IOException e) {

			printErr("Exception cloning shell",e);
			return null;
		}
	}
	
	
	public void close()
	{
		if( mEnv != null )
			mEnv.close();
		
		SystemEnvironment.getInstance().setProperty("user.dir", mSavedCD);
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	
	public XEnvironment getEnv() {
		return 	mEnv;
	}


	
	
	public		int		runScript( InputStream stream ) throws ParseException
	{
		
		InputStream save = mCommandInput;
		mCommandInput = stream ;
		ShellParser parser= new ShellParser(mCommandInput,getEncoding());
		int ret = 0;
		try {
			while( mExitVal == null && mReturnVal == null ){
		      	Command c = parser.command_line();
		      	if( c == null )
		      		break;
		      
		    	
		      	if( mOpts.mVerbose ){
		      		String s  = c.toString(false);
		      		if( s.length() > 0)
		      			printErr( "- " + s );
		      	}
		      	
		      	ret = exec( c );
			}
					
			
		
		} catch (Exception e) {
	       // System.out.println("NOK.");
	        printErr(e.getMessage());
	        mLogger.error("Exception parsing statement" , e );
	        parser.ReInit(mCommandInput,getEncoding());
	      } catch (Error e) {
	       //  System.out.println("Error");
	        printErr(e.getMessage());
	        mLogger.error("Exception parsing statement" , e );
	        parser.ReInit(mCommandInput,getEncoding());
	
	     } 
      
		
		
		finally {
			mCommandInput = save;
		}
		if( mExitVal != null )
			ret = mExitVal.intValue();
		else
		if( mReturnVal != null ){
			ret = mReturnVal.intValue();
			mReturnVal = null ;
		}
			
		return ret;
		
	}
	
	
	
	
	private		int		interactive() throws UnsupportedEncodingException
	{
		mIsInteractive = true ;
		int		ret = 0;
		
		setCommandInput();
		
		
		// ShellParser parser= new ShellParser(mCommandInput,Shell.getEncoding());
		ShellParser parser= new ShellParser(new ShellParserReader(mCommandInput,Shell.getEncoding()));
		
		while (mExitVal == null) {
			
			  System.out.print("$ ");
		      try {
		      	Command c = parser.command_line();
		      	if( c == null )
		      		break;
		      	
		      	if( mOpts.mVerbose ){
		      		String s  = c.toString(false);
		      		if( s.length() > 0)
		      			printErr( "- " + s );
		      	}
		      	
		      	
		      	ret = exec( c );
		      	
		      	// PrintWriter out = new PrintWriter( System.out );
		      	//s.print(out);
		      	//out.flush();
		      	
		      } catch (Exception e) {
		        printErr(e.getMessage());
		        mLogger.error("Exception parsing statement",e);
		        parser.ReInit(new ShellParserReader(mCommandInput,Shell.getEncoding()));
		      } catch (Error e) {
		        printErr("Error: e.getMessage()");
		        mLogger.error("Exception parsing statement",e);
		        parser.ReInit(new ShellParserReader(mCommandInput,Shell.getEncoding()));

		      } 
		      
		}
		if( mExitVal != null )
			ret = mExitVal.intValue();
		return ret;
	}

	/*
	 * Setup the mCommandInput
	 * Try to locate jline if its in the classpath and use it
	 * otherwise default to System.in
	 */
	
	
	private void setCommandInput() {
		mCommandInput = null ;
		try {
			/*
			 * import jline.ConsoleReader;
			 * import jline.ConsoleReaderInputStream;
			 */
			Class<?> consoleReaderClass = Class.forName("jline.ConsoleReader");

			if(consoleReaderClass != null  ){
				Class<?> consoleInputClass = Class.forName("jline.ConsoleReaderInputStream");
				if( consoleInputClass != null ){
					// ConsoleReader jline = new ConsoleReader(); 
					Object jline =  consoleReaderClass.newInstance();
					
					Constructor<?> constructor = consoleInputClass.getConstructor( consoleReaderClass );
					// mCommandInput = new ConsoleReaderInputStream(jline);

					if( constructor != null )
						mCommandInput = (InputStream) constructor.newInstance(jline);
				}
			}
				
				
		} catch (Exception e1) {
			mLogger.warn("Exception loading jline",e1);
			
		}
		if( mCommandInput == null )
			mCommandInput = System.in;

	
	}
	
	/*
	 * Expand a single string 
	 * 1) quote expansion
	 * 2) variable expansion
	 * 3) wildcard expansion
	 */
	
	public int exec(Command c) throws Exception {
		
		if( mOpts.mExec){
			String out = c.toString(true);
			if( out.length() > 0 )
				printErr("+ " + out);
			
		
		}
		
		try {
		
			if( c.isWait())
				return mStatus = c.exec(this);
			
			ShellThread sht = new ShellThread( new Shell(this) , this , c);
			
			if( isInteractive() )
				printErr( "" + sht.getId() );
			sht.start();
			addJob( sht );
			return mStatus = 0;
		} catch( Exception e )
		{
			printErr("Exception running: " + c.toString(true) + "\n" +  e.toString() );
			mLogger.error("Exception running command: " + c.toString(false) , e );
			return mStatus = -1;
			
		}
		
	}

	private boolean isInteractive() {
		return mIsInteractive ;
	}

	private synchronized void addJob(ShellThread sht) {
		mChildren.add(sht);
		mLastThreadId = sht.getId();
	}

	public void printErr(String s) {
		PrintWriter out;
		try {
			out = new PrintWriter( getEnv().getStderr().asOutputStream() );
		} catch (IOException e) {
			mLogger.error("Exception printing err:" + s , e );
			return ;
		}
		out.println(s);

		out.flush();
		
	}
	public void printOut(String s)  {
		PrintWriter out;
		try {
			out = new PrintWriter( getEnv().getStdout().asOutputStream() );
		} catch (IOException e) {
			mLogger.error("Exception writing output: " + s , e );
			return;
		}
		out.println(s);

		out.flush();
		
	}
	public void printErr(String s,Exception e) {
		PrintWriter out;
		try {
			out = getEnv().getStderr().asPrintWriter();
		} catch (IOException e1) {
			mLogger.error("Exception writing output: " + s , e );
			return ;
		}
		out.println(s);
		out.println(e.getMessage());
		
		out.flush();
		
	}

	public static void main(String argv[]) throws Exception {
	 	List<XValue> vargs = new ArrayList<XValue>(argv.length);
	 	for( String a : argv)
	 		vargs.add( new XValue(a));
		
		Shell shell = new Shell();
	    
		Options opts = new Options( "x,v,c:" ,  vargs );
		opts.parse();
		
		
		
		if( opts.hasOpt("v") )
    		shell.mOpts.mVerbose = true ;

    	if(opts.hasOpt("x"))
    		shell.mOpts.mExec = true ;
    	
    	String command  = null ;
    	if( opts.hasOpt("c"))
    		command = opts.getOpt("c").toString();
	    
	    int ret = 0;
	    vargs = opts.getRemainingArgs();
	    
	    
	    if(  vargs.size() == 0 ){
	    	ret = shell.interactive();
	    	
	    } else {

	     	
		    // Run command
		    if(command != null)
		    {


		    	
		    	Command cmd = new EvalScriptCommand( command );
	    		ret = shell.exec(cmd);
		    	

		    }
		    else // Run script 
		    {
		    	
		    	String scmd = vargs.get(0).toString();
		    	ICommand cmd = CommandFactory.getInstance().getScript( shell , scmd, true );
		    	if( cmd == null )
		    		shell.printErr( scmd + ": not found");
		    	else {
		    		
		    			// Run as sourced mode, in this shell ...
		    		// must set args ourselves
		    		
		    		shell.setArg0( scmd);
		    		shell.setArgs(vargs );
		    		ret = cmd.run( shell , scmd , null );
		    	}
		    	
		    	
		    }
	    	
	    }
	    
	    
	   
	    System.exit(ret);
	  }
	
	
	public void setArg0(String string) {
		mArg0 = string;
		
	}

	// Translate a shell return code to java bool
	public static boolean toBool(int intVal ) {
		return intVal == 0 ;
		
	}
	
	// Translate a java bool to a shell return code
	public static int fromBool( boolean boolVal )
	{
		return boolVal ? 0 : 1;
	}
	

	
	public Path getExternalPath(){
		XValue	pathVar = getEnv().getVarValue("PATH");
		if( pathVar == null )
			return new Path();
		return new Path( pathVar.toString().split( File.pathSeparator ));
		
	}
	
	public Path getPath(){
		XValue	pathVar = getEnv().getVarValue("XPATH");
		if( pathVar == null )
			return new Path();
		return new Path( pathVar.toString().split( File.pathSeparator ));
		
	}
	/* 
	 * Current Directory
	 */
	public File		getCurdir()
	{
		return new File( System.getProperty("user.dir"));

	}
	
	
	public  void  		setCurdir( File cd ) throws IOException
	{
		String dir = cd.getCanonicalPath();
		SystemEnvironment.getInstance().setProperty("user.dir",dir);

	
	}

	public void setArgs(List<XValue> args) {
		mArgs.clear();
		mArgs.addAll(args);
		
		
	}

	public File getExplicitFile(String name, boolean mustExist ) throws IOException {
		File file = new File( name).getCanonicalFile();
		
		
		if(  mustExist && ! file.exists() )
			return null;
		
		return file;
	}

	public List<XValue> 	getArgs() {
		return mArgs;
	}
	
	
	public void exit(int retval) {
		mExitVal = new Integer(retval);
		
	}
	
	public void exec_return(int retval) {
		mReturnVal = new Integer(retval);
		
	}
	
	
	/*
	 * Return TRUE if we should keep running on this shell
	 * Includes early termination in control stacks
	 */
	public boolean keepRunning()
	{
		// Hit exit stop 
		if(  mExitVal != null || mReturnVal != null )
			return false ;
		
		// If the top control stack is break then stopi
		if(! mControlStack.empty() ){
			ControlLoop loop = mControlStack.peek();
			if( loop.mBreak || loop.mContinue )
				return false;
		}

		return true ;
				
		
		
	}

	public String getArg0() {
		return mArg0;
	}

	public List<XValue> expand(String s, boolean bExpandSequences , boolean bExpandWild , boolean bExpandWords  ) throws IOException, CoreException {
		Expander e = new Expander( this );
		List<XValue> result =  e.expand(s,bExpandWild, bExpandWords );
		if( bExpandSequences )
			result = Util.expandSequences( result );
		return result;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return mStatus;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(int status) {
		mStatus = status;
	}

	
	public File getFile(String fname) throws IOException {
		return getExplicitFile( fname , false);
	}
	
	public File getFile(XValue fvalue) throws IOException {
		return getFile( fvalue.toString());
	}
	

	public String expandString(String value, boolean bExpandWild ) throws IOException, CoreException {
		List<XValue> ret = expand(value,false,bExpandWild, false );
		if( ret.size() == 0 )
			return "";
		else
		if( ret.size() == 1 )
			return ret.get(0).toString();
		
		StringBuffer sb = new StringBuffer();
		for( XValue v : ret ){
			if( sb.length() > 0 )
				sb.append(' ');
			sb.append( v.toString() );
		}
		return  sb.toString();
		
	}

	// Expand a word and return as a single XValue
	// Preserves sequences and expands 
	public	XValue	expand( String value , boolean bExpandWild , boolean bExpandWords ) throws IOException, CoreException {
			List<XValue> ret = expand(value,false, bExpandWild , bExpandWords );
			if( ret.size() == 0 )
				return new XValue("");
			else
			if( ret.size() == 1 )
				return ret.get(0);
			
			return new XValue( ret );

	}
	
	
	
	public void shift(int num) {
		while( ! mArgs.isEmpty() && num-- > 0 )
			mArgs.remove(0);
		
		
	}
	
	/*
	 * Returns the singleton processor for all of Xmlsh
	 */
	public static synchronized Processor getProcessor()
	{
		if( mProcessor == null ){
			mProcessor  = new Processor(false);
			// mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL, net.sf.saxon.event.Builder.LINKED_TREE);
		}
		
		return mProcessor;
	}

	public synchronized void removeJob(ShellThread job) {
		mChildren.remove(job);
		
	}
	
	/*
	 * Returns the children of the current thread
	 * copied into a collection so that it is thread safe
	 */
	
	public synchronized List<ShellThread> getChildren()
	{
		ArrayList<ShellThread> copy = new ArrayList<ShellThread>();
		copy.addAll(mChildren);
		return copy;
	}



	public long getLastThreadId() {
		// TODO Auto-generated method stub
		return mLastThreadId;
	}

	
	/*
	 * Break n levels of control stacks
	 */
	public int doBreak(int levels) 
	{
		int end = mControlStack.size() - 1 ;
		
		while( levels-- > 0 && end >= 0 )
			mControlStack.get(end--).mBreak = true ;
		
		return 0;
			
		
		
	}
	
	/*
	 * Continue n levels of control stacks
	 * 
	 */

	public int doContinue(int levels) 
	{
		int end = mControlStack.size() - 1 ;
		
		/*
		 * Break n-1 levels 
		 */
		while( levels-- > 1 && end >= 0 )
			mControlStack.get(end--).mBreak = true ;
		
		// Continue the final level
		if( end >= 0 )
			mControlStack.get(end).mContinue = true ;
		
		return 0;
	}

	public ControlLoop pushLoop() {
		ControlLoop loop = new ControlLoop();
		mControlStack.add( loop );
		return loop;
	}

	/*
	 * Pop the control stack until we hit loop, if loop isnt found (SNH) pop until empty
	 * 
	 */
	public void popLoop(ControlLoop loop) {
		
		while( ! mControlStack.empty() )
			if ( mControlStack.pop() == loop )
				break ;
	}

	public void declareFunction(FunctionDefinition func) {
		if( mFunctions == null )
			mFunctions = new FunctionDefinitions();
		mFunctions.put( func.getName() , func);
		
	}

	public FunctionDefinition getFunction(String name) {
		if( mFunctions == null )
			return null;
		return mFunctions.get(name);
	}

	public Modules getModules() {
		return mModules;
	}
	
	public static String getEncoding()
	{
		return System.getProperty("file.encoding");
		
	}


	/*
	 * Execute a command as a function body
	 * Extracts return values from the function if present
	 */
	public int execFunction(Command body) throws Exception {
		int ret = exec(body);
		if( mReturnVal != null ){
			ret = mReturnVal.intValue();
			mReturnVal = null;
		}
		return ret ;
	
	}

	/*
	 * Declare a module using the prefix=value notation
	 */
	public void importModule(String moduledef) {
		mModules.declare(moduledef);
		
	}

	public URI getURI( String file ) throws IOException, URISyntaxException
	{
		if( Util.isURIScheme(file))
			
				return new URI(file);
			
		 else
				return getFile(file).toURI();
			
	}
	
	
	
	public InputStream getInputStream(String file) throws IOException {
		if( Util.isURIScheme(file)){
			try {
				return new URI(file).toURL().openStream();
			} catch (Exception e) {
				mLogger.debug("Exception converting to URI: " + file , e);
				return null;
			}
			
		}
		else
			return new FileInputStream(getFile(file));
	}

	public OutputStream getOutputStream(String file, boolean append) throws FileNotFoundException, IOException
	{
		return  new FileOutputStream(getFile(file),append);
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
