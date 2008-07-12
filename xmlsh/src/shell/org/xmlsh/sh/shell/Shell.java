/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

import net.sf.saxon.s9api.Processor;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.Path;
import org.xmlsh.core.XDynamicVariable;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.sh.grammar.ShellParser;
import org.xmlsh.util.Util;

public class Shell {
	private		Stack<XEnvironment>	mEnvStack = new Stack<XEnvironment>();
	private		List<XValue> 	mArgs = new ArrayList<XValue>();
	private		InputStream	mCommandInput = null;
	private		String	mArg0 = "xmlsh";
	
	// Set to non null until exit or EOF
	private 	Integer mExitVal = null;
	
	private		int	    mStatus = 0;	// $? variable
	private		static Processor	mProcessor = null;
	
	private 	String  mSavedCD = null;
	

	private		List<ShellThread>	mChildren = new ArrayList<ShellThread>();
	private boolean mIsInteractive = false ;
	

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
	public Shell()
	{
		mSavedCD = System.getProperty("user.dir");
		mEnvStack.push( new XEnvironment(this));
		
		setGlobalVars();
		
		
	}
	
	/*
	 * Populate the environment with any global variables
	 */
	
	private void setGlobalVars() {
	    
		
		// Export path to shell path
	    String path = System.getenv("PATH");
	    getEnv().setVar( new XVariable("PATH", new XValue(path)));
	
		
		
		getEnv().setVar(
				new XDynamicVariable("PWD" , EnumSet.of( XVarFlag.READONLY , XVarFlag.XEXPR )) { 
					public XValue getValue() 
					{
						return new XValue( getEnv().getCurdir().getAbsolutePath() ) ;
					}
					
				}
				
				
		);
		
		getEnv().setVar("TMPDIR" , System.getProperty("java.io.tmpdir"));
	}

	/*
	 * Cloned shell for sub-thread execution
	 */
	private Shell( Shell that )
	{
		mEnvStack.push( that.getEnv().clone(this) );
		mCommandInput = that.mCommandInput;
		mArg0 = that.mArg0;
		mSavedCD = System.getProperty("user.dir");
				
	}
	
	public Shell clone()
	{
		return new Shell( this );
	}
	
	
	public void close()
	{
		while( !mEnvStack.isEmpty() )
			mEnvStack.pop().close();
		
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
		return 	mEnvStack.peek();
	}
	public XEnvironment getParentEnv()
	{
		return mEnvStack.get(mEnvStack.size()-2);
	}
	

	
	
	public		int		runScript( InputStream stream ) throws ParseException
	{
		
		InputStream save = mCommandInput;
		mCommandInput = stream ;
		ShellParser parser= new ShellParser(mCommandInput);
		int ret = 0;
		try {
			while( mExitVal == null ){
		      	Command c = parser.command_line();
		      	if( c == null )
		      		break;
		      
		      	ret = exec( c );
			}
					
			
		
		} catch (Exception e) {
	       // System.out.println("NOK.");
	        System.out.println(e.getMessage());
	        e.printStackTrace(System.out);
	        parser.ReInit(mCommandInput);
	      } catch (Error e) {
	       //  System.out.println("Error");
	        System.out.println(e.getMessage());
	        parser.ReInit(mCommandInput);
	
	     } 
      
		
		
		finally {
			mCommandInput = save;
		}
		if( mExitVal != null )
			ret = mExitVal.intValue();
		return ret;
		
	}
	
	
	
	
	private		int		interactive()
	{
		mIsInteractive = true ;
		int		ret = 0;
		mCommandInput = System.in;
		ShellParser parser= new ShellParser(mCommandInput);
		
		while (mExitVal == null) {
			

		      System.out.print("$ ");
		      try {
		      	Command c = parser.command_line();
		      	if( c == null )
		      		break;
		      
		      	ret = exec( c );
		      	
		      	// PrintWriter out = new PrintWriter( System.out );
		      	//s.print(out);
		      	//out.flush();
		      	
		      } catch (Exception e) {
		        System.out.println("NOK.");
		        System.out.println(e.getMessage());
		        e.printStackTrace(System.out);
		        parser.ReInit(mCommandInput);
		      } catch (Error e) {
		        System.out.println("Error");
		        System.out.println(e.getMessage());
		        parser.ReInit(mCommandInput);

		      } 
		      
		}
		if( mExitVal != null )
			ret = mExitVal.intValue();
		return ret;
	}
	
	/*
	 * Expand a single string 
	 * 1) quote expansion
	 * 2) variable expansion
	 * 3) wildcard expansion
	 */
	
	public int exec(Command c) throws Exception {
		if( c.isWait())
			return mStatus = c.exec(this);
		
		ShellThread sht = new ShellThread( new Shell(this) , this , c);
		
		if( isInteractive() )
			printErr( "" + sht.getId() );
		sht.start();
		addJob( sht );
		return mStatus = 0;
		
		
	}

	private boolean isInteractive() {
		return mIsInteractive ;
	}

	private synchronized void addJob(ShellThread sht) {
		mChildren.add(sht);
	}

	public void printErr(String s) {
		PrintWriter out = new PrintWriter( getEnv().getStderr() );
		out.println(s);

		out.flush();
		
	}
	public void printErr(String s,Exception e) {
		PrintWriter out = new PrintWriter( getEnv().getStderr() );
		out.println(s);
		out.println(e.getMessage());
		
		out.flush();
		
	}

	public static void main(String args[]) throws Exception {
	 	
		
		Shell shell = new Shell();
	    
	    
	    
	    int ret = 0;
	    if( args.length == 0 ){
	    	ret = shell.interactive();
	    } else {
	    	
	    	ICommand cmd = CommandFactory.getInstance().getScript( shell , args[0], true );
	    	if( cmd == null )
	    		shell.printErr( args[0] + ": not found");
	    	else {
	    		
	    		List<XValue> args2 = new ArrayList<XValue>(args.length-1);
	    		for( int i = 1 ; i < args.length ; i++ )
	    			args2.add( new XValue(args[i]));
	    		// Run as sourced mode, in this shell ...
	    		// must set args ourselves
	    		
	    		shell.setArg0( args[0]);
	    		shell.setArgs( args2 );
	    		ret = cmd.run( shell , args[0] , null );
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
	
	public void pushEnv() 
	{
		mEnvStack.push( getEnv().clone());
		
		
	}

	public void popEnv() throws IOException {
		XEnvironment env = mEnvStack.pop();
		env.close();
		
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
	public boolean keepRunning()
	{
		return mExitVal == null;
	}

	public String getArg0() {
		return mArg0;
	}

	public List<XValue> expand(String s, boolean bExpandSequences ) throws IOException {
		Expander e = new Expander( this );
		List<XValue> result =  e.expand(s);
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
	

	public XValue expandString(String value) throws IOException {
		List<XValue> ret = expand(value,false);
		if( ret.size() == 0 )
			return new XValue();
		else
		if( ret.size() == 1 )
			return ret.get(0);
		
		StringBuffer sb = new StringBuffer();
		for( XValue v : ret ){
			if( sb.length() > 0 )
				sb.append(' ');
			sb.append( v.toString() );
		}
		return new XValue(sb.toString());
		
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
