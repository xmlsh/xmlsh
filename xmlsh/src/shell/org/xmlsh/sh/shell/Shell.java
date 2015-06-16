/**
f * $Id$
 * $Date$
 * 
 */

package org.xmlsh.sh.shell;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.saxon.s9api.Processor;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.ExitOnErrorException;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.SearchPath;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XClassLoader;
import org.xmlsh.core.XDynamicVariable;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.core.io.FileInputPort;
import org.xmlsh.core.io.FileOutputPort;
import org.xmlsh.core.io.IShellPrompt;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.core.io.ShellIO;
import org.xmlsh.core.io.StreamInputPort;
import org.xmlsh.core.io.StreamOutputPort;
import org.xmlsh.sh.core.CommandExpr;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.core.IExpression;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.core.SourceLocator;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.grammar.ShellParser;
import org.xmlsh.sh.grammar.ShellParserReader;
import org.xmlsh.sh.module.CommandFactory;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.module.ModuleConfig;
import org.xmlsh.sh.module.ModuleFactory;
import org.xmlsh.sh.module.PName;
import org.xmlsh.sh.module.RootModule;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.NullOutputStream;
import org.xmlsh.util.PathMatchOptions;
import org.xmlsh.util.SessionEnvironment;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ThreadLocalShell;

public class Shell implements AutoCloseable, Closeable , IShellPrompt {
	private static volatile int __id = 0;
	private final int _id = ++__id ;
	
	private static String[] _reservedEnvVars = {
	     ShellConstants.PATH , 
	     ShellConstants.PS1 ,
	     ShellConstants.PS2 ,
	     ShellConstants.PWD,
	     ShellConstants.VAR_RANDOM ,
	     ShellConstants.VAR_RANDOM32,
	     ShellConstants.VAR_RANDOM64 ,
	     ShellConstants.ENV_XMODPATH,
	     ShellConstants.ENV_XMLSH_HOME , 
	     ShellConstants.ENV_XMLSH, 
	     ShellConstants.ENV_XPATH
         
	};
	@Override
	public String toString() {
		return "[" + _id + "," +  mLastThreadId +  "]" ;
	}
	

	public boolean isInFunction()
	{
	    return mCallStack != null && !mCallStack.isEmpty();
	}
	

	// The return of a function and/or command
	// A interger 'exit value' and a XValue - may or may not be the same
	public static class ReturnValue {
		public ReturnValue(int exitStatus, XValue returnValue) {
			super();
			mExitStatus = exitStatus;
			mReturnValue = returnValue;
		}

		public int mExitStatus;
		public XValue mReturnValue;
	}

	class CallStackEntry {
		public String name;
		public IExpression cmd;
		public SourceLocation loc;

		public CallStackEntry(String name, IExpression cmd, SourceLocation loc) {
			mLogger.entry(name, cmd, loc);
			this.name = name;
			this.cmd = cmd;
			this.loc = loc;
		}

		public SourceLocation makeLocation() {
			SourceLocation s = new SourceLocation(name,
					(loc == null ? getLocation() : loc));
			return s;
		}
	}

	static Logger mLogger = LogManager.getLogger();
	private ShellOpts mOpts;
	private ShellIO   mIO; // Command and event IO environment


	private XEnvironment mEnv = null;
	private List<XValue> mArgs = new CopyOnWriteArrayList<XValue>();
	private String mArg0 = "xmlsh";
	private SessionEnvironment mSession = null;

	// Set to non null until exit or EOF
	private Integer mExitVal = null;
	private XValue mReturnVal = null;

	private int mStatus = 0; // $? variable

	private String mSavedCD = null;

	private volatile List<ShellThread> mChildren = null;
	private Map<String, String> mTraps = null;
	private boolean mIsInteractive = false;
	private long mLastThreadId = 0;

	private Stack<ControlLoop> mControlStack = null;
	private Stack<CallStackEntry> mCallStack = null;

	// Depth of conditions used for 'throw on error'
	private int mConditionDepth = 0;

	// Current classloader
	private SourceLocation mCurrentLocation = null;

	/*
	 * Initializtion statics
	 */
	static boolean bInitialized = false;
	static Properties mSavedSystemProperties;
	private static Processor mProcessor = null;

	private Shell mParent = null;
	private IFS mIFS;
	private ThreadGroup mThreadGroup = null;
	private volatile boolean mClosed = true;
	private final static EvalEnv mPSEnv = EvalEnv.newInstance(false, false,
			true, false);

	private AtomicInteger mRunDepth = new AtomicInteger(); // count of depth if
	// the shell is
	// executing
	private volatile List<Process> mChildProcess = null;

	// Special flag/marker indicating a return
	private final XValue mReturnFlag = XValue.nullValue();

	public static void uninitialize() {
		if (!bInitialized)
			return;

		mProcessor = null;
		System.setProperties(mSavedSystemProperties);
		mSavedSystemProperties = null;
		SystemEnvironment.uninitialize();
		ThreadLocalShell.set(null);
		bInitialized = false;
	}

	static {
		ShellConstants.initialize();
	}

	/*
	 * New top level shell
	 */
	public Shell() throws Exception { 
	    this( new ShellIO(true) );
	    
	}
	
	
	/*
	 * Create a new Shell.
	 * 
	 */

	public Shell(ShellIO io) throws Exception {
		mLogger.entry(io);
		mIO = io ;
		mIO.setPrompt(this);
		mClosed = false ;
		mOpts = new ShellOpts();
		mSavedCD = System.getProperty(ShellConstants.PROP_USER_DIR);
		mEnv = new XEnvironment(this,  new StaticContext() , 
		        RootModule.getInstance() , io );
		mSession = new SessionEnvironment();


		setGlobalVars();

		ThreadLocalShell.set(this); // cur thread active shell
		importStandardModules();
	
	}

	/*
	 * Populate the environment with any global variables
	 */

	private void importStandardModules() throws Exception {
		
		IModule hInternal = 
				 ModuleFactory.createPackageModule(this, ModuleFactory.getInternalModuleConfig(this ,
		 					"xmlsh" , Arrays.asList(
											"org.xmlsh.internal.commands", "org.xmlsh.internal.functions"),
		 							CommandFactory.kCOMMANDS_HELP_XML ) 
		 							
						 );
		
		
		 getModules().importModule( this , null , hInternal , null);
		 
		 
	}

	private void setGlobalVars() throws InvalidArgumentException {

	    
	    // System env first 
		Map<String, String> env = System.getenv();
		for (Map.Entry<String, String> entry : env.entrySet()) {

			String name = entry.getKey();
			if (Util.isPath(name))
				continue;
			if (Util.isBlank(name))
				continue;
			if (!name.matches("^[a-zA-Z_0-9]+$"))
				continue;

			// Ignore reserved vars that are set internally
			if (Util.contains(_reservedEnvVars , name ))
				continue;

			getEnv().initVariable(
					XVariable.newInstance(name,
							XValue.newXValue(entry.getValue()),
							XVariable.systemFlags()));

		}
		
		
		

		// Builtins may come from env or properties 
		// Export path to shell path
		String path = FileUtils.toJavaPath(  getSysProp(  ShellConstants.PATH ) );
		getEnv().initVariable(
				XVariable.newInstance(
						ShellConstants.PATH,
						Util.isBlank(path) ? XValue.empytSequence() : XValue
								.newXValue(path.split(File.pathSeparator)),
								XVariable.systemFlags()));

		String xpath = FileUtils
				.toJavaPath( getSysProp(ShellConstants.ENV_XPATH));
		getEnv().initVariable(
				XVariable.newInstance(
						ShellConstants.ENV_XPATH,
						Util.isBlank(xpath) ? XValue.newXValue(".") : XValue
								.newXValue(xpath.split(File.pathSeparator)),
								XVariable.systemFlags()));

		
		
		
		String xmlsh  = getSysProp( ShellConstants.ENV_XMLSH );
		String xmlshhome = getSysProp( ShellConstants.ENV_XMLSH_HOME  );
		if( Util.isBlank(xmlshhome))
		    xmlshhome = xmlsh ;
		
		if( Util.isBlank( xmlshhome))
		    xmlshhome = tryFindHome();
		
		if( Util.isBlank(xmlsh))
		    xmlsh = xmlshhome ;
		
		if( Util.isBlank(xmlshhome)) { 
		    mLogger.warn("Required  property {} missing - limited functionalty." , ShellConstants.ENV_XMLSH_HOME);
		}
		getEnv().setVar(
                ShellConstants.ENV_XMLSH, XValue.newXValue(FileUtils.toJavaPath(xmlsh)) , XVariable.standardFlags() );
		
        getEnv().setVar(
                ShellConstants.ENV_XMLSH_HOME, XValue.newXValue(FileUtils.toJavaPath(xmlshhome)) , XVariable.standardFlags() );
		

		String xmpath = FileUtils.toJavaPath(getSysProp(ShellConstants.ENV_XMODPATH));
		
		getEnv().setVar(
						ShellConstants.ENV_XMODPATH,
						Util.isBlank(xmpath)  ? XValue.empytSequence() : 
						XValue.newXValue( xmpath.split(File.pathSeparator)  ) ,
		      XVariable.standardFlags()
		        );

		// PWD
		getEnv().initVariable(
				new XDynamicVariable(ShellConstants.PWD, EnumSet.of(
						XVarFlag.READONLY, XVarFlag.EXPORT)) {
					@Override
					public XValue getValue() {
						return XValue.newXValue(FileUtils.toJavaPath(getEnv()
								.getCurdir().getAbsolutePath()));
					}

				});

		// RANDOM
		getEnv().initVariable(
				new XDynamicVariable(ShellConstants.VAR_RANDOM, EnumSet
						.of(XVarFlag.READONLY)) {
					Random mRand = new Random();

					@Override
					public XValue getValue() {
						return XValue.newXValue(mRand.nextInt(0x7FFF));
					}

				});

		// RANDOM32
		getEnv().initVariable(
				new XDynamicVariable(ShellConstants.VAR_RANDOM32, EnumSet
						.of(XVarFlag.READONLY)) {
					Random mRand = new Random();

					@Override
					public XValue getValue() {
						long v = mRand.nextInt();
						v &= 0x7FFFFFFFL;
						return XValue.newXValue((int) v);
					}

				});

		// RANDOM
		getEnv().initVariable(
				new XDynamicVariable(ShellConstants.VAR_RANDOM64, EnumSet
						.of(XVarFlag.READONLY)) {
					Random mRand = new Random();

					@Override
					public XValue getValue() {
						return XValue.newXValue(mRand.nextLong() & 0x7FFFFFFFFFFFFFFFL);
					}
				});

		getEnv().setVar(
				ShellConstants.ENV_TMPDIR,
				XValue.newXValue(FileUtils.toJavaPath(System
						.getProperty(ShellConstants.PROP_JAVA_IO_TMPDIR))),
						XVariable.systemFlags());

		if (getEnv().getVar(ShellConstants.ENV_HOME) == null)
			getEnv().setVar(
					ShellConstants.ENV_HOME,
					XValue.newXValue(FileUtils.toJavaPath(System
							.getProperty(ShellConstants.PROP_USER_HOME))),
							XVariable.systemFlags());

	}

	// Try to find the install root
	// http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
	private static String tryFindHome() {
	    mLogger.entry();
	    try {
          CodeSource cs = Shell.class.getProtectionDomain().getCodeSource();
          if( cs != null ){
               Path p = Paths.get( cs.getLocation().toURI());
               String sdir  = null ;
               while( (p = FileUtils.resolveLink( p  ) ) != null ){
                   if( ! Files.isDirectory( p ) )
                       continue ;
                   if( sdir == null)
                      sdir = p.toString(); // first dir found
                   
                   Path parent =  FileUtils.getParent( p );
                   if( parent == null || parent == p || Files.isSameFile(parent, p ))
                       break ;
                   p=parent;
                   if( p != null ){
                       if(   Files.isDirectory( p.resolve( "modules") ) ||
                              Files.isDirectory( p.resolve( "lib") ) ) 
                              return mLogger.exit(p.toString());
                   }
               }
               return mLogger.exit(sdir );
          }
          
	    } catch( SecurityException | URISyntaxException  | IOException e ){
	        mLogger.catching(e);
	    }

	    return null;
    }


    private String getSysProp(String name) {
	    return System.getProperty("xmlsh.env." + name , System.getenv(name));
    }


    /*
	 * Cloned shell for sub-thread execution
	 */
	private Shell(Shell that) throws IOException {
		this(that, that.getThreadGroup());
	}

	private Shell(Shell that, ThreadGroup threadGroup) throws IOException {
		
		mLogger.entry(that, threadGroup);
		mIO = that.mIO ;
		mClosed = false ;
		mParent = that;
		mThreadGroup = threadGroup == null ? that.getThreadGroup()
				: threadGroup;
		mOpts = new ShellOpts(that.mOpts);
		mEnv = that.getEnv().clone(	this );
		mArg0 = that.mArg0;

		// clone $1..$N
		mArgs = new CopyOnWriteArrayList<XValue>(that.mArgs);

		mSavedCD = System.getProperty(ShellConstants.PROP_USER_DIR);

		// mModule.addRef? mModule.clone() ?

		// Pass through the Session Enviornment, keep a reference
		mSession = that.mSession;
		mSession.addRef();

		// Cloning shells doesnt save the condition depth
		// mConditionDepth = that.mConditionDepth;
		mLogger.exit();

	}

	@Override
	public Shell clone() {
		
		mLogger.entry();
		try {
			return new Shell(this);
		} catch (IOException e) {

			printErr("Exception cloning shell", e);
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		mLogger.entry();
		// Synchronized only to change states
		synchronized (this) {
			
			
			mLogger.trace("closing {} - closed is: {} ", this , mClosed);
			if (mClosed){
				 mLogger.exit();
			   return;
			}
			// Mark closed now while syncronized
			mClosed = true ;
		}

		if (mParent != null)
			mParent.notifyChildClose(this);
		mParent = null;
		mThreadGroup = null;
		if (mEnv != null) {
			mEnv.clear();
			mEnv = null;
		}
		if (mSavedCD != null)
			SystemEnvironment.getInstance().setProperty(
					ShellConstants.PROP_USER_DIR, mSavedCD);
		if (mSession != null) {
			Util.safeRelease(mSession);
			mSession = null;
		} 
		
		
		mLogger.exit();
	}

	private void notifyChildClose(Shell shell) {
		// Async code might invalidate job
		// but removeJob can handle invalid pointers
		ShellThread job = findJobByShell(shell);
		if (job != null)
			removeJob(job);

	}

	private ShellThread findJobByShell(Shell shell) {

		List<ShellThread> children = getChildren(false);

		if (children != null)
			synchronized (children) {
				for (ShellThread t : children) {
					if (t.getShell() == shell)
						return t;
				}
			}
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		
		mLogger.entry();
		close();
	}

	public XEnvironment getEnv() {
		return mEnv;
	}

	public SessionEnvironment getSession() {
		return mSession;
	}

	public IExpression parseScript(Reader reader, String source)
			throws CoreException {
		
		mLogger.entry(reader, source);

		try {

			enterEval();
			ShellParser parser = new ShellParser( this, 
					newParserReader(reader, true), source);
			return parser.script();

		} catch (Exception e) {
			throw new CoreException("Exception parsing source: " + source, e);
		}

		finally {
			exitEval();
		}
		

	}

	public ICommandExpr parseEval(String scmd) throws CoreException {

		mLogger.entry(scmd);

		try ( Reader reader = Util.toReader(scmd ) ){
			enterEval();

			ShellParser parser = new ShellParser(this,newParserReader(reader,false),"<eval>");

			ICommandExpr c = parser.script();
			return mLogger.exit( c);

		} catch (Exception e) {
			
           throw new CoreException("Exception parsing command: " + scmd, e);
		}

		finally {
			exitEval();
		}
	}

	private void exitEval() {
		
		mLogger.entry();
		int d;
		if ((d = mRunDepth.decrementAndGet()) < 0) {
			mLogger.error("SNH: run depth underrun: " + d
					+ " resetting to 0: stack {}", (Object[]) Thread
					.currentThread().getStackTrace());
			mRunDepth.compareAndSet(d, 0);
		}
	}

	private void enterEval() {
		mRunDepth.incrementAndGet();
	}

	public boolean isExecuting() {
		return mRunDepth.get() > 0;
	}

	public boolean validateScript(Reader reader, String source)
			throws ParseException, IOException {
		SourceLocation saveLoc = getLocation();
		ShellParser parser = null;
		boolean bSuccess = false;
		try {
			enterEval();
			parser = new ShellParser(this,newParserReader(reader, true), source);

			while (parser.command_line() != null)
				;
			bSuccess = true;

		} finally {
			exitEval();
			setCurrentLocation(saveLoc);

		}
		return bSuccess;

	}

	/*
	 * 
	 * public int runScript( ) {
	 * 
	 * ICommandExpr parsed = parseScript(reader, source)
	 * 
	 * 
	 * }
	 */
	public ReturnValue runScript(URL scriptURL, String source,
			boolean convertReturn) throws ParseException, IOException, ThrowException
	{
		
		mLogger.entry(scriptURL, source, convertReturn);
		try ( Reader reader = Util.toReader(scriptURL, getInputTextEncoding())) {
			return runScript( reader , source , convertReturn );
		}
			
	}
			

	public ReturnValue runScript(Reader reader, String source,
			boolean convertReturn) throws ParseException, ThrowException,
			IOException {

		mLogger.entry(reader, source, convertReturn);
		try {
			enterEval();
			SourceLocation saveLoc = getLocation();
			int exitStatus = 0;
			ShellParser parser = null;
			try {
				parser = new ShellParser(this,newParserReader(reader,true), source);
				while (!hasReturned()) {
					CommandExpr c = parser.command_line();
					if (c == null)
						break;

					setSourceLocation(c.getSourceLocation());
					if (mOpts.mVerbose || mOpts.mLocation) {

						if (mOpts.mLocation) {
							mLogger.info(formatLocation());
						}

						if (mOpts.mVerbose) {
							String s = c.describe(false);

							if (s.length() > 0) {
								printErr(s);
							}
						}
					}

					exitStatus = exec(c);
				}
			}

			catch (ThrowException e) {
				// mLogger.info("Rethrowing throw exception",e);
				throw e ;

			} catch (ExitOnErrorException e) {
				exitStatus = e.getValue();
			}

			
			catch (Exception | Error e) {
				printErr(e.getMessage());
				mLogger.warn("Exception parsing statement", e);
				parser.ReInit(newParserReader(reader,false), source);

			} finally {
				setCurrentLocation(saveLoc);

			}

			// Exited evaluation - but still in entry point

			if (mExitVal != null)
				exitStatus = mExitVal.intValue();
			// Special check for returning thrown errors
			else if (convertReturn) // clears mReturnValue
				exitStatus = getReturnValueAsExitStatus(exitStatus);

			onSignal("EXIT");
			return mLogger.exit(new ReturnValue(exitStatus, getReturnValue())) ;

		} finally {
			exitEval();
		}
	}

	private boolean hasReturned() {
		return !(mExitVal == null && mReturnVal == null);
	}

	private ShellParserReader newInteractiveParserReader()
			throws IOException, CoreException {

	        return newParserReader(  mIO.getReader(), false );
	    
	}

	private ShellParserReader newParserReader(Reader reader,boolean bSkipBOM)
			throws IOException {
		return new ShellParserReader(reader, bSkipBOM );
	}


	public String getInputTextEncoding() {
		return getSerializeOpts().getInputTextEncoding();
	}

	public int interactive() throws Exception
	{
		mIsInteractive = true;
		int ret = 0;

		try {
			enterEval();

			// ShellParser parser= new
			// ShellParser(mCommandInput,Shell.getEncoding());
			ShellParser parser = new ShellParser(this,newInteractiveParserReader(),
					"stdin");

			while (mExitVal == null) {

				CommandExpr c = null;
				try {
				    parser.reset();
					c = parser.command_line();

					if (c == null)
						break;
					setSourceLocation(c.getSourceLocation());

					if (mOpts.mVerbose) {
						String s = c.describe(false);
						if (s.length() > 0) {
							SourceLocator loc = getLocation();
							printErr("- " + s);
						}
					}

					ret = exec(c);

					// PrintWriter out = new PrintWriter( System.out );
					// s.print(out);
					// out.flush();

				} catch (ThrowException e) {
					printErr("Ignoring thrown value: " + e.getMessage());
					mLogger.error("Ignoring throw value", e);
					parser.ReInit(newInteractiveParserReader(), null);
				} catch (Exception e) {

					SourceLocation loc = c != null ? c.getSourceLocation() : null;

					if (loc != null) {
						String sLoc = loc.format(mOpts.mLocationFormat);
						mLogger.info(loc.format(mOpts.mLocationFormat));
						printErr(sLoc);
					}

					printErr(e.getMessage());
					mLogger.warn("Exception parsing statement", e);
					parser.ReInit(newInteractiveParserReader(), null);
				} catch (Error e) {
					printErr("Error: " + e.getMessage());
					SourceLocation loc = c != null ? c.getSourceLocation() : null;
					mLogger.warn("Exception parsing statement", e);
					if (loc != null) {
						String sLoc = loc.format(mOpts.mLocationFormat);

						mLogger.info(loc.format(mOpts.mLocationFormat));
						printErr(sLoc);
					}
					parser.ReInit(newInteractiveParserReader(), null);
				}
			}
			if (mExitVal != null)
				ret = mExitVal.intValue();
			onSignal("EXIT");
		} finally {
			exitEval();
		}

		return ret;
	}

	public void setSourceLocation(SourceLocation loc) {
		setCurrentLocation(loc);
	}

	public void runRC(String rcfile) throws IOException, Exception {
		
		mLogger.entry(rcfile);
		// Try to source the rcfile
		if (rcfile != null) {
			try {
				enterEval();
				File script = this.getFile(rcfile);
				if (script.exists() && script.canRead()) {
					ICommand icmd = CommandFactory.getScript(
							this, script, rcfile ,  SourceMode.SOURCE, null);
					if (icmd != null) {
						// push location
						SourceLocation l = getLocation();
						setCurrentLocation(icmd.getLocation());
						icmd.run(this, rcfile, null);
						setCurrentLocation(l);

					}
				}
				onSignal("EXIT");
			} finally {
				exitEval();
			}
		}
		
		mLogger.exit();
	}

	public String getPS(String ps, String def) {

		XValue ps1 = getEnv().getVarValue(ps);
		if (ps1 == null)
			return def;
		String sps1 = ps1.toString();
		if (!Util.isBlank(sps1))
			try {
				sps1 = EvalUtils.expandStringToString(this, sps1, mPSEnv);
			} catch (IOException | CoreException e) {
				mLogger.debug("Exception getting PS var " + ps, e);
				return def;
			}
		return sps1;

	}


	/*
	 * Main entry point for executing commands. All command execution should go
	 * through this entry point
	 * 
	 * Handles background shell ("&") Handles "throw on error" (-e)
	 */
	public int exec(ICommandExpr c) throws ThrowException, ExitOnErrorException {
		return exec(c, getLocation(c));
	}

	// Default location for command
	private SourceLocation getLocation(IExpression c) {

		return c.hasLocation() ? c.getSourceLocation() : getLocation();
	}

	public int exec(ICommandExpr c, SourceLocation loc) throws ThrowException,
	ExitOnErrorException {
		
		mLogger.entry(c, loc);

		try {
			enterEval();
			if (loc == null)
				loc = c.getSourceLocation();

			setCurrentLocation(loc);

			if (mOpts.mExec || mOpts.mTrace) {
				logExec(c, mOpts.mExec, loc);
			}

			try {

				if (c.isWait()) {
					// Execute forground command
					mStatus = c.exec(this);

					// If not success then may throw if option 'throw on error'
					// is set (-e)
					if (mStatus != 0 && mOpts.mThrowOnError && c.isSimple()) {
						if (!isInCommandConndition())
							throw new ExitOnErrorException(mStatus);
					}
					return mStatus;
				}

				ShellThread sht = new ShellThread(newThreadGroup(c.getName()),
						new Shell(this), null, c);

				if (isInteractive())
					printErr("" + sht.getId());

				addJob(sht);
				sht.start();

				return mStatus = 0;
			} catch (ThrowException | ExitOnErrorException e ) {
				throw e;
			}

			catch (Exception e) {
				
				printLoc(mLogger, loc);

				printErr("Exception running: " + c.describe(true));
				printErr(e.toString(), loc);

				mLogger.error(
						"Exception running command: " + c.describe(false), e);
				mStatus = -1;
				// If not success then may throw if option 'throw on error' is
				// set (-e)
				if (mStatus != 0 && mOpts.mThrowOnError && c.isSimple()) {
					if (!isInCommandConndition())
						throw new ThrowException(XValue.newXValue(mStatus));
				}
				return mLogger.exit(mStatus);

			}
		} finally {
			exitEval();
		}
		
	}

	public void logExec(ICommandExpr c, boolean bExec, SourceLocation loc) {

		StringBuilderWriter sw = new StringBuilderWriter();
		PrintWriter w = new PrintWriter(sw);

		c.print(w, bExec);
		w.flush();

		StringBuilder sb = sw.getBuilder();

		if (sb.length() > 0) {
			String scmd = sb.toString();
			if (mOpts.mTrace && mLogger.isEnabled(mOpts.mTraceLevel)) {
				traceExec(loc, scmd);
			}
			if (mOpts.mExec) {
				if (loc != null) {
					printErr("+ " + loc.format(mOpts.mLocationFormat));
					printErr(scmd);
				} else
					printErr("+ " + scmd);
			}
		}
	}

	/*
	 * Returns TRUE if the shell is currently in a condition
	 */

	private void traceExec(SourceLocation loc, String scmd) {
		String strace = SourceLocation.formatLocation(loc,
				mOpts.mLocationFormat) + " " + scmd;

		if (mOpts.mTraceFile != null) {
			String sEnc = mOpts.mSerialize.getOutputTextEncoding();
			try {
				OutputPort op = getEnv().getOutput(mOpts.mTraceFile, true);
				try (OutputStream os = op.asOutputStream(getSerializeOpts())) {
					os.write(strace.getBytes(sEnc));
					os.write(Util.getNewlineBytes(getSerializeOpts()));
					os.flush();
				}
			} catch (IOException | CoreException e) {
				mLogger.debug("Exception tracing output", e);
			}

		} else
			mLogger.log(mOpts.mTraceLevel, strace);

	}

	public ThreadGroup newThreadGroup(String name) {
		return new ThreadGroup(getThreadGroup(), name);
	}

	public boolean isInCommandConndition() {
		return mConditionDepth > 0;
	}

	// Enter a condition
	private void pushCondition() {
		mConditionDepth++;
	}

	private void popCondition() {
		mConditionDepth--;
	}

	public boolean isInteractive() {
		return mIsInteractive;
	}

	private void addJob(ShellThread sht) {

		List<ShellThread> children = getChildren(true);
		synchronized (children) {
			children.add(sht);
		}

		mLastThreadId = sht.getId();
	}

	public void printErr(String s) {
		printErr(s, getLocation());
	}

	public void printErr(String s, SourceLocation loc) {
	    mLogger.entry(s,loc);
	    mLogger.warn("printOut: {}" , s );

		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(getEnv().getStderr().asOutputStream(
						getSerializeOpts()), getSerializeOpts()
						.getOutputTextEncoding())))) {
			if (mOpts.mLocation) {
				out.println(formatLocation(loc));
			}
			out.println(s);
			out.flush();
		} catch (UnsupportedEncodingException | CoreException e) {
			mLogger.error("Exception writing output: " + s, e);

		} finally {
		    mLogger.exit();
		}

	}

	private String formatLocation() {
		return formatLocation(null);
	}

	private String formatLocation(SourceLocation loc) {
		if (loc == null)
			loc = getLocation();
		return loc == null ? "<?>" : loc.format(mOpts.mLocationFormat);
	}

	public void printOut(String s) {
	    mLogger.entry(s);
	    mLogger.debug("printOut: {}" , s );
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(getEnv().getStdout().asOutputStream(
						getSerializeOpts()), getSerializeOpts()
						.getOutputTextEncoding())))) {
			out.println(s);
			out.flush();
		} catch (IOException | CoreException e) {
			mLogger.error("Exception writing output: " + s, e);
			return;
		}
		finally {
		    mLogger.exit();
		}
	}
	
	public String readCommandLine(String prompt) throws IOException{
	    return mIO.readCommandLine(prompt);
	}


	public void printErr(String s, Exception e) {
	       
	    mLogger.entry( s,e);
	    mLogger.warn("printOut: {}" , s );

		try (PrintWriter out = getEnv().getStderr().asPrintWriter(
				getSerializeOpts())) {
			
			
			SourceLocation loc = getLocation();
			if (loc != null)
				out.println(formatLocation(loc));

			out.println(s);

			out.println(e.getMessage());
			for (Throwable t = e.getCause(); t != null; t = t.getCause()) {
				out.println("  Caused By: " + t.getMessage());
			}

			out.flush();
		} catch (IOException | CoreException e1) {
			mLogger.error("Exception writing output: " + s, e);
			return ;
		} finally {
		    mLogger.exit();
		}

	}

	public static void main(String argv[]) throws Exception {
		mLogger.entry();
		List<XValue> vargs = new ArrayList<XValue>(argv.length);
		for (String a : argv)
			vargs.add(XValue.newXValue(a));

		
		Shell shell = new Shell();
		org.xmlsh.builtin.commands.xmlsh cmd = new org.xmlsh.builtin.commands.xmlsh(
				true);

		int ret = -1;
		try {
			shell.enterEval();
			ret = cmd.run(shell, "xmlsh", vargs);
		} catch (ThrowException e){
			ret = shell.convertReturnValueToExitStatus( e.getValue());
			
		} catch (Throwable e) {
			mLogger.error("Uncaught exception in main", e);
		} finally {
			shell.exitEval();
			shell.close();
		}

		
		mLogger.exit(ret);
		System.exit(ret);

	}

	public void setArg0(String string) {
		mArg0 = string;

	}

	// Translate a shell return code to java bool
	public static boolean toBool(int intVal) {
		return intVal == 0;

	}

	// Translate a java bool to a shell return code
	public static int fromBool(boolean boolVal) {
		return boolVal ? 0 : 1;
	}

	public SearchPath getExternalPath() {
		return getPath(ShellConstants.PATH, true);
	}

	public SearchPath getPath(String var, boolean bSeqVar) {
		XValue pathVar = getEnv().getVarValue(var);
		if (pathVar == null || pathVar.isEmpty() )
			return new SearchPath();
		if (bSeqVar)
			return new SearchPath(pathVar);
		else
			return new SearchPath(pathVar.toString().split(File.pathSeparator));

	}

	/*
	 * Current Directory
	 */
	public static File getCurdir() {
		return new File(System.getProperty(ShellConstants.PROP_USER_DIR));
	}
	public static Path getCurPath() {
		return Paths.get(System.getProperty(ShellConstants.PROP_USER_DIR));
	}

	public static void  setCurdir(File cd) throws IOException {
		String dir = cd.getCanonicalPath();
		SystemEnvironment.getInstance().setProperty(
				ShellConstants.PROP_USER_DIR, dir);

	}

	public void setArgs(List<XValue> args) {
		mArgs = new CopyOnWriteArrayList<>(args);

	}

	public File getExplicitFile(String name, boolean mustExist)
			throws IOException {
		return getExplicitFile(null, name, mustExist);
	}

	public File getExplicitFile(String name, boolean mustExist, boolean isFile )
			throws IOException {
		File f = getExplicitFile(null, name, mustExist);
		if( f != null && (isFile? f.isFile() : f.isDirectory() ) )
             return f ;
		return null ;
	
	}

	public File getExplicitFile(String name, PathMatchOptions match )
			throws IOException {
		File f = getExplicitFile(null, name, true);
		Path path = FileUtils.asValidPath( f) ;
		if( path == null )
			return null ;
		if( f != null && match.doVisit(path))
             return f ;
		return null ;
	
	}
	
	public File getExplicitFile(File dir, String name, boolean mustExist)
			throws IOException {

		File file = null;
		file = new File(dir, name);

		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			mLogger.info("Exception translating file to canonical file", e);
			// try to still use file
		}
		if (mustExist && !file.exists())
			return null;

		return file;
	}

	public List<XValue> getArgs() {
		return mArgs;
	}

	public void exit(int retval) {
		mExitVal = Integer.valueOf(retval);
	}

	public void exec_return(XValue retval) {
		mReturnVal = retval == null ? mReturnFlag : retval;
	}

	/*
	 * Return TRUE if we should keep running on this shell Includes early
	 * termination in control stacks
	 */
	public boolean keepRunning() {
		// Hit exit stop
		if (mClosed || hasReturned())
			return false;

		// If the top control stack is break then stop
		if (hasControlStack()) {
			ControlLoop loop = getControlStack().peek();
			if (loop.mBreak || loop.mContinue)
				return false;
		}

		return true;

	}

	public String getArg0() {
		return mArg0;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return mStatus;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		mStatus = status;
  }

	public File getFile(File dir, String file) throws IOException {
		return getExplicitFile(dir, file, false);
	}

	public File getFile(String fname) throws IOException {
		return getExplicitFile(fname, false);
	}

	public File getFile(XValue fvalue) throws IOException {
		return getFile(fvalue.toString());
	}

	
	// unchecked exceptions
	public Path getPath( XValue fvalue ) throws InvalidPathException {
	    if( fvalue == null || fvalue.isNull() )
	        return null ; 
		return getPath( fvalue.toString());
	}
	
	public Path getPath(String fname) throws InvalidPathException  {

		Path dir = getCurPath();
		return dir.resolve(fname);
		
	}


	public void shift(int num) {
		num = Math.min(num, mArgs.size());
		if (num <= 0)
			return;
		mArgs = mArgs.subList(num, mArgs.size());

	}

	/*
	 * Returns the singleton processor for all of Xmlsh
	 */
	public static synchronized Processor getProcessor() {
		if (mProcessor == null) {
			String saxon_ee = System.getenv("XMLSH_SAXON_EE");
			boolean bEE = Util.isEmpty(saxon_ee) ? true : Util
					.parseBoolean(saxon_ee);
			mProcessor = new Processor(bEE);

			/*
			 * mProcessor.getUnderlyingConfiguration().getEditionCode();
			 * 
			 * System.err.println("Version " +
			 * mProcessor.getSaxonProductVersion() );
			 * System.err.println("XQuery " +
			 * mProcessor.getConfigurationProperty
			 * (FeatureKeys.XQUERY_SCHEMA_AWARE) ); System.err.println("XSLT " +
			 * mProcessor
			 * .getConfigurationProperty(FeatureKeys.XSLT_SCHEMA_AWARE) );
			 * System.err.println("Schema " +
			 * mProcessor.getConfigurationProperty(FeatureKeys.SCHEMA_VALIDATION
			 * ));
			 */

			// mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL,
			// net.sf.saxon.event.Builder.LINKED_TREE);
			mProcessor.registerExtensionFunction(new EvalDefinition());
			mProcessor.getUnderlyingConfiguration().setSerializerFactory(
					new XmlshSerializerFactory(mProcessor
							.getUnderlyingConfiguration()));
			mProcessor.getUnderlyingConfiguration().setErrorListener(
					new XmlshErrorListener());

		}

		return mProcessor;
	}

	public void removeJob(Thread job) {

		if (mChildren != null)
			synchronized (mChildren) {
				mChildren.remove(job);
				mChildren.notify(); // Must be in syncrhonized block
			}
	}

	// Kill a child shell whether or not its in our list of children
	public void killChild(ShellThread job, long waitTime) {
		if (job != Thread.currentThread()) {
			if (job.isAlive()) {
				try {
					job.shutdown(true, waitTime);
				} catch (Exception e) {

					mLogger.warn("Exception trying to close child shell: "
							+ job.describe());
				}
			} else
				job.interrupt();

			Thread.yield();
			if (waitTime >= 0) {
				try {
					job.join(waitTime);
				} catch (InterruptedException e) {
					mLogger.warn("Exception trying to wait for shell: "
							+ job.describe());
				}
			}
			Thread.yield();

			if (job.isAlive())
				mLogger.warn("Failed to kill child shell: " + job.describe());
		}
	}

	/*
	 * Returns the children of the current thread copied into a collection so
	 * that it is thread safe
	 */

	public List<ShellThread> getChildren(boolean create) {
		// mChildren is only set never chaqnged or cleared

		// Memory Barrier
		if (mChildren == null && create) {
			synchronized (this) {
				if (mChildren == null)
					mChildren = Collections
					.synchronizedList(new ArrayList<ShellThread>());
			}
		}

		return mChildren;
	}

	/*
	 * Waits until there are "at most n" running children of this shell
	 */
	public boolean waitAtMostChildren(int n, long waitTime) {
		long end = System.currentTimeMillis() + waitTime;

		waitTime = Util.nextWait(end, waitTime);
		while (childrenSize() > n) {
			if (waitTime < 0)
				return false;
			try {
				enterEval(); // equivilent to an eval - were blocking
				if (mChildren != null)
					synchronized (mChildren) {
						mChildren.wait(waitTime);
					}

			} catch (InterruptedException e) {
				mLogger.warn("interrupted while waiting for job to complete", e);
			} finally {
				exitEval();
				waitTime = Util.nextWait(end, waitTime);

			}
		}
		return true;

	}

	private int childrenSize() {
		// memory barrier
		if (mChildren == null)
			return 0;
		synchronized (mChildren) {
			return mChildren.size();
		}
	}

	public long getLastThreadId() {
		// TODO Auto-generated method stub
		return mLastThreadId;
	}

	/*
	 * Break n levels of control stacks -1 means break all
	 */
	public int doBreak(int levels) {
		if (!hasControlStack())
			return 0;

		int end = getControlStack().size() - 1;
		if (levels < 0) {
			while (end >= 0)
				getControlStack().get(end--).mBreak = true;
		} else {

			while (levels-- > 0 && end >= 0)
				getControlStack().get(end--).mBreak = true;

		}
		return 0;

	}

	/*
	 * Continue n levels of control stacks
	 */

	public int doContinue(int levels) {
		if (!hasControlStack())
			return 0;

		int end = getControlStack().size() - 1;

		/*
		 * Break n-1 levels
		 */
		while (levels-- > 1 && end >= 0)
			getControlStack().get(end--).mBreak = true;

		// Continue the final level
		if (end >= 0)
			getControlStack().get(end).mContinue = true;

		return 0;
	}

	public ControlLoop pushLoop(SourceLocator sourceLocator) {
		ControlLoop loop = new ControlLoop(sourceLocator);
		getControlStack().add(loop);
		return loop;
	}

	/*
	 * Pop the control stack until we hit loop, if loop isnt found (SNH) pop
	 * until empty
	 */
	public void popLoop(ControlLoop loop) {

		while (hasControlStack())
			if (mControlStack.pop() == loop)
				break;
	}

	public void declareFunction(IFunctionDefiniton func) {

		mLogger.entry(func);
		StaticContext ctx = getStaticContext();
		assert( ctx != null );
		ctx.declareFunction(func);
	    mLogger.exit();

	}

	public IFunctionDefiniton getFunctionDecl(String name) {
		mLogger.entry(name);
		
		// First look in the current module
	//	IFunctionDecl funcd = mModule.get().getFunction(name);
		
		
		StaticContext ctx = getStaticContext();
		assert( ctx != null );
		return mLogger.exit( ctx.getFunction(name));
	}

	public Modules getModules() {
		mLogger.entry();
		return mLogger.exit( getEnv().getModules() );
		
	}

	/*
	 * Execute a function as a command Extracts return values from the function
	 * if present
	 */
	public int execFunctionAsCommand(String name, ICommandExpr cmd,
			SourceLocation location, List<XValue> args) throws Exception {

		List<XValue> saveArgs = getArgs();
		String saveArg0 = getArg0();
		Variables save_vars = pushLocalVars();

		getCallStack().push(new CallStackEntry(name, cmd, location));
		setArg0(name);
		setArgs(args);

		try {
			int ret = exec(cmd, location);
			return ret;

		} finally {
			popLocalVars(save_vars);
			setArg0(saveArg0);
			setArgs(saveArgs);
			getCallStack().pop();

		}
	}

	/*
	 * Run a function expressed as a command body
	 */
	public XValue runCommandFunction(String name, ICommandExpr mBody,
			List<XValue> args) throws ThrowException,
			ExitOnErrorException {

		List<XValue> saveArgs = getArgs();
		String saveArg0 = getArg0();
		Variables save_vars = pushLocalVars();

		getCallStack().push(new CallStackEntry(name, mBody, getLocation() ));
		setArg0(name);
		setArgs(args);

		try {
			int ret = exec(mBody, mBody.getSourceLocation());
			return getReturnValue();

		} finally {
			popLocalVars(save_vars);
			setArg0(saveArg0);
			setArgs(saveArgs);
			getCallStack().pop();

		}

	}

	/*
	 * Convert return value to exit value
	 */
	private int convertReturnValueToExitStatus(XValue value) {
		// Special case for null values
		if (value == null || value == mReturnFlag)
			return 0;

		// Null is false (1)
		if (value.isNull())
			return 1;

		// Change: Empty sequence is false
		if (value.isEmpty() || value.isEmptySequence())
			return 1;

		/*
		 * Special: if looks like an integer then thats the exit value
		 */
		if (value.isAtomic()) {
			// Check if native boolean

			String s = value.toString();
			// Empty string is false
			if (Util.isBlank(s))
				return 0;

			if (Util.isInt(s, true))
				return Integer.parseInt(s);
			else if (s.equalsIgnoreCase("true"))
				return 0;
			else
				return 1; // False

		}

		try {
			return value.toBoolean() ? 0 : 1;
		} catch (Exception e) {
			mLogger.error("Exception parsing value as boolean", e);
			return -1;
		}

	}

	/*
	 * Declare a module using the prefix=value notation
	 */
	public boolean  importModule(String prefix, String name, List<URL> at, List<XValue> init)
			throws Exception {
		
		mLogger.entry(at, init);
		PName pname = new PName(name);
		
		ModuleConfig config = ModuleFactory.getModuleConfig( this , pname , at );

		if( config == null)
			return false ;
		
		IModule mod = getModules().getExistingModuleByName( config.getName());
		
		if( mod == null )
			mod =   ModuleFactory.createModule(this, config );
	 
		if( mod == null )
		    throw new UnexpectedException("Module could not be created: " + name );
		assert( mod != null );
		boolean inited = getModules().importModule(this, prefix , mod ,  init);

		if( inited )
		  mLogger.debug("Imported module {} fresh init: {}" , mod , inited );
		assert( mod != null );
		return mLogger.exit( true );

	}
	// Dup function but may need different
	public boolean importScript(String prefix, String name, List<URL> at, List<XValue> init) throws Exception
	{
		return importModule( prefix , name , at, init );
	}
	
	// returns true if succceeded
	public boolean importPackage(String prefix, String name,
			List<String> packages) throws Exception  {
		
		
		 mLogger.entry(prefix, name, packages);
	     String sHelp = packages.get(0).replace(ShellConstants.kDOT_CHAR, '/') + "/commands.xml";
		
	 		IModule mod = getModules().getExistingModuleByName(name);
	 		if( mod == null ){
	 			ModuleConfig config = ModuleFactory.getInternalModuleConfig( this ,
	 					name, packages, sHelp );
	 			
	 	    	if( config != null )
	 			  mod =  ModuleFactory.createPackageModule(this, config );
	 		}
	 		if( mod == null )
	 			throw new FileNotFoundException("Cannot locate package module:" + name );

	 		boolean inited = getModules().importModule( this , prefix , mod , null );
	 		return  true ;
	
	}

	public void importJava(List<URL> urls) throws CoreException {
		
		mLogger.entry(urls);
		
		getModule().addClassPaths( this , urls);
		return ;

	}

	public URL getURL(String file) throws CoreException {
		mLogger.entry(file);
		URL url = Util.tryURL(file);
		if (url == null)
			try {
				url = getFile(file).toURI().toURL();
			} catch (IOException e) {
				throw new CoreException(e);
			}
		return mLogger.exit(url);

	}

	public URI getURI(String file) throws CoreException {
		URI uri = Util.tryURI(file);
		if (uri == null)
			try {
				uri = getFile(file).toURI();
			} catch (IOException e) {
				throw new CoreException(e);
			}
		return uri;

	}
	
    public List<URL> toUrls( XValue args) throws CoreException {
    	if( args == null )
    		return null ;
    	List<URL> at  = new ArrayList<>();
	      for(XValue xv : args )
	    	  at.add( getURL(xv.toString()));
		return at;
    
    }
    public List<URL> toUrls(List<XValue> args) throws CoreException {
    	if( args == null || args.isEmpty())
    		return null ;
		List<URL> at = new ArrayList<>(args.size());
	      for(XValue xv : args )
	    	  for( XValue x : xv )
	    	     at.add( getURL(x.toString()));
		return at;
	}

	public InputPort newInputPort(String file) throws IOException {
		/*
		 * Special case to support /dev/null file on Windows systems Doesnt hurt
		 * on unix either to fake this out instead of using the OS
		 */
		if (FileUtils.isNullFilePath(file)) {
			return new StreamInputPort(new NullInputStream(), file);
		}

		URL url = Util.tryURL(file);

		if (url != null) {

			return new StreamInputPort(url.openStream(), url.toExternalForm());

		} else
			return new FileInputPort(getFile(file));
	}

	// Returns a new port need to manage the port properly
	public OutputPort newOutputPort(String file, boolean append)
			throws FileNotFoundException, IOException {
		if (FileUtils.isNullFilePath(file)) {
			return new StreamOutputPort(new NullOutputStream());
		}

		else {
			URL url = Util.tryURL(file);
			if (url != null)
				return new StreamOutputPort(url.openConnection()
						.getOutputStream());

		}
		return new FileOutputPort(getFile(file), append);
	}

	public OutputStream getOutputStream(String file, boolean append,
			SerializeOpts opts) throws FileNotFoundException, IOException,
			CoreException {
		OutputPort p = newOutputPort(file, append);
		addAutoRelease(p);
		return p.asOutputStream(opts);

	}

	public void addAutoRelease(OutputPort p) {
		getEnv().addAutoRelease(p);
	}

	public OutputStream getOutputStream(File file, boolean append)
			throws FileNotFoundException {

		return new FileOutputStream(file, append);
	}

	public void setOption(String name, boolean flag) {
		mOpts.setOption(name, flag);

	}

	public void setOption(String name, XValue value)
			throws InvalidArgumentException {
		mOpts.setOption(name, value);

	}

	public IModule getModule() {
		mLogger.entry();
		IModule mod = getEnv().getModule();
		return mLogger.exit(mod);
		
	}

	/**
	 * @return the opts
	 */
	public ShellOpts getOpts() {
		return mOpts;
	}

	/*
	 * Executes a command as a condition so that it doesnt throw an exception if
	 * errors
	 */
	public int execCondition(CommandExpr left) throws ThrowException,
	ExitOnErrorException {

		pushCondition();
		try {
			return exec(left);
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
		if (url != null)
			return url;

		for (IModule m : getModules()) {
			url = m.getResource(res);
			if (url != null)
				return url;
		}
		return null;

	}

	public void setOptions(Options opts) throws InvalidArgumentException {
		for (OptionValue ov : opts.getOpts()) {
			setOption(ov);
		}

	}

	private void setOption(OptionValue ov) throws InvalidArgumentException {
		mOpts.setOption(ov);

	}

	public SerializeOpts getSerializeOpts(Options opts)
			throws InvalidArgumentException {
		if (opts == null || opts.getOpts() == null)
			return mOpts.mSerialize;

		SerializeOpts sopts = mOpts.mSerialize.clone();
		sopts.setOptions(opts);
		return sopts;
	}

	public SerializeOpts getSerializeOpts() {
		return mOpts.mSerialize;

	}

	public Variables pushLocalVars() {
		return mEnv.pushLocalVars();

	}

	public void popLocalVars(Variables vars) {
		
		mLogger.entry(vars);
		mEnv.popLocalVars(vars);

	}

	public boolean requireVersion(String module, String sreq) {
		// Creates a 3-4 element array [ "1" , "0" , "1" , ? ]
		String aver[] = Version.getVersion().split("\\.");
		String areq[] = sreq.split("\\.");

		// Start with major and go down
		for (int i = 0; i < Math.max(aver.length, areq.length); i++) {
			if (i >= areq.length)
				break;
			int ireq = Util.parseInt(areq[i], 0);
			int iver = i >= aver.length ? 0 : Util.parseInt(aver[i], 0);

			// Same version OK check minor
			if (ireq == iver)
				continue;
			else if (ireq < iver)
				break;
			else if (ireq > iver) {
				return false;
			}

		}
		return true;
	}

	/*
	 * Get the return value of the last return statement
	 */
	private XValue getReturnValue() {
		XValue ret = mReturnVal;
		mReturnVal = null;
		return (ret == null || ret == mReturnFlag) ? XValue.empytSequence()
				: ret;
	}

	public XClassLoader getClassLoader() throws CoreException {
		
		return mLogger.exit(getModule().getClassLoader());
	}

	public XClassLoader getClassLoader(final List<URL> urls) throws CoreException {
			return getClassLoader( urls , null );
		}
	public XClassLoader getClassLoader(final List<URL> urls, XClassLoader parentClassLoader) throws CoreException {

		mLogger.entry(urls);
		// No class path sent, use this shells or this class
		if (Util.isEmpty(urls)) {
			
            XClassLoader loader = parentClassLoader == null ? getClassLoader() : parentClassLoader;
			return mLogger.exit( loader );
		}
 
		
		if( parentClassLoader == null )
			parentClassLoader = getClassLoader();
		if( parentClassLoader == null )
			parentClassLoader = XClassLoader.newInstance(getContextClassLoader());
		final ClassLoader parent = parentClassLoader;
		
		mLogger.trace("Parent loader for new class loader: {}", parent );

		
		return mLogger.exit( XClassLoader.newInstance( urls.toArray(new URL[urls.size() ]), parentClassLoader));
	}

	public void printLoc(Logger logger, SourceLocation loc) {

		if (loc != null) {
			String sLoc = loc.format(mOpts.mLocationFormat);
			logger.info(sLoc);

		}
	}

	public void trap(String signal, String cmd) {
		if (Util.isBlank(signal) || Util.isBlank(cmd))
			return;
		if (mTraps == null)
			mTraps = new HashMap<String, String>();

		if (signal.equals("0"))
			signal = "EXIT";
		mTraps.put(signal, cmd);

	}

	void onSignal(String signal) {

		// TODO: Should we avoid thiws on shutdown ?
		if (mTraps == null)
			return;
		String scmd = mTraps.get(signal);

		if (scmd == null)
			return;

		try {
			ICommandExpr c = parseEval(scmd);
			exec(c);
		} catch (Exception e) {
			this.printErr("Exception running trap: " + signal + ":" + scmd, e);
		}

	}

	public SourceLocation getLocation() {

		return mCurrentLocation == null ? new SourceLocation()
		: mCurrentLocation;
	}

	public SourceLocation getLocation(int depth) {
		if (depth < 0)
			return getLocation();
		if (!hasCallStack())
			return null;

		if (mCallStack.size() <= depth)
			return null;
		return mCallStack.get(depth).makeLocation();

	}

	public synchronized Shell getParent() {
		return mParent;
	}

	private boolean hasControlStack() {
		return mControlStack != null && !mControlStack.empty();
	}

	private Stack<ControlLoop> getControlStack() {
		if (mControlStack == null)
			mControlStack = new Stack<ControlLoop>();
		return mControlStack;
	}

	public boolean hasCallStack() {
		return mCallStack != null && !mCallStack.empty();
	}

	public Stack<CallStackEntry> getCallStack() {
		if (mCallStack == null)
			mCallStack = new Stack<CallStackEntry>();
		return mCallStack;
	}

	public int getReturnValueAsExitStatus(int defRet) {

		// Special null return value means 0 - SNH need to check logic of
		// scripts
		if (mReturnVal == null)
			return defRet;
		return convertReturnValueToExitStatus(getReturnValue());
	}

	public void setCurrentLocation(SourceLocation loc) {

		if (loc == null && mCurrentLocation != null)
			mLogger.debug("Overriting current location with null");
		else
			mCurrentLocation = loc;
	}

	public IFS getIFS() {
		String sisf = getEnv().getVarString("IFS");
		if (mIFS == null || !mIFS.isCurrent(sisf))
			;

		mIFS = new IFS(sisf);
		return mIFS;
	}

	public ThreadGroup getThreadGroup() {
		return mThreadGroup == null ? Thread.currentThread().getThreadGroup()
				: mThreadGroup;
	}

	public ShellThread getFirstThreadChild() {
		if (mChildren == null)
			return null;
		synchronized (mChildren) {
			return mChildren.isEmpty() ? null : mChildren.get(0);
		}

	}

	// Shutdown the shell
	// Intended to be called from a external thread to try to force the shell to
	// quit
	// Dont actually close it - causes too much asyncronous grief

	public boolean shutdown(boolean force, long waitTime) {
		
		mLogger.entry(force, waitTime);
		if (mClosed)
			return mLogger.exit(true);
		// Mark us done
		mExitVal = Integer.valueOf(0);

		long end = System.currentTimeMillis() + waitTime;
		waitTime = Util.nextWait(end, waitTime);

		// Break all
		doBreak(-1);

		List<ShellThread> children = getChildren(false);
		if (force) {
			if (children != null) {
				synchronized (children) {
					children = new ArrayList<>(children);
				}
				for (ShellThread c : children) {
					waitTime = Util.nextWait(end, waitTime);
					killChild(c, waitTime);
				}
			}
			terminateChildProcesses();
		}
		waitTime = Util.nextWait(end, waitTime);
		waitAtMostChildren(0, waitTime);
		Thread.yield();
		return mLogger.exit(mClosed);

	}

	public void addChildProcess(Process proc) {
		if (mChildProcess == null) {
			synchronized (this) {
				if (mChildProcess == null)
					mChildProcess = Collections
					.synchronizedList(new ArrayList<Process>());
			}
		}
		synchronized (mChildProcess) {
			mChildProcess.add(proc);
		}
	}

	public void removeChildProcess(Process proc) {
		if (mChildProcess == null)
			return;
		mChildProcess.remove(proc);
	}

	public void terminateChildProcesses() {
		if (mChildProcess == null)
			return;
		synchronized (mChildProcess) {
			for (Process p : mChildProcess)
				p.destroy();
			mChildProcess.clear();
		}
	}

	public boolean isClosed() {
		return mClosed;
	}

	public IModule getModuleByPrefix(String prefix) {
		
		return mLogger.exit(getEnv().getModuleByPrefix(prefix));
	}

	public FunctionDefinitions getFunctionDelcs() {
		return getEnv().getFunctions() ;
	}

	
	/*
	 * Associates the current shell with an associated module and any static context 
	 * it may have from when it was initialized.
	 */


	public StaticContext getStaticContext() {
		mLogger.entry();
		return mLogger.exit(getEnv().getStaticContext());
		
	}


	public Iterable<IModule> getDefaultModules() {
		mLogger.entry();
		return getStaticContext().getDefaultModules();
	
	}

	public StaticContext getExportedContext() {
		mLogger.entry();
		return mLogger.exit( mEnv.exportStaticContext( ) );
	}

	public void pushModule(IModule mod) {
	   
		ThreadContext.put("xmodule", mod.describe() );
	    mLogger.entry(mod);
		mLogger.exit(getEnv().pushModule(mod,mod.getStaticContext()));
	
	}

	public IModule  popModule() throws IOException {
		mLogger.entry();
		getEnv().popModule();
		IModule mod = getModule();
		ThreadContext.put("xmodule", mod.describe());
		return mLogger.exit(mod);
	}

	public static ClassLoader getContextClassLoader() {
		
		mLogger.entry();
		/*
		 * Gets the shell context class loadeder
		 */
		try {
		   ClassLoader loader = Thread.currentThread().getContextClassLoader();
		   if( loader == null )
			   loader = ClassLoader.getSystemClassLoader();
		    return mLogger.exit(loader);
		} catch( Throwable t ){
			mLogger.catching(t);
			return  mLogger.exit(ClassLoader.getSystemClassLoader());
		}
		
	}


    @Override
    public String getPrompt(int level) {
        switch( level ){
        case 0 :
            return getPS(ShellConstants.PS1,"$ " );
        case 1:
            return getPS(ShellConstants.PS2,"> " );
        default: 
            return null ;
        }

    }
    
    public String getShellHome() {
        XValue h = getEnv().getVarValue(ShellConstants.ENV_XMLSH_HOME);
        if( h == null || h.isEmpty() )
            return null ;
        return FileUtils.toJavaPath(getPath( h ));

        
    }
    
    public SearchPath getModulePath() {
        String home = getShellHome();
        SearchPath xm = null;
        if( home != null )
           xm = new SearchPath( Paths.get( home , "modules" ) );
        else
            xm = new SearchPath();
        xm.add( getPath(ShellConstants.ENV_XMODPATH, true)); 
        return xm;
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
