/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import net.sf.saxon.query.ModuleURIResolver;
import net.sf.saxon.s9api.Processor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ExitOnErrorException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Path;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XDynamicVariable;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.FunctionDefinition;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.grammar.ShellParser;
import org.xmlsh.sh.grammar.ShellParserReader;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.NullOutputStream;
import org.xmlsh.util.SessionEnvironment;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

public class Shell {
	
	private static Logger mLogger = LogManager.getLogger(Shell.class);
	private 	ShellOpts	mOpts;
	
	private		FunctionDefinitions mFunctions = null;
	private		XEnvironment	mEnv  = null;
	private		List<XValue> 	mArgs = new ArrayList<XValue>();
	private		InputStream	mCommandInput = null;
	private		String	mArg0 = "xmlsh";
	private		SessionEnvironment mSession = null ;
	
	// Set to non null until exit or EOF
	private 	Integer mExitVal = null;
	private		Integer mReturnVal = null;

	
	private		int	    mStatus = 0;	// $? variable
	
	private 	String  mSavedCD = null;
	

	private		List<ShellThread>	mChildren = new ArrayList<ShellThread>();
	private 	boolean mIsInteractive = false ;
	private		long	mLastThreadId = 0;
	
	private		Stack<ControlLoop>  mControlStack = new Stack<ControlLoop>();
	 
	// Depth of conditions used for 'throw on error'
	private int 	mConditionDepth = 0;

	private		Modules		mModules	= null;
	
	// current module
	private		Module	mModule = null;

	
	/*
	 * Initializtion statics
	 */
	private		static 	boolean			bInitialized = false ;
	private		static	Properties		mSavedSystemProperties;
	private		static Processor		mProcessor = null;
	private		static	ModuleURIResolver	mModuleURIResolver = null ;
	
	
	/**
	 * Must call initialize atleast once, protects against multiple initializations 
	 */
	public	static	void 	initialize()
	{

		if( bInitialized )
			return ;
		
		Logging.configureLogger();

		mLogger.info("xmlsh initialize");
		
		/*
	     * Workaround a saxon bug - pre-initialize processor
	     */
		 getProcessor();
		 
		 
		 // Can only be called once per process
		 try {
			 URL.setURLStreamHandlerFactory(new ShellURLFactory() );
		 
		 } 
		 catch( Error e )
		 {
			 // mLogger.debug("Exception trying to seURLStreamHandlerFactory" , e );
		 }
		 
		 
		mSavedSystemProperties = System.getProperties();
		SystemEnvironment.getInstance().setProperty("user.dir", System.getProperty("user.dir"));
		System.setProperties( new SystemProperties(System.getProperties()));
		// PropertyConfigurator.configure(Shell.class.getResource("log4j.properties"));
	

	}
	
	
	public static void uninitialize()
	{
		if( ! bInitialized )
			return ;
		
		
		mProcessor = null ;
		System.setProperties( mSavedSystemProperties );
		mSavedSystemProperties = null ;
		SystemEnvironment.uninitialize();
		ShellContext.set(null);
		bInitialized = false ;
		
		
	}
	
	
	static {
		initialize();
	
	}

	/*
	 * New top level shell
	 */
	public Shell( ) throws IOException, CoreException
	{
		this( true );
		
	}
	
	public Shell(boolean bUseStdio) throws IOException, CoreException
	{
		mOpts = new ShellOpts();
		mSavedCD = System.getProperty("user.dir");
		mEnv =  new XEnvironment(this,bUseStdio);
		mModules = new Modules();
		mSession = new SessionEnvironment();
		// Add xmlsh commands 
		mModules.declare( new Module( null , "xmlsh" , "org.xmlsh.commands.internal", CommandFactory.kCOMMANDS_HELP_XML));
		
		setGlobalVars();
		
		mModule = null ; // no current module
		
		ShellContext.set(this);	// cur thread active shell
		
		
	}
	
	/*
	 * Populate the environment with any global variables
	 */
	
	private void setGlobalVars() throws InvalidArgumentException {
	    
		
		Map<String,String> 	env = System.getenv();
		

		for( Map.Entry<String,String > entry : env.entrySet() ){

			String name = entry.getKey();
			if( Util.isPath(name) )
				continue ;
			if( Util.isBlank(name))
				continue ;
			if( ! name.matches("^[a-zA-Z_0-9]+$"))
				continue ;
			
			// Ignore PS1
			if( name.equals("PS1"))
				continue ;
			
			getEnv().setVar( new XVariable( name , new XValue(entry.getValue()) , EnumSet.of(XVarFlag.EXPORT )),false );
			
			
		}
		
		
		// Export path to shell path
	    String path = Util.toJavaPath(System.getenv("PATH"));
	    	getEnv().setVar( new XVariable("PATH", 
	    			Util.isBlank(path) ? new XValue() : new XValue(path.split(File.pathSeparator))) , false );
	
	    String xpath = Util.toJavaPath(System.getenv("XPATH"));
	    getEnv().setVar( new XVariable("XPATH", 
	    		Util.isBlank(xpath) ? new XValue() : new XValue(xpath.split(File.pathSeparator))) , false );
	
		
		getEnv().setVar(
				new XDynamicVariable("PWD" , EnumSet.of( XVarFlag.READONLY , XVarFlag.XEXPR )) { 
					public XValue getValue() 
					{
						return new XValue( Util.toJavaPath(getEnv().getCurdir().getAbsolutePath()) ) ;
					}
					
				}
				
				, false 
		);
		
		getEnv().setVar("TMPDIR" , Util.toJavaPath(System.getProperty("java.io.tmpdir")), false );
		
		if( getEnv().getVar("HOME") == null )
			getEnv().setVar("HOME" , Util.toJavaPath(System.getProperty("user.home")), false );
		
		
	}

	/*
	 * Cloned shell for sub-thread execution
	 */
	private Shell( Shell that ) throws IOException
	{
		mOpts = new ShellOpts(that.mOpts);
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
		
		mModule = that.mModule;
		
		// Pass through the Session Enviornment, keep a reference
		mSession = that.mSession;
		mSession.addRef();
		
		
		// Cloning shells doesnt save the condition depth
		// mConditionDepth = that.mConditionDepth;
		
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
		if( mEnv != null ){
			mEnv.close();
			mEnv = null ;
		}
		if( mSavedCD != null )
			SystemEnvironment.getInstance().setProperty("user.dir", mSavedCD);
		if( mSession != null ){
			mSession.release();
			mSession = null ;
		}
	
	
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

	public SessionEnvironment getSession()
	{
		return mSession ;
	}

	public 		Command	parseEval( String scmd ) throws CoreException 
	{

		InputStream save = mCommandInput;
		InputStream is = null;

		try {
			is = Util.toInputStream(scmd, getSerializeOpts());
			mCommandInput = is ;
			ShellParser parser= new ShellParser(new ShellParserReader(mCommandInput,getTextEncoding()));
			
	      	Command c = parser.script();
	      	return c;
			
		
		}  catch ( Exception e )
		{
			throw new CoreException("Exception parsing command: " + scmd , e );
		}
		
		
		finally {
			mCommandInput = save;
			Util.safeClose(is);
			
		}

	
	}
	
	
	public		int		runScript( InputStream stream, String source ) throws ParseException, UnsupportedEncodingException, ThrowException
	{
		
		InputStream save = mCommandInput;
		mCommandInput = stream ;
		ShellParser parser= new ShellParser(new ShellParserReader(mCommandInput,getTextEncoding()), source );
		int ret = 0;
		try {
			while( mExitVal == null && mReturnVal == null  ){
		      	Command c = parser.command_line();
		      	if( c == null )
		      		break;
		      
		    	
		      	if( mOpts.mVerbose ){
		      		String s  = c.toString(false);
		      		if( s.length() > 0){
		      			SourceLocation loc = c.getLocation();
		      			if( loc != null ){
		      				String sLoc = loc.toString();
		      				mLogger.info(sLoc);
		      				printErr( "- " + sLoc );
		      				printErr(s );
		      			} else 
		      		
		      				printErr( "- " + s );
		      		}
		      	}
		      	
		      	ret = exec( c );
			}
					
			
		
		} 
		catch( ThrowException e )
		{
			// mLogger.info("Rethrowing throw exception",e);
			throw e ;	// rethrow 
			
		}
		catch( ExitOnErrorException e )
		{
			// Caught Exit on error from a script
			ret = e.getValue();
			
		}
		
		
		catch (Exception e) {
	       // System.out.println("NOK.");
	        printErr(e.getMessage());
	        mLogger.error("Exception parsing statement" , e );
	        parser.ReInit(new ShellParserReader(mCommandInput,getTextEncoding()), source );
	      } catch (Error e) {
	       //  System.out.println("Error");
	        printErr(e.getMessage());
	        mLogger.error("Exception parsing statement" , e );
	        parser.ReInit(new ShellParserReader(mCommandInput,getTextEncoding()), source);
	
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
	
	
	
	
	public String getTextEncoding() {
		return getSerializeOpts().getText_encoding();
	}

	public		int		interactive(String rcfile ) throws Exception
	{
		mIsInteractive = true ;
		int		ret = 0;
		
		
		// Try to source the rcfile 
		if( rcfile != null ){
			
			File script = this.getFile(rcfile);
			if( script.exists() && script.canRead() ){

				ICommand icmd = CommandFactory.getInstance().getScript(this, script ,true, null );
				if( icmd != null )
					icmd.run(this, rcfile , null);
	    	}
		}
		
		
		
		
		setCommandInput();
		
		
		// ShellParser parser= new ShellParser(mCommandInput,Shell.getEncoding());
		ShellParser parser= new ShellParser(new ShellParserReader(mCommandInput,getTextEncoding()));
		
		while (mExitVal == null) {
			
			  System.out.print(getPS1());
			  Command c = null ;
		      try {
		      	c = parser.command_line();
		      	if( c == null )
		      		break;
		      	
		      	if( mOpts.mVerbose ){
		      		String s  = c.toString(false);
		      		if( s.length() > 0){
		      			SourceLocation loc  = c.getLocation();
		      			if( loc != null )	{
		      				printErr("- " + loc.toString());
		      				printErr(s);
		      			} else
		      				printErr( "- " + s );
		      		}
		      	}
		      	
		      	
		      	ret = exec( c );
		      	
		      	// PrintWriter out = new PrintWriter( System.out );
		      	//s.print(out);
		      	//out.flush();
		      	
		      } 
		      catch (ThrowException e) {
		        printErr("Ignoring thrown value: " + e.getMessage());
		        mLogger.error("Ignoring throw value",e);
		        parser.ReInit(new ShellParserReader(mCommandInput,getTextEncoding()));
		      }
		      catch (Exception e) {
		    	

		        SourceLocation loc = c != null ? c.getLocation() : null ;
		        
		        if( loc != null ){
		        	String sLoc = loc.toString();
		        	mLogger.info(loc.toString());
		        	printErr( sLoc );
		        }

		        printErr(e.getMessage());
		        mLogger.error("Exception parsing statement",e);
		        parser.ReInit(new ShellParserReader(mCommandInput,getTextEncoding()));
		      } catch (Error e) {
		        printErr("Error: " + e.getMessage());
		        SourceLocation loc = c != null ? c.getLocation() : null ;
		        mLogger.error("Exception parsing statement",e);
		        if( loc != null ){
		        	String sLoc = loc.toString();
		        
		        	mLogger.info(loc.toString());
		        	printErr( sLoc );
		        }
		        parser.ReInit(new ShellParserReader(mCommandInput,getTextEncoding()));

		      } 
		      
		}
		if( mExitVal != null )
			ret = mExitVal.intValue();
		return ret;
	}


	private String getPS1() throws IOException, CoreException {
		
		XValue ps1 = getEnv().getVarValue("PS1");
		if( ps1 == null )
			return "$ ";
		String sps1 = ps1.toString();
		if( !Util.isBlank(sps1))
			sps1 = expandString(sps1, false,null);
		
		return sps1;

		
		
		
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

					if( constructor != null ){
						mCommandInput = (InputStream) constructor.newInstance(jline);
						// System.err.println("using jline");
					}
					
				}
			}
				
				
		} catch (Exception e1) {
			mLogger.info("Exception loading jline");
			
		}
		if( mCommandInput == null )
			mCommandInput = System.in;

	
	}
	
	
	/*
	 * Main entry point for executing commands.
	 * All command execution should go through this entry point
	 * 
	 * Handles background shell ("&") 
	 * Handles "throw on error" (-e) 
	 * 
	 * 
	 */

	
	public int exec(Command c) throws ThrowException, ExitOnErrorException {
		
		if( mOpts.mExec){
			String out = c.toString(true);
			if( out.length() > 0 ){
				SourceLocation loc = c.getLocation();
				
				if( loc != null ) {
					printErr("+ " + loc.toString());
					printErr( out );
				}
				
				else
					printErr("+ " + out);
			}
			
		
		}
		
		try {
		
			if( c.isWait()){
				// Execute forground command 
				mStatus = c.exec(this);
				
				// If not success then may throw if option 'throw on error' is set (-e)
				if( mStatus != 0 && mOpts.mThrowOnError && c.isSimple()  ){
					if( ! isInCommandConndition() )
						throw new ExitOnErrorException( mStatus);
					
					
					
				}
				return mStatus ;
				
				
			}
			
			ShellThread sht = new ShellThread( new Shell(this) , this , c);
			
			if( isInteractive() )
				printErr( "" + sht.getId() );
			
			addJob( sht );
			sht.start();

			return mStatus = 0;
		} 
		catch( ThrowException e ){
			// mLogger.info("Rethrowing ThrowException",e);
			throw e ;
		}
		catch( ExitOnErrorException e ){
			// rethrow 
			throw e ;
		}
		
		catch( Exception e )
		{
			// printErr("Exception running: " + c.toString(true) + "\n" +  e.toString() );
			mLogger.error("Exception running command: " + c.toString(false) , e );
			mStatus = -1;
			// If not success then may throw if option 'throw on error' is set (-e)
			if( mStatus != 0 && mOpts.mThrowOnError && c.isSimple()  ){
				if( ! isInCommandConndition() )
					throw new ThrowException( new  XValue(mStatus));
			}
			return mStatus ;
			
		}
		
	}
	
	/*
	 * Returns TRUE if the shell is currently in a condition 
	 */

	public boolean isInCommandConndition() {
		return mConditionDepth > 0  ;
	}
	// Enter a condition 
	private void pushCondition()
	{
		mConditionDepth++;
	}
	private void popCondition()
	{
		mConditionDepth--;
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
			out = new PrintWriter( 
					new BufferedWriter(
							new OutputStreamWriter(getEnv().getStderr().asOutputStream(getSerializeOpts()), getSerializeOpts().getText_encoding())
						)
					 );
		} catch (IOException e) {
			mLogger.error("Exception printing err:" + s , e );
			return ;
		}
		out.println(s);

		out.flush();
		out.close();
		
	}
	public void printOut(String s)  {
		PrintWriter out;
		try {
			out = new PrintWriter( 
					new BufferedWriter(
							new OutputStreamWriter(getEnv().getStdout().asOutputStream(getSerializeOpts()), getSerializeOpts().getText_encoding())
						)
					 );
		} catch (IOException e) {
			mLogger.error("Exception writing output: " + s , e );
			return;
		}
		out.println(s);
		out.flush();
		out.close();
		
	}
	public void printErr(String s,Exception e) {
		PrintWriter out;
		try {
			out = getEnv().getStderr().asPrintWriter(getSerializeOpts());
		} catch (IOException e1) {
			mLogger.error("Exception writing output: " + s , e );
			return ;
		}
		out.println(s);
		out.println(e.getMessage());
		
		out.flush();
		out.close();
	}

	public static void main(String argv[]) throws Exception {
	 	List<XValue> vargs = new ArrayList<XValue>(argv.length);
	 	for( String a : argv)
	 		vargs.add( new XValue(a));
		
		org.xmlsh.commands.builtin.xmlsh cmd = new org.xmlsh.commands.builtin.xmlsh(true);
		
		Shell shell = new Shell();
		int ret = -1;
		try {
			ret = cmd.run(shell , "xmlsh" , vargs);
		} finally {
			shell.close();
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
		return getPath("PATH",true);
	}
	
	
	public Path getPath(String var, boolean bSeqVar ){
		XValue	pathVar = getEnv().getVarValue(var);
		if( pathVar == null )
			return new Path();
		if( bSeqVar )
			return new Path( pathVar );
		else
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
		mArgs = args ;
		
		
	}
	
	
	public File getExplicitFile(String name, boolean mustExist ) throws IOException {
		return getExplicitFile( null , name,mustExist );
	}
	
	
	public File getExplicitFile(File dir , String name, boolean mustExist ) throws IOException {
	
	
		File file=null;
		try {
			file = new File( dir , name).getCanonicalFile();
			if(  mustExist && ! file.exists() )
				return null;

		} 
		
		catch( IOException e ){
			// Ignore IOExceptions trying to get a file because it is typically
			// an invalid name like foo:bar
			return null;
		}
		
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
		if(  mExitVal != null || mReturnVal != null  )
			return false ;
		
		// If the top control stack is break then stop
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

	public List<XValue> expand(String s, boolean bExpandSequences , boolean bExpandWild , boolean bExpandWords  , SourceLocation loc  ) throws IOException, CoreException {
		Expander e = new Expander( this , loc );
		List<XValue> result =  e.expand(s,bExpandWild, bExpandWords );
		if( bExpandSequences )
			result = Util.expandSequences( result );
		else
			result = Util.combineSequence( result );
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

	
	public File getFile(File dir, String file) throws IOException {
		return getExplicitFile( dir, file , false);
	}

	
	public File getFile(String fname) throws IOException {
		return getExplicitFile( fname , false);
	}
	
	public File getFile(XValue fvalue) throws IOException {
		return getFile( fvalue.toString());
	}
	

	public String expandString(String value, boolean bExpandWild , SourceLocation loc ) throws IOException, CoreException {
		List<XValue> ret = expand(value,false,bExpandWild, false, loc  );
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
	public	XValue	expand( String value , boolean bExpandWild , boolean bExpandWords , SourceLocation loc ) throws IOException, CoreException {
			List<XValue> ret = expand(value,false, bExpandWild , bExpandWords, loc  );
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
			mProcessor.registerExtensionFunction(new EvalDefinition() );

			
			
		}
		
		return mProcessor;
	}

	public synchronized void removeJob(ShellThread job) {
		mChildren.remove(job);
		notify();
		
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
	
	/* 
	 * Waits until there are "at most n" running children of this shell
	 */
	public synchronized void waitAtMostChildren(int n)
	{
		while( mChildren.size() > n ){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
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
	public void importModule(String moduledef,  List<XValue> init) throws  CoreException {

		
		
		mModules.declare(this, moduledef,  init );
		
	}
	
	public void importPackage(String prefix ,String name , String pkg ) throws CoreException {
		String sHelp = pkg.replace('.', '/') + "/commands.xml";
		
		mModules.declare( new Module( prefix , name , pkg ,  sHelp));
	}

	public URL getURL( String file ) throws MalformedURLException, IOException
	{
		URL url = Util.tryURL(file);
		if( url == null )
			url = getFile(file).toURI().toURL();
		return url;
			
	}
	
	
	
	public InputStream getInputStream(String file) throws FileNotFoundException, IOException {
		/*
		 * Special case to support /dev/null file on Windows systems
		 * Doesnt hurt on unix either to fake this out instead of using the OS
		 */
		if( file.equals("/dev/null") ){
			
			return new NullInputStream();
			
		}
		
		URL url = Util.tryURL(file);
		
		if( url != null ){

				return url.openStream();
			
		}
		else
			return new FileInputStream(getFile(file));
	}

	public OutputStream getOutputStream(String file, boolean append) throws FileNotFoundException, IOException
	{
		if( file.equals("/dev/null")){
			return new NullOutputStream();
		}
	
		else
		{
			URL url = Util.tryURL(file);
			if( url != null )

				return url.openConnection().getOutputStream();
			
		}
		
		
		return  new FileOutputStream(getFile(file),append);
	}


	public void setOption(String name, boolean flag) {
		mOpts.setOption(name,flag);
		
	}

	public void setOption(String name, XValue value) throws InvalidArgumentException {
		mOpts.setOption(name,value);
		
	}

	public void setModule(Module module) {
		mModule = module ;
		
	}

	public Module getModule() {
		return mModule ;
	}

	/**
	 * @return the opts
	 */
	public ShellOpts getOpts() {
		return mOpts;
	}


	/* Executes a command as a condition so that it doesnt throw 
	 * an exception if errors
	 */
	public int execCondition(Command left) throws ThrowException, ExitOnErrorException {
		
		pushCondition();
		try {
			return exec( left );
		} 
		
		
		finally {
			popCondition();
		}

	}

	/*
	 * Locate a resource in this shell, or in any of the modules
	 */

	public URL getResource(String res) {
		URL url = getClass().getResource(res);
		if( url != null )
			return url;
		
		for( Module m : mModules ){
			url = m.getResource(res);
			if( url != null )
				return url ;
		}
		return null;
	
	}


	public void setOptions(Options opts) throws InvalidArgumentException {
		for( OptionValue ov : opts.getOpts()){
			setOption(ov);
		}
		
	}


	private void setOption(OptionValue ov) throws InvalidArgumentException {
		mOpts.setOption(ov);

	}


	public SerializeOpts getSerializeOpts(Options opts) throws InvalidArgumentException {
		if( opts == null || opts.getOpts() == null )
			return mOpts.mSerialize;
		
		SerializeOpts sopts = mOpts.mSerialize.clone();
		sopts.setOptions(opts);
		return sopts;
	}

	public SerializeOpts getSerializeOpts()
	{
		return mOpts.mSerialize;
		
	}


	public Variables pushLocalVars() {
		return mEnv.pushLocalVars();
		
	}


	public void popLocalVars(Variables vars ) {
		mEnv.popLocalVars( vars );
		
	}







	
	
	
}
//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
