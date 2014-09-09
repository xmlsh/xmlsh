/**
 * $Id$
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.ExitOnErrorException;
import org.xmlsh.core.FileInputPort;
import org.xmlsh.core.FileOutputPort;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.Path;
import org.xmlsh.core.ScriptCommand.SourceMode;
import org.xmlsh.core.StreamInputPort;
import org.xmlsh.core.StreamOutputPort;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XDynamicVariable;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.CommandExpr;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.grammar.ShellParser;
import org.xmlsh.sh.grammar.ShellParserReader;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.NullOutputStream;
import org.xmlsh.util.SessionEnvironment;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

public class Shell implements AutoCloseable, Closeable
{

  class CallStackEntry
  {
    public String name;
    public ICommandExpr cmd;
    public SourceLocation loc;

    public CallStackEntry(String name, ICommandExpr cmd, SourceLocation loc)
    {

      this.name = name;
      this.cmd = cmd;
      this.loc = loc;
    }

    public SourceLocation makeLocation()
    {
      SourceLocation s = new SourceLocation(name, (loc == null ? getLocation() : loc));
      return s;
    }
  }

  static Logger mLogger = LogManager.getLogger();
  private ShellOpts mOpts;

  // Script functons
  private FunctionDefinitions mFunctions = null;
  private XEnvironment mEnv = null;
  private List<XValue> mArgs = new CopyOnWriteArrayList<XValue>();
  private InputStream mCommandInput = null;
  private String mArg0 = "xmlsh";
  private SessionEnvironment mSession = null;

  // Set to non null until exit or EOF
  private Integer mExitVal = null;
  private XValue mReturnVal = null;

  private int mStatus = 0;	// $? variable

  private String mSavedCD = null;

  private volatile List<ShellThread> mChildren = null;
  private Map<String, String> mTraps = null;
  private boolean mIsInteractive = false;
  private long mLastThreadId = 0;

  private Stack<ControlLoop> mControlStack = null;
  private Stack<CallStackEntry> mCallStack = null;

  // Depth of conditions used for 'throw on error'
  private int mConditionDepth = 0;

  private Modules mModules = null;

  // current module
  private IModule mModule = null;

  // Current classloader
  private ClassLoader mClassLoader = null;
  private SourceLocation mCurrentLocation = null;

  /*
   * Initializtion statics
   */
  static boolean bInitialized = false;
  static Properties mSavedSystemProperties;
  private static Processor mProcessor = null;
  // private static ModuleURIResolver mModuleURIResolver = null ;

  private Shell mParent = null;
  private IFS mIFS;
  private ThreadGroup mThreadGroup = null;
  private volatile boolean mClosed = false;
  private final static EvalEnv mPSEnv = EvalEnv.newInstance(false, false, true, false);

  private AtomicInteger mRunDepth = new AtomicInteger(); // count of depth if the shell is executing
  private volatile List<Process> mChildProcess = null;
  
  
  // Special flag/marker indicating a return
  private final   XValue mReturnFlag = XValue.nullValue();

  public static void uninitialize()
  {
    if(!bInitialized)
      return;

    mProcessor = null;
    System.setProperties(mSavedSystemProperties);
    mSavedSystemProperties = null;
    SystemEnvironment.uninitialize();
    ShellContext.set(null);
    bInitialized = false;
  }

  static {
    ShellConstants.initialize();
  }

  /*
   * New top level shell
   */
  public Shell() throws Exception
  {
    this(true);

  }

  public Shell(boolean bUseStdio) throws Exception
  {
    mOpts = new ShellOpts();
    mSavedCD = System.getProperty(ShellConstants.PROP_USER_DIR);
    mEnv = new XEnvironment(this, bUseStdio);
    mModules = new Modules(this);
    mSession = new SessionEnvironment();
    // Add xmlsh commands
    mModules.declarePackageModule( null, "xmlsh",
      Arrays.asList("org.xmlsh.internal.commands",
         "org.xmlsh.internal.functions" ) ,
      CommandFactory.kCOMMANDS_HELP_XML,null );

    setGlobalVars();

    mModule = null; // no current module
    ShellContext.set(this);	// cur thread active shell

  }

  /*
   * Populate the environment with any global variables
   */

  private void setGlobalVars() throws InvalidArgumentException
  {

    Map<String, String> env = System.getenv();

    for (Map.Entry<String, String> entry : env.entrySet()) {

      String name = entry.getKey();
      if(Util.isPath(name))
        continue;
      if(Util.isBlank(name))
        continue;
      if(!name.matches("^[a-zA-Z_0-9]+$"))
        continue;

      // Ignore PS1
      if(name.equals("PS1"))
        continue;

      getEnv().setVar(XVariable.newInstance(name, XValue.newXValue(entry.getValue()), XVariable.systemFlags() ));

    }

    // Export path to shell path
    String path = FileUtils.toJavaPath(System.getenv(ShellConstants.PATH));
    getEnv()
      .setVar(
        XVariable.newInstance(ShellConstants.PATH, Util.isBlank(path) ? XValue.empytSequence() : 
          XValue.newXValue(path.split(File.pathSeparator)),XVariable.systemFlags()));

    String xpath = FileUtils.toJavaPath(System.getenv(ShellConstants.XPATH));
    getEnv().setVar(
      XVariable.newInstance(ShellConstants.XPATH, Util.isBlank(xpath) ? XValue.newXValue(".") :
        XValue.newXValue(
           xpath.split(File.pathSeparator)),XVariable.systemFlags()));

    String xmpath = FileUtils.toJavaPath(System.getenv(ShellConstants.XMODPATH));
    getEnv().setVar(
      XVariable.newInstance(ShellConstants.XMODPATH, Util.isBlank(xmpath) ? 
          XValue.empytSequence(): XValue.newXValue(xmpath.split(File.pathSeparator)),XVariable.systemFlags()));

    // PWD
    getEnv().setVar(new XDynamicVariable(ShellConstants.PWD, EnumSet.of(XVarFlag.READONLY,XVarFlag.EXPORT))
      {
        @Override
        public XValue getValue()
        {
          return XValue.newXValue(FileUtils.toJavaPath(getEnv().getCurdir().getAbsolutePath()));
        }

      });

    // RANDOM
    getEnv().setVar(new XDynamicVariable(ShellConstants.VAR_RANDOM, EnumSet.of(XVarFlag.READONLY))
      {
        Random mRand = new Random();

        @Override
        public XValue getValue()
        {
          return XValue.newXValue(mRand.nextInt(0x7FFF));
        }

      });

    // RANDOM32
    getEnv().setVar(new XDynamicVariable(ShellConstants.VAR_RANDOM32, EnumSet.of(XVarFlag.READONLY))
      {
        Random mRand = new Random();

        @Override
        public XValue getValue()
        {
          long v = mRand.nextInt();
          v &= 0x7FFFFFFFL;
          return XValue.newXValue((int) v);
        }

      });

    // RANDOM
    getEnv().setVar(new XDynamicVariable(ShellConstants.VAR_RANDOM64, EnumSet.of(XVarFlag.READONLY))
      {
        Random mRand = new Random();

        @Override
        public XValue getValue()
        {
          return XValue.newXValue(mRand.nextLong() & 0x7FFFFFFFFFFFFFFFL);
        }
      });

    getEnv().setVar(ShellConstants.ENV_TMPDIR,
     XValue.newInstance( FileUtils.toJavaPath(System.getProperty(ShellConstants.PROP_JAVA_IO_TMPDIR))));

    if(getEnv().getVar(ShellConstants.ENV_HOME) == null)
      getEnv().setVar(ShellConstants.ENV_HOME, 
        XValue.newInstance(FileUtils.toJavaPath(System.getProperty(ShellConstants.PROP_USER_HOME))));

  }

  /*
   * Cloned shell for sub-thread execution
   */
  private Shell(Shell that) throws IOException
  {
    this(that, that.getThreadGroup());
  }

  private Shell(Shell that, ThreadGroup threadGroup) throws IOException
  {
    mParent = that;
    mThreadGroup = threadGroup == null ? that.getThreadGroup() : threadGroup;
    mOpts = new ShellOpts(that.mOpts);
    mEnv = that.getEnv().clone(this);
    mCommandInput = that.mCommandInput;
    mArg0 = that.mArg0;

    // clone $1..$N
    mArgs = new CopyOnWriteArrayList<XValue>(that.mArgs);

    mSavedCD = System.getProperty(ShellConstants.PROP_USER_DIR);

    if(that.mFunctions != null)
      mFunctions = new FunctionDefinitions(that.mFunctions);

    mModules = new Modules(this , that.mModules);

    mModule = that.mModule;

    // Pass through the Session Enviornment, keep a reference
    mSession = that.mSession;
    mSession.addRef();

    // Reference the parent classloader
    mClassLoader = that.mClassLoader;

    // Cloning shells doesnt save the condition depth
    // mConditionDepth = that.mConditionDepth;

  }

  @Override
  public Shell clone()
  {
    try {
      return new Shell(this);
    } catch (IOException e) {

      printErr("Exception cloning shell", e);
      return null;
    }
  }

  @Override
  public void close()
  {
    // Synchronized only to change states
    synchronized (this) {
      if(mClosed)
        return;
      mClosed = true;
    }
    
    if(mParent != null)
      mParent.notifyChildClose(this);
    mParent = null;
    mThreadGroup = null;
    if(mEnv != null) {
      Util.safeClose(mEnv);
      mEnv = null;
    }
    if(mSavedCD != null)
      SystemEnvironment.getInstance().setProperty(ShellConstants.PROP_USER_DIR, mSavedCD);
    if(mSession != null) {
      Util.safeRelease(mSession);
      mSession = null;
    }
    
    if( mModules != null )
      Util.safeRelease(mModules);
  }

  private void notifyChildClose(Shell shell)
  {
    // Async code might invalidate job
    // but removeJob can handle invalid pointers
    ShellThread job = findJobByShell(shell);
    if(job != null)
      removeJob(job);

  }

  private ShellThread findJobByShell(Shell shell)
  {

    List<ShellThread> children = getChildren(false);

    if(children != null)
      synchronized (children) {
        for (ShellThread t : children) {
          if(t.getShell() == shell)
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
  protected void finalize() throws Throwable
  {
    close();
  }

  public XEnvironment getEnv()
  {
    return mEnv;
  }

  public SessionEnvironment getSession()
  {
    return mSession;
  }
  
  public ICommandExpr parseScript(Reader reader, String source) throws CoreException
  {

    try {

      enterEval();
      ShellParser parser = new ShellParser(newParserReader(reader,false), source );
      return parser.script();

    } catch (Exception e) {
      throw new CoreException("Exception parsing source: " + source, e);
    }

    finally {
      exitEval();
    }
    
  }


  public ICommandExpr parseEval(String scmd) throws CoreException
  {

    InputStream save = mCommandInput;
    InputStream is = null;

    try {

      enterEval();

      is = Util.toInputStream(scmd, getSerializeOpts());
      mCommandInput = is;
      ShellParser parser = new ShellParser(newParserReader(false), "<eval>");

      ICommandExpr c = parser.script();
      return c;

    } catch (Exception e) {
      throw new CoreException("Exception parsing command: " + scmd, e);
    }

    finally {
      mCommandInput = save;
      Util.safeClose(is);
      exitEval();
    }
  }

  private void exitEval()
  {
    int d;
    if((d = mRunDepth.decrementAndGet()) < 0) {
      mLogger.error("SNH: run depth underrun: " + d + " resetting to 0: stack {}" , (Object[])Thread.currentThread().getStackTrace() );
      mRunDepth.compareAndSet(d, 0);
    }
  }

  private void enterEval()
  {
    mRunDepth.incrementAndGet();
  }

  public boolean isExecuting()
  {
    return mRunDepth.get() > 0;
  }

  
  public boolean validateScript( InputStream stream, String source ) throws ParseException, IOException{
      SourceLocation saveLoc = getLocation();
      ShellParser parser = null;
      boolean bSuccess = false ;
      try {
        enterEval();
        parser = new ShellParser(newParserReader(stream,true), source);
        
        while(  parser.command_line() != null )
          ;
        bSuccess = true ;

      } finally {
        exitEval();
        setCurrentLocation(saveLoc);

      }
      return bSuccess;
      
        
  }
  
  
  public int runScript(InputStream stream, String source, boolean convertReturn) throws ParseException, ThrowException,
      IOException
  {

    try {
      enterEval();
      InputStream save = mCommandInput;
      SourceLocation saveLoc = getLocation();
      mCommandInput = stream;
      int ret = 0;
      ShellParser parser = null;
      try {
        parser = new ShellParser(newParserReader(true), source);
        while ( ! hasReturned() ) {
          CommandExpr c = parser.command_line();
          if(c == null)
            break;

          setSourceLocation(c.getLocation());
          if(mOpts.mVerbose || mOpts.mLocation) {

            if(mOpts.mLocation) {
              mLogger.info(formatLocation());
            }

            if(mOpts.mVerbose) {
              String s = c.toString(false);

              if(s.length() > 0) {
                printErr(s);
              }
            }
          }

          ret = exec(c);
        }
      }

      catch (ThrowException e) {
        // mLogger.info("Rethrowing throw exception",e);
        throw e;	// rethrow

      } catch (ExitOnErrorException e) {
        // Caught Exit on error from a script
        ret = e.getValue();

      }

      catch (Exception | Error e ) {
        printErr(e.getMessage());
        mLogger.error("Exception parsing statement", e);
        parser.ReInit(newParserReader(false), source);

      } finally {
        mCommandInput = save;
        setCurrentLocation(saveLoc);

      }

      // Exited evaluation - but still in entry point

      if(mExitVal != null)
        ret = mExitVal.intValue();
      // Special check for returning thrown  errors 
      else if(convertReturn  && mReturnVal != null  ) {
        ret = this.convertReturnValueToExitValue(mReturnVal);
        mReturnVal = null;
      }

      onSignal("EXIT");
      return ret;

    } finally {
      exitEval();
    }
  }

  private boolean hasReturned()
  {
    return  !(mExitVal == null && mReturnVal == null) ;
  }

  private ShellParserReader newParserReader(boolean bSkipBOM) throws IOException
  {
    return newParserReader( mCommandInput , bSkipBOM);
  }

  private ShellParserReader newParserReader(InputStream stream, boolean bSkipBOM) throws IOException
  {
     return newParserReader( new InputStreamReader(stream, getInputTextEncoding()) , bSkipBOM );
  }
  
  private ShellParserReader newParserReader(Reader reader, boolean bSkipBOM) throws IOException
  {
    return new ShellParserReader(reader, bSkipBOM);
  }

  private void prompt(boolean ps1)
  {
    System.out.print(getPS(ps1 ? ShellConstants.PS1 : ShellConstants.PS2, ps1 ? "$ " : " >"));
    System.out.flush();
  }

  public String getInputTextEncoding()
  {
    return getSerializeOpts().getInputTextEncoding();
  }

  public int interactive() throws Exception
  {
    return interactive(null);
  }

  public int interactive(InputStream input) throws Exception

  {
    mIsInteractive = true;
    int ret = 0;

    setCommandInput(input);
    try {
      enterEval();

      // ShellParser parser= new
      // ShellParser(mCommandInput,Shell.getEncoding());
      ShellParser parser = new ShellParser(newParserReader(false), "stdin");

      while (mExitVal == null) {

        CommandExpr c = null;
        try {
          prompt(true);
          c = parser.command_line();

          if(c == null)
            break;
          setSourceLocation(c.getLocation());

          if(mOpts.mVerbose) {
            String s = c.toString(false);
            if(s.length() > 0) {
              SourceLocation loc = getLocation();
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
          parser.ReInit(newParserReader(false), null);
        } catch (Exception e) {

          SourceLocation loc = c != null ? c.getLocation() : null;

          if(loc != null) {
            String sLoc = loc.format(mOpts.mLocationFormat);
            mLogger.info(loc.format(mOpts.mLocationFormat));
            printErr(sLoc);
          }

          printErr(e.getMessage());
          mLogger.error("Exception parsing statement", e);
          parser.ReInit(newParserReader(false), null);
        } catch (Error e) {
          printErr("Error: " + e.getMessage());
          SourceLocation loc = c != null ? c.getLocation() : null;
          mLogger.error("Exception parsing statement", e);
          if(loc != null) {
            String sLoc = loc.format(mOpts.mLocationFormat);

            mLogger.info(loc.format(mOpts.mLocationFormat));
            printErr(sLoc);
          }
          parser.ReInit(newParserReader(false), null);
        }
      }
      if(mExitVal != null)
        ret = mExitVal.intValue();
      onSignal("EXIT");
    } finally {
      exitEval();
    }

    return ret;
  }

  public void setSourceLocation(SourceLocation loc)
  {
    setCurrentLocation(loc);
  }

  public void runRC(String rcfile) throws IOException, Exception
  {
    // Try to source the rcfile
    if(rcfile != null) {
      try {
        enterEval();
        File script = this.getFile(rcfile);
        if(script.exists() && script.canRead()) {
          ICommand icmd = CommandFactory.getInstance().getScript(this, script, SourceMode.SOURCE, null);
          if(icmd != null) {
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
  }

  public String getPS(String ps, String def)
  {

    XValue ps1 = getEnv().getVarValue(ps);
    if(ps1 == null)
      return def;
    String sps1 = ps1.toString();
    if(!Util.isBlank(sps1))
      try {
        sps1 = EvalUtils.expandStringToString(this, sps1, mPSEnv, null);
      } catch (IOException | CoreException e) {
        mLogger.debug("Exception getting PS var " + ps, e);
        return def;
      }
    return sps1;

  }

  /*
   * Setup the mCommandInput
   * Try to locate jline if its in the classpath and use it
   * otherwise default to System.in
   */

  private void setCommandInput(InputStream in)
  {
    mCommandInput = in;
    if(mCommandInput == null) {
      if(!Util.isWindows())
        try {
          /*
           * import jline.ConsoleReader;
           * import jline.ConsoleReaderInputStream;
           */
          Class<?> consoleReaderClass = Class.forName("jline.ConsoleReader");

          if(consoleReaderClass != null) {
            Class<?> consoleInputClass = Class.forName("jline.ConsoleReaderInputStream");
            if(consoleInputClass != null) {
              // ConsoleReader jline = new ConsoleReader();
              Object jline = consoleReaderClass.newInstance();

              Constructor<?> constructor = consoleInputClass.getConstructor(consoleReaderClass);
              // mCommandInput = new ConsoleReaderInputStream(jline);

              if(constructor != null) {
                mCommandInput = (InputStream) constructor.newInstance(jline);
                // System.err.println("using jline");
              }

            }
          }

        } catch (Exception e1) {
          mLogger.info("Exception loading jline");
        }
    }
    if(mCommandInput == null)
      mCommandInput = System.in;
  }

  /*
   * Main entry point for executing commands.
   * All command execution should go through this entry point
   * 
   * Handles background shell ("&")
   * Handles "throw on error" (-e)
   */
  public int exec(ICommandExpr c) throws ThrowException, ExitOnErrorException
  {
    return exec(c, getLocation(c));
  }

  // Default location for command
  private SourceLocation getLocation(ICommandExpr c)
  {

    return c.hasLocation() ? c.getLocation() : getLocation();
  }

  public int exec(ICommandExpr c, SourceLocation loc) throws ThrowException, ExitOnErrorException
  {

    try {
      enterEval();
      if(loc == null)
        loc = c.getLocation();

      setCurrentLocation(loc);

      if(mOpts.mExec|| mOpts.mTrace ) {
        logExec(c,mOpts.mExec, loc);
      }

      try {

        if(c.isWait()) {
          // Execute forground command
          mStatus = c.exec(this);

          // If not success then may throw if option 'throw on error'
          // is set (-e)
          if(mStatus != 0 && mOpts.mThrowOnError && c.isSimple()) {
            if(!isInCommandConndition())
              throw new ExitOnErrorException(mStatus);
          }
          return mStatus;
        }

        ShellThread sht = new ShellThread(newThreadGroup(c.getName()), new Shell(this), null, c);

        if(isInteractive())
          printErr("" + sht.getId());

        addJob(sht);
        sht.start();

        return mStatus = 0;
      } catch (ThrowException e) {
        // mLogger.info("Rethrowing ThrowException",e);
        throw e;
      } catch (ExitOnErrorException e) {
        // rethrow
        throw e;
      }

      catch (Exception e) {
        printLoc(mLogger, loc);

        printErr("Exception running: " + c.toString(true));
        printErr(e.toString(), loc);

        mLogger.error("Exception running command: " + c.toString(false), e);
        mStatus = -1;
        // If not success then may throw if option 'throw on error' is
        // set (-e)
        if(mStatus != 0 && mOpts.mThrowOnError && c.isSimple()) {
          if(!isInCommandConndition())
            throw new ThrowException(XValue.newXValue(mStatus));
        }
        return mStatus;

      }
    } finally {
      exitEval();
    }
  }

  public void logExec(ICommandExpr c, boolean bExec , SourceLocation loc)
  {
    
    StringBuilderWriter sw = new StringBuilderWriter();
    PrintWriter w = new PrintWriter(sw);

    c.print(w, bExec);
    w.flush();
    
    StringBuilder sb = sw.getBuilder();
    
    
    if(sb.length() > 0) {
        String scmd = sb.toString();
        if( mOpts.mTrace && mLogger.isEnabled(mOpts.mTraceLevel)) {
          traceExec( loc , scmd );
        }
        if( mOpts.mExec ) {
          if(loc != null ) {
            printErr("+ " + loc.format(mOpts.mLocationFormat));
            printErr(scmd);
          }
          else 
            printErr("+ " + scmd);
        }
    }
  }

  /*
   * Returns TRUE if the shell is currently in a condition
   */

  private void traceExec(SourceLocation loc, String scmd)
  {
    String strace = 
        SourceLocation.formatLocation( loc , mOpts.mLocationFormat ) + " " + scmd ;
    
    if( mOpts.mTraceFile != null ) {
      String sEnc = mOpts.mSerialize.getOutputTextEncoding();
      try {
        OutputPort op = getEnv().getOutput( mOpts.mTraceFile , true );
        try ( OutputStream os = op.asOutputStream(getSerializeOpts()) ){
          os.write( strace.getBytes(sEnc) );
          os.write(  Util.getNewline(getSerializeOpts()) );
          os.flush();
        }
      } catch (IOException | CoreException e) {
        mLogger.debug("Exception tracing output", e);
      }
      
    }
    else
      mLogger.log( mOpts.mTraceLevel ,  strace );
      
      
    
  }

  public ThreadGroup newThreadGroup(String name)
  {
    return new ThreadGroup(getThreadGroup(), name);
  }

  public boolean isInCommandConndition()
  {
    return mConditionDepth > 0;
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

  private boolean isInteractive()
  {
    return mIsInteractive;
  }

  private void addJob(ShellThread sht)
  {

    List<ShellThread> children = getChildren(true);
    synchronized (children) {
      children.add(sht);
    }

    mLastThreadId = sht.getId();
  }

  public void printErr(String s)
  {
    printErr(s, getLocation());
  }

  public void printErr(String s, SourceLocation loc)
  {
    try (
      PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getEnv().getStderr().asOutputStream(
        getSerializeOpts()), getSerializeOpts().getOutputTextEncoding())))) {
      if(mOpts.mLocation) {
        out.println(formatLocation(loc));
      }
      out.println(s);
      out.flush();
    } catch (UnsupportedEncodingException | CoreException e) {
      mLogger.error("Exception writing output: " + s, e);

    }

  }

  private String formatLocation()
  {
    return formatLocation(null);
  }

  private String formatLocation(SourceLocation loc)
  {
    if(loc == null)
      loc = getLocation();
    return loc == null ? "<?>" : loc.format(mOpts.mLocationFormat);
  }

  public void printOut(String s)
  {
    try (
      PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(getEnv().getStdout().asOutputStream(
        getSerializeOpts()), getSerializeOpts().getOutputTextEncoding())))) {
      out.println(s);
      out.flush();
    } catch (IOException | CoreException e) {
      mLogger.error("Exception writing output: " + s, e);
      return;
    }

  }

  public void printErr(String s, Exception e)
  {
    printErr(s, e, null);
  }

  public void printErr(String s, Exception e, SourceLocation loc)
  {
    try (PrintWriter out = getEnv().getStderr().asPrintWriter(getSerializeOpts())) {
      if(loc == null)
        loc = getLocation();
      if(loc != null)
        out.println(formatLocation(loc));

      out.println(s);

      out.println(e.getMessage());
      for (Throwable t = e.getCause(); t != null; t = t.getCause()) {
        out.println("  Caused By: " + t.getMessage());
      }

      out.flush();
    } catch (IOException | CoreException e1) {
      mLogger.error("Exception writing output: " + s, e);
      return;
    }

  }

  public static void main(String argv[]) throws Exception
  {
    List<XValue> vargs = new ArrayList<XValue>(argv.length);
    for (String a : argv)
      vargs.add(XValue.newXValue(a));

    Shell shell = new Shell(true);
    org.xmlsh.builtin.commands.xmlsh cmd = new org.xmlsh.builtin.commands.xmlsh(true);

    int ret = -1;
    try {
      shell.enterEval();
      ret = cmd.run(shell, "xmlsh", vargs);
    } catch (Throwable e) {
      mLogger.error("Uncaught exception in main", e);
    } finally {
      shell.exitEval();
      shell.close();
    }

    System.exit(ret);

  }

  public void setArg0(String string)
  {
    mArg0 = string;

  }

  // Translate a shell return code to java bool
  public static boolean toBool(int intVal)
  {
    return intVal == 0;

  }

  // Translate a java bool to a shell return code
  public static int fromBool(boolean boolVal)
  {
    return boolVal ? 0 : 1;
  }

  public Path getExternalPath()
  {
    return getPath(ShellConstants.PATH, true);
  }

  public Path getPath(String var, boolean bSeqVar)
  {
    XValue pathVar = getEnv().getVarValue(var);
    if(pathVar == null)
      return new Path();
    if(bSeqVar)
      return new Path(pathVar);
    else return new Path(pathVar.toString().split(File.pathSeparator));

  }

  /*
   * Current Directory
   */
  public File getCurdir()
  {
    return new File(System.getProperty(ShellConstants.PROP_USER_DIR));

  }

  public void setCurdir(File cd) throws IOException
  {
    String dir = cd.getCanonicalPath();
    SystemEnvironment.getInstance().setProperty(ShellConstants.PROP_USER_DIR, dir);

  }

  public void setArgs(List<XValue> args)
  {
    mArgs = new CopyOnWriteArrayList<>(args);

  }

  public File getExplicitFile(String name, boolean mustExist) throws IOException
  {
    return getExplicitFile(null, name, mustExist);
  }

  public File getExplicitFile(File dir, String name, boolean mustExist) throws IOException
  {

    File file = null;
    file = new File(dir, name);

    try {
      file = file.getCanonicalFile();
    } catch (IOException e) {
      mLogger.info("Exception translating file to canonical file", e);
      // try to still use file
    }
    if(mustExist && !file.exists())
      return null;

    return file;
  }

  public List<XValue> getArgs()
  {
    return mArgs;
  }

  public void exit(int retval)
  {
    mExitVal = Integer.valueOf(retval);
  }

  public void exec_return(XValue retval)
  {
    mReturnVal = retval == null ? mReturnFlag : retval ;
  }

  /*
   * Return TRUE if we should keep running on this shell
   * Includes early termination in control stacks
   */
  public boolean keepRunning()
  {
    // Hit exit stop
    if(mClosed || hasReturned() )
      return false;

    // If the top control stack is break then stop
    if(hasControlStack()) {
      ControlLoop loop = getControlStack().peek();
      if(loop.mBreak || loop.mContinue)
        return false;
    }

    return true;

  }

  public String getArg0()
  {
    return mArg0;
  }

  /**
   * @return the status
   */
  public int getStatus()
  {
    return mStatus;
  }

  /**
   * @param status
   *          the status to set
   */
  public void setStatus(int status)
  {
    mStatus = status;
  }

  public File getFile(File dir, String file) throws IOException
  {
    return getExplicitFile(dir, file, false);
  }

  public File getFile(String fname) throws IOException
  {
    return getExplicitFile(fname, false);
  }

  public File getFile(XValue fvalue) throws IOException
  {
    return getFile(fvalue.toString());
  }

  public void shift(int num)
  {
    num = Math.min(num, mArgs.size());
    if(num <= 0)
      return;
    mArgs = mArgs.subList(num, mArgs.size());

  }

  /*
   * Returns the singleton processor for all of Xmlsh
   */
  public static synchronized Processor getProcessor()
  {
    if(mProcessor == null) {
      String saxon_ee = System.getenv("XMLSH_SAXON_EE");
      boolean bEE = Util.isEmpty(saxon_ee) ? true : Util.parseBoolean(saxon_ee);
      mProcessor = new Processor(bEE);

      /*
       * mProcessor.getUnderlyingConfiguration().getEditionCode();
       * 
       * System.err.println("Version " + mProcessor.getSaxonProductVersion() );
       * System.err.println("XQuery " + mProcessor.getConfigurationProperty(FeatureKeys.XQUERY_SCHEMA_AWARE) );
       * System.err.println("XSLT " + mProcessor.getConfigurationProperty(FeatureKeys.XSLT_SCHEMA_AWARE) );
       * System.err.println("Schema " + mProcessor.getConfigurationProperty(FeatureKeys.SCHEMA_VALIDATION ));
       */

      // mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL, net.sf.saxon.event.Builder.LINKED_TREE);
      mProcessor.registerExtensionFunction(new EvalDefinition());
      mProcessor.getUnderlyingConfiguration().setSerializerFactory(
        new XmlshSerializerFactory(mProcessor.getUnderlyingConfiguration()));
      mProcessor.getUnderlyingConfiguration().setErrorListener(new XmlshErrorListener());

    }

    return mProcessor;
  }

  public void removeJob(Thread job)
  {

    if(mChildren != null)
      synchronized (mChildren) {
        mChildren.remove(job);
        mChildren.notify();  // Must be in syncrhonized block
      }
  }

  // Kill a child shell whether or not its in our list of children
  public void killChild(ShellThread job, long waitTime)
  {
    if(job != Thread.currentThread()) {
      if(job.isAlive()) {
        try {
          job.shutdown(true, waitTime);
        } catch (Exception e) {

          mLogger.warn("Exception trying to close child shell: " + job.describe());
        }
      }
      else job.interrupt();

      Thread.yield();
      if(waitTime >= 0) {
        try {
          job.join(waitTime);
        } catch (InterruptedException e) {
          mLogger.warn("Exception trying to wait for shell: " + job.describe());
        }
      }
      Thread.yield();

      if(job.isAlive())
        mLogger.warn("Failed to kill child shell: " + job.describe());
    }
  }

  /*
   * Returns the children of the current thread
   * copied into a collection so that it is thread safe
   */

  public List<ShellThread> getChildren(boolean create)
  {
    // mChildren is only set never chaqnged or cleared

    // Memory Barrier
    if(mChildren == null && create) {
      synchronized (this) {
        if(mChildren == null)
          mChildren = Collections.synchronizedList(new ArrayList<ShellThread>());
      }
    }

    return mChildren;
  }

  /*
   * Waits until there are "at most n" running children of this shell
   */
  public boolean waitAtMostChildren(int n, long waitTime)
  {
    long end = System.currentTimeMillis() + waitTime;

    waitTime = Util.nextWait(end, waitTime);
    while (childrenSize() > n) {
      if(waitTime < 0)
        return false;
      try {
        enterEval(); // equivilent to an eval - were blocking
        if(mChildren != null)
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

  private int childrenSize()
  {
    // memory barrier
    if(mChildren == null)
      return 0;
    synchronized (mChildren) {
      return mChildren.size();
    }
  }

  public long getLastThreadId()
  {
    // TODO Auto-generated method stub
    return mLastThreadId;
  }

  /*
   * Break n levels of control stacks
   * -1 means break all
   */
  public int doBreak(int levels)
  {
    if(!hasControlStack())
      return 0;

    int end = getControlStack().size() - 1;
    if(levels < 0) {
      while (end >= 0)
        getControlStack().get(end--).mBreak = true;
    }
    else {

      while (levels-- > 0 && end >= 0)
        getControlStack().get(end--).mBreak = true;

    }
    return 0;

  }

  /*
   * Continue n levels of control stacks
   */

  public int doContinue(int levels)
  {
    if(!hasControlStack())
      return 0;

    int end = getControlStack().size() - 1;

    /*
     * Break n-1 levels
     */
    while (levels-- > 1 && end >= 0)
      getControlStack().get(end--).mBreak = true;

    // Continue the final level
    if(end >= 0)
      getControlStack().get(end).mContinue = true;

    return 0;
  }

  public ControlLoop pushLoop(SourceLocation sourceLocation)
  {
    ControlLoop loop = new ControlLoop(sourceLocation);
    getControlStack().add(loop);
    return loop;
  }

  /*
   * Pop the control stack until we hit loop, if loop isnt found (SNH) pop until empty
   */
  public void popLoop(ControlLoop loop)
  {

    while (hasControlStack())
      if(mControlStack.pop() == loop)
        break;
  }

  public void declareFunction(IFunctionDecl func)
  {
    if(mFunctions == null)
      mFunctions = new FunctionDefinitions();
    mFunctions.put(func.getName(), func);

  }

  public IFunctionDecl getFunctionDecl(String name)
  {

    if(mFunctions == null)
      return null;
    return mFunctions.get(name);
  }

  public Iterable<IModule> getModules()
  {
    return mModules;
  }

  /*
   * Execute a function as a command
   * Extracts return values from the function if present
   */
  public int execFunctionAsCommand(String name, ICommandExpr cmd, SourceLocation location, List<XValue> args) throws Exception
  {

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
  public XValue runCommandFunction(String name , ICommandExpr mBody, SourceLocation location , List<XValue> args) throws ThrowException, ExitOnErrorException
  {
    
    List<XValue> saveArgs = getArgs();
    String saveArg0 = getArg0();
    Variables save_vars = pushLocalVars();

    getCallStack().push(new CallStackEntry(name, mBody , location  ));
    setArg0(name);
    setArgs(args);

    try {
      int ret = exec(mBody, mBody.getLocation());
      return this.getReturnValue();

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
  private int convertReturnValueToExitValue(XValue value)
  {
    // Special case for null values
    if( value == null || value == mReturnFlag )
      return 0 ; 

    // Null is false (1)
    if(value.isNull())
      return 1;
    
    // Change: Empty sequence is false
    if( value.isEmpty() || value.isEmptySequence() )
      return 1;
    
    /*
     * Special: if looks like an integer then thats the exit value
     */
    if(value.isAtomic()) {
      // Check if native boolean

      String s = value.toString();
      // Empty string is false
      if(Util.isBlank(s))
        return 0;

      if(Util.isInt(s, true))
        return Integer.parseInt(s);
      else if(s.equalsIgnoreCase("true"))
        return 0;
      else return 1; // False

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
  public boolean importModule(String moduledef, XValue at ,List<XValue> init) throws Exception
  {

    return mModules.declare(this, moduledef, at , init);

  }

  
  public boolean importScript( String script , XValue at ,List<XValue> init  ) throws Exception{
    
    return mModules.declare(this, script, at , init);

}

  public boolean  importPackage(String prefix, String name, List<String> packages ) throws Exception
  {
    // TODO: Help needs better placement
    String sHelp = packages.get(0).replace('.', '/') + "/commands.xml";

    return mModules.declare(ModuleFactory.createPackageModule(null, prefix, name, packages, sHelp),null);
  }

  public void importJava(XValue uris) throws CoreException
  {
    mClassLoader = getClassLoader(uris);

  }

  public URL getURL(String file) throws CoreException
  {
    URL url = Util.tryURL(file);
    if(url == null)
      try {
        url = getFile(file).toURI().toURL();
      } catch (MalformedURLException e) {
        throw new CoreException(e);
      } catch (IOException e) {
        throw new CoreException(e);
      }
    return url;

  }

  public URI getURI(String file) throws MalformedURLException, IOException
  {
    URI uri = Util.tryURI(file);
    if(uri == null)
      uri = getFile(file).toURI();
    return uri;

  }

  public InputPort newInputPort(String file) throws IOException
  {
    /*
     * Special case to support /dev/null file on Windows systems
     * Doesnt hurt on unix either to fake this out instead of using the OS
     */
    if(FileUtils.isNullFilePath(file)) {
      return new StreamInputPort(new NullInputStream(), file);
    }

    URL url = Util.tryURL(file);

    if(url != null) {

      return new StreamInputPort(url.openStream(), url.toExternalForm());

    }
    else return new FileInputPort(getFile(file));
  }

  // Returns a new port need to manage the port properly
  public OutputPort newOutputPort(String file, boolean append) throws FileNotFoundException, IOException
  {
    if(FileUtils.isNullFilePath(file)) {
      return new StreamOutputPort(new NullOutputStream());
    }

    else {
      URL url = Util.tryURL(file);
      if(url != null)
        return new StreamOutputPort(url.openConnection().getOutputStream());

    }
    return new FileOutputPort(getFile(file), append);
  }

  public OutputStream getOutputStream(String file, boolean append, SerializeOpts opts) throws FileNotFoundException,
      IOException, CoreException
  {
    OutputPort p = newOutputPort(file, append);
    addAutoRelease(p);
    return p.asOutputStream(opts);

  }

  public void addAutoRelease(OutputPort p)
  {
    getEnv().addAutoRelease(p);
  }

  public OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException
  {

    return new FileOutputStream(file, append);
  }

  public void setOption(String name, boolean flag)
  {
    mOpts.setOption(name, flag);

  }

  public void setOption(String name, XValue value) throws InvalidArgumentException
  {
    mOpts.setOption(name, value);

  }

  public void setModule(IModule module)
  {
    mModule = module;

  }

  public IModule getModule()
  {
    return mModule;
  }

  /**
   * @return the opts
   */
  public ShellOpts getOpts()
  {
    return mOpts;
  }

  /*
   * Executes a command as a condition so that it doesnt throw
   * an exception if errors
   */
  public int execCondition(CommandExpr left) throws ThrowException, ExitOnErrorException
  {

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

  public URL getResource(String res)
  {
    URL url = getClass().getResource(res);
    if(url != null)
      return url;

    for (IModule m : mModules) {
      url = m.getResource(res);
      if(url != null)
        return url;
    }
    return null;

  }

  public void setOptions(Options opts) throws InvalidArgumentException
  {
    for (OptionValue ov : opts.getOpts()) {
      setOption(ov);
    }

  }

  private void setOption(OptionValue ov) throws InvalidArgumentException
  {
    mOpts.setOption(ov);

  }

  public SerializeOpts getSerializeOpts(Options opts) throws InvalidArgumentException
  {
    if(opts == null || opts.getOpts() == null)
      return mOpts.mSerialize;

    SerializeOpts sopts = mOpts.mSerialize.clone();
    sopts.setOptions(opts);
    return sopts;
  }

  public SerializeOpts getSerializeOpts()
  {
    return mOpts.mSerialize;

  }

  public Variables pushLocalVars()
  {
    return mEnv.pushLocalVars();

  }

  public void popLocalVars(Variables vars)
  {
    mEnv.popLocalVars(vars);

  }

  public int requireVersion(String module, String sreq)
  {
    // Creates a 3-4 element array [ "1" , "0" , "1" , ? ]
    String aver[] = Version.getVersion().split("\\.");
    String areq[] = sreq.split("\\.");

    // Start with major and go down
    for (int i = 0; i < Math.max(aver.length, areq.length); i++) {
      if(i >= areq.length)
        break;
      int ireq = Util.parseInt(areq[i], 0);
      int iver = i >= aver.length ? 0 : Util.parseInt(aver[i], 0);

      // Same version OK check minor
      if(ireq == iver)
        continue;
      else if(ireq < iver)
        break;
      else if(ireq > iver) {
        return -1;
      }

    }
    return 0;
  }

  /*
   * Get the return value of the last return statement
   */
  public XValue getReturnValue()
  {
    XValue ret = mReturnVal;
    mReturnVal = null;
    return (ret == null || ret == mReturnFlag ) ? XValue.empytSequence() : ret ;
  }

  public ClassLoader getClassLoader(XValue classpath) throws CoreException
  {

    // No class path sent, use this shells or this class
    if(classpath == null) {
      if(mClassLoader != null)
        return mClassLoader;
      else return this.getClass().getClassLoader();
    }

    final List<URL> urls = new ArrayList<URL>();
    for (XValue item : classpath) {
      String cp = item.toString();
      URL url = getURL(cp);
      urls.add(url);
    }

    final ClassLoader parent = getClass().getClassLoader();

    URLClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>()
      {
        @Override
        public URLClassLoader run()
        {
          return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
        }
      });
    return loader;
  }

  public void printLoc(Logger logger, SourceLocation loc)
  {

    if(loc != null) {
      String sLoc = loc.format(mOpts.mLocationFormat);
      logger.info(sLoc);

    }
  }

  public void trap(String signal, String cmd)
  {
    if(Util.isBlank(signal) || Util.isBlank(cmd))
      return;
    if(mTraps == null)
      mTraps = new HashMap<String, String>();

    if(signal.equals("0"))
      signal = "EXIT";
    mTraps.put(signal, cmd);

  }

  void onSignal(String signal)
  {

    // TODO: Should we avoid thiws on shutdown ?
    if(mTraps == null)
      return;
    String scmd = mTraps.get(signal);

    if(scmd == null)
      return;

    try {
      ICommandExpr c = parseEval(scmd);
      exec(c);
    } catch (Exception e) {
      this.printErr("Exception running trap: " + signal + ":" + scmd, e);
    }

  }

  public SourceLocation getLocation()
  {

    return mCurrentLocation == null ? new SourceLocation() : mCurrentLocation;
  }

  public SourceLocation getLocation(int depth)
  {
    if(depth < 0)
      return getLocation();
    if(!hasCallStack())
      return null;

    if(mCallStack.size() <= depth)
      return null;
    return mCallStack.get(depth).makeLocation();

  }

  public synchronized Shell getParent()
  {
    return mParent;
  }

  private boolean hasControlStack()
  {
    return mControlStack != null && !mControlStack.empty();
  }

  private Stack<ControlLoop> getControlStack()
  {
    if(mControlStack == null)
      mControlStack = new Stack<ControlLoop>();
    return mControlStack;
  }

  public boolean hasCallStack()
  {
    return mCallStack != null && !mCallStack.empty();
  }

  public Stack<CallStackEntry> getCallStack()
  {
    if(mCallStack == null)
      mCallStack = new Stack<CallStackEntry>();
    return mCallStack;
  }

  public int getReturnValueAsExitValue()
  {

// Special null return value means 0  - SNH need to check logic of scripts
    if( mReturnVal == null )
      return 0;
      return convertReturnValueToExitValue(getReturnValue());
  }

  public void setCurrentLocation(SourceLocation loc)
  {

    if(loc == null && mCurrentLocation != null)
      mLogger.debug("Overriting current location with null");
    else mCurrentLocation = loc;
  }

  public IFS getIFS()
  {
    String sisf = this.getEnv().getVarString("IFS");
    if(mIFS == null || !mIFS.isCurrent(sisf))
      ;

    mIFS = new IFS(sisf);
    return mIFS;
  }

  public ThreadGroup getThreadGroup()
  {
    return mThreadGroup == null ? Thread.currentThread().getThreadGroup() : mThreadGroup;
  }

  public ShellThread getFirstThreadChild()
  {
    if(mChildren == null)
      return null;
    synchronized (mChildren) {
      return mChildren.isEmpty() ? null : mChildren.get(0);
    }

  }

  // Shutdown the shell
  // Intended to be called from a external thread to try to force the shell to quit
  // Dont actually close it - causes too much asyncronous grief

  public boolean shutdown(boolean force, long waitTime)
  {
    if(mClosed)
      return true;
    // Mark us done
    mExitVal = Integer.valueOf(0);

    long end = System.currentTimeMillis() + waitTime;
    waitTime = Util.nextWait(end, waitTime);

    // Break all
    doBreak(-1);

    List<ShellThread> children = getChildren(false);
    if(force) {
      if(children != null) {
        synchronized (children) {
          children = new ArrayList<>(children);
        }
        for (ShellThread c : children) {
          waitTime = Util.nextWait(end, waitTime);
          this.killChild(c, waitTime);
        }
      }
      terminateChildProcesses();
    }
    waitTime = Util.nextWait(end, waitTime);
    this.waitAtMostChildren(0, waitTime);
    Thread.yield();
    return mClosed;

  }

  public void addChildProcess(Process proc)
  {
    if(mChildProcess == null) {
      synchronized (this) {
        if(mChildProcess == null)
          mChildProcess = Collections.synchronizedList(new ArrayList<Process>());
      }
    }
    synchronized (mChildProcess) {
      mChildProcess.add(proc);
    }
  }

  public void removeChildProcess(Process proc)
  {
    if(mChildProcess == null)
      return;
    mChildProcess.remove(proc);
  }

  public void terminateChildProcesses()
  {
    if(mChildProcess == null)
      return;
    synchronized (mChildProcess) {
      for (Process p : mChildProcess)
        p.destroy();
      mChildProcess.clear();
    }
  }

  public boolean isClosed()
  {
    return mClosed;
  }

  public IModule getModuleByPrefix(String prefix)
  {
    return mModules.getModuleByPrefix(prefix);
  }

public FunctionDefinitions getFunctionDelcs() {
	
	// HACK for now 
	return new FunctionDefinitions(mFunctions);
	
}







}
//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
