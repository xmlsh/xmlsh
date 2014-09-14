/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import static org.xmlsh.core.XVariable.XVarFlag.EXPORT;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Stack;

import javax.xml.transform.Source;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xmlsh.builtin.commands.exit;
import org.xmlsh.sh.shell.FunctionDefinitions;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.ModuleHandle;
import org.xmlsh.sh.shell.Modules;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.StaticContext;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;

public class XEnvironment implements AutoCloseable, Closeable {

	@SuppressWarnings("unused")
	private 	static Logger mLogger = LogManager.getLogger( XEnvironment.class );
	private 	Shell mShell;
	private		volatile XIOEnvironment mIO;
	private		Variables	mVars;
	private     StaticContext  mStaticContext;
	private		Stack<XIOEnvironment>  mSavedIO;
	private     Stack<ModuleHandle> mModuleStack;
	private     boolean bClosed = false;




	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		if( ! bClosed )
		close();
	}


	public XEnvironment(Shell shell, StaticContext ctx , boolean bInitIO ) throws IOException
	{
		
		mLogger.entry(shell, ctx, bInitIO);
		mStaticContext = ctx ;
		mShell = shell;
		mVars = new Variables();
		mIO = new XIOEnvironment();
		mModuleStack = new Stack<>();
		pushModule(shell.getModule());

		if( bInitIO )
			getIO().initStdio();


		mLogger.exit();
	}




	public void	addAutoRelease( AbstractPort obj )
	{
		getIO().addAutoRelease(obj);
	}


	private XIOEnvironment getIO()
	{
		if( mIO == null ) {
			synchronized( this ) {
				mIO = new XIOEnvironment();
			}
		}
		return mIO ;
	}

	/*
	 * Standard Varibles 
	 */
	public XVariable	getVar( String name )
	{
		/*
		 * Special variables 
		 */
	 
		if( name == null ) {
			// $* 
	    	 return new XVariable(null, XValue.newXValue(mShell.getArgs()));
		}
		
		switch( name ) {
		case "*" :
			return new XVariable(null, XValue.newXValue(mShell.getArgs()));
		case "@" :
			return  mShell.getArgs().size() == 0 ? null 
					: new XVariable(null, XValue.newXValue(mShell.getArgs()));
	     case "#" :
      return new XVariable(name, XValue.newXValue(mShell.getArgs().size()));
	     case "$" :
      return new XVariable(name, XValue.newXValue(Thread.currentThread().getId()));
	     case "?" :
      return new XVariable(name, XValue.newXValue(mShell.getStatus()));
	     case "!" :
      return new XVariable(name, XValue.newXValue(mShell.getLastThreadId()));
		}
		if(Util.isInt(name, false)) {
           int n = Util.parseInt(name, -1);
           if(n == 0)
            return new XVariable(name, XValue.newXValue(mShell.getArg0()));
          else if(n > 0 && n <= mShell.getArgs().size())
            return new XVariable(name, mShell.getArgs().get(n - 1));
          else
        	   return null ;
       }
		return mVars.get(name);
	}

	public void	setVar( XVariable var)
	{
		mVars.put(var );
	}

  public void setLocalVar( XVariable var)
  {
    mVars.putLocal(var );
  }


  

	public void setIndexedVar( String name , XValue value, String ind  ) throws CoreException 
	{
	    XVariable var = mVars.get(name);
        if( var == null )
           var = new XVariable(name, XValue.newXValue( TypeFamily.XTYPE , new XValueMap()));
      else
          var = var.clone();
          
      var.setIndexedValue(value, ind);
            
	    setVar( var );
	}



	/*
	 * Append to a variable as a sequence 
	 */
	public void appendVar(String name, XValue xvalue ) throws InvalidArgumentException {


		XVariable var = mVars.get(name);
		if( var == null ){
			// If no existing variable then dont touch
			setVar(new XVariable(name, xvalue) );
			return ;
		}

		var = var.clone();
		xvalue = var.getValue().append(xvalue);
		var.setValue(  xvalue  );
		setVar( var);

	}
	

  public void setLocalVar(String name, XValue value)
  {
    
    XVariable var = mVars.get(name);
    if( var == null )
      var = new XVariable(name, value);
    else
      var = var.newValue( value );
    setLocalVar( var );
    
  }
  
  public XVariable exportVar( String name ){
    XVariable var = mVars.get(name);
    if( var == null ){
      var = XVariable.newInstance(name);
      var.unset();
    }
    var.setFlag(EXPORT);
    mVars.put(var);
    return var;
  }


  public void setVar( String name , XValue value) throws InvalidArgumentException 
  {

    XVariable var = mVars.get(name);
    if( var == null )
      var = new XVariable(name, value);
    else
      var = var.newValue( value );
    setVar( var );
  }


	@Override
	public XEnvironment clone()
	{

		// TODO When cloning, only export marked for export vars
		// Add typeset command


		try {
			return clone( mShell );
		} catch (IOException e) {
			mShell.printErr("Exception cloning shell", e);
			return null;
		}
	}
	/*
	 * Clone an environment for use in a new thread
	 * 
	 * @see java.lang.Object#clone()
	 */

	public XEnvironment clone(Shell shell) throws IOException
	{
		
		mLogger.entry(shell);
		XEnvironment 	that = new XEnvironment(shell, mStaticContext.clone() , false);
		that.mVars		= new Variables(this.mVars);
		that.mIO = new XIOEnvironment(this.getIO());
		
		return mLogger.exit( that);
	}


	@Override
	public void close() throws IOException  {
		mLogger.entry(bClosed);
		if( bClosed ){
			mLogger.warn("Multiple close");
			
			mLogger.exit();
		}
		
		if( this.mSavedIO != null && ! mSavedIO.isEmpty())
			throw new IOException("Closing XEnvironment when mSavedIO is not empty");

		getIO().release();
		
		// close modules
 
		if( mModuleStack != null && ! mModuleStack.empty()){
			// Should be 1 module on stack during close 
			if( mModuleStack.size() > 1 )
				mLogger.error("Module stack is not empty {} " , mModuleStack.size() );
			
			while( !mModuleStack.isEmpty() )
			    mModuleStack.pop().release();
			mModuleStack = null ;
		}
		bClosed = true ;
				
	}

	public Shell getShell() { 
		return mShell;
	}

	public Variables getVars() { return mVars ; }


	public Collection<String> getVarNames() {
		return mVars.getVarNames();
	}

	public String getVarString( String key )
	{
		XVariable var = getVar(key);
		if( var== null )
			return null ;

		return var.getValue().toString();
	}

	/*
	 * Save the environment by cloning it and pushing it to this
	 * and return the OLD environment
	 */
	public void saveIO() throws CoreException
	{
		if( mSavedIO == null )
			mSavedIO = new Stack<XIOEnvironment>();

		mSavedIO.push(getIO());
		mIO = new XIOEnvironment(getIO());

	}

	public void restoreIO()
	{
		getIO().release();
		mIO = mSavedIO.pop();
	}







	/**
	 * @param file
	 * @param append
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws CoreException 
	 * @see org.xmlsh.sh.shell.Shell#getOutputStream(java.lang.String, boolean)
	 */
	public OutputStream getOutputStream(String file, boolean append, SerializeOpts opts ) throws FileNotFoundException,
	IOException, CoreException {
		return mShell.getOutputStream(file, append, opts );
	}

	public OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException {
		return mShell.getOutputStream(file, append);
	}
	/**
	 * @param s
	 * @param e
	 * @see org.xmlsh.sh.shell.Shell#printErr(java.lang.String, java.lang.Exception)
	 */
	public void printErr(String s, Exception e) {
		mShell.printErr(s, e);
	}


	/**
	 * @param s
	 * @see org.xmlsh.sh.shell.Shell#printErr(java.lang.String)
	 */
	public void printErr(String s) {
		mShell.printErr(s);
	}


	/**
	 * @return
	 * @see org.xmlsh.sh.shell.Shell#getCurdir()
	 */
	public File getCurdir() {
		return mShell.getCurdir();
	}


	/**
	 * @param cd
	 * @throws IOException 
	 * @see org.xmlsh.sh.shell.Shell#setCurdir(java.io.File)
	 */
	public void setCurdir(File cd) throws IOException {
		mShell.setCurdir(cd);
	}


	public XValue getVarValue(String name) {
		XVariable var = getVar(name);
		if( var == null )
			return null ;
		else
			return var.getValue();
	}


	public void unsetVar(String name ) throws InvalidArgumentException {
		mVars.unset( name );
	}

	public boolean isStdinSystem() { return  getStdin().isSystem(); }
	public boolean isStdoutSystem() { return  getStdout().isSystem() ; }
	public boolean isStderrSystem() { return  getStderr().isSystem() ; }


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStderr()
	 */
	public OutputPort getStderr() {
		return getIO().getStderr();
	}


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStdin()
	 */
	public InputPort getStdin()  {
		return getIO().getStdin();
	}


	/**
	 * @return
	 * @throws IOException 
	 * @see org.xmlsh.core.XIOEnvironment#getStdout()
	 */
	public OutputPort getStdout()  {
		return getIO().getStdout();
	}


	/*
	 * Create or return an output port - managed by the autorelease pool
	 */
	public OutputPort getOutput( XValue port, boolean append ) throws IOException
	{


		if( port == null )
			return getStdout();
		if( port.isAtomic()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdout();

			OutputPort p = mShell.newOutputPort(name, append);
			addAutoRelease(p);
			return p;
		}
		else
		{
			OutputPort p = new VariableOutputPort(  new XVariable(null, port) );
			addAutoRelease(p);
			return p;
		}


	}

	public OutputPort getOutput( String port , boolean append) throws IOException
	{
		return getOutput( XValue.newXValue(port) , append );
	}


	public OutputPort getOutput( File file , boolean append ) throws IOException
	{

		return new FileOutputPort( file , append);



	}


	/**
	 * @param stderr
	 * @throws IOException
	 * @throws InvalidArgumentException 
	 * @see org.xmlsh.core.XIOEnvironment#setStderr(java.io.OutputStream)
	 */
	public void setStderr(OutputStream stderr) throws CoreException {
		getIO().setStderr(stderr);
	}
	public void setStderr(OutputPort stderr) throws CoreException {
		getIO().setStderr(stderr);
	}


	/**
	 * @param stdin
	 * @throws IOException
	 * @see org.xmlsh.core.XIOEnvironment#setStdin(java.io.InputStream)
	 */

	public void setStdin(InputStream in) throws IOException {
		setInput(null, in );
	}

	public void setStdin(XVariable variable) throws IOException, InvalidArgumentException {

		setInput( null , variable );
	}
	public void setStdin(InputPort in ) throws IOException {

		setInput( null , in );
	}

	public InputPort setInput(String name,InputStream in) throws IOException  {
		return getIO().setInput( name,new StreamInputPort(in,null));
	}

	public InputPort setInput(String name, XVariable variable) throws IOException, InvalidArgumentException {

		return getIO().setInput( name,new VariableInputPort(variable));
	}

	public InputPort setInput( String name , InputPort in ) throws IOException {

		return getIO().setInput( name  , in );
	}


	public void setStdout(OutputStream out) throws CoreException {
		setOutput( null ,  new StreamOutputPort(out));

	}
	public void setStdout(OutputPort  port) throws CoreException {
		setOutput( null , port );
	}

	public void setStdout(XVariable xvar) throws CoreException {
		setOutput( null ,  new VariableOutputPort(xvar));
	}

	public void setOutput(String name ,OutputStream out) throws CoreException {
		setOutput( name,new StreamOutputPort(out));

	}


	public void setOutput(String name ,XVariable xvar) throws CoreException {
	  assert( xvar != null );
		setOutput( name,new VariableOutputPort(xvar));
	}

	public void setOutput(String name , OutputPort out) throws CoreException {
		getIO().setOutput( name, out );
	}


	public void declareNamespace(String ns ) {

	  getNamespaces().declare( ns );

	}
	public void declareNamespace(String prefix, String uri) {
		
	  getNamespaces().declare(prefix, uri);
	}


	

	public Namespaces getNamespaces() {
		return mStaticContext.getNamespaces();
	}


	public InputStream getInputStream(XValue file,SerializeOpts opts) throws IOException, InvalidArgumentException, CoreException{
		return getInput(file).asInputStream(opts);
	}



	public Source getSource(XValue value,SerializeOpts opts) throws  IOException, InvalidArgumentException, CoreException {
		return getInput(value).asSource(opts);

	}




	/*
	 * Get an input by name or value
	 * 
	 * If port is null return stdin
	 * If port is a string 
	 * If port is a string 
	 * 	  if equals to "-" return stdin
	 *    if looks like "scheme://path" return a port based on an input stream from UI
	 *    if looks like "name" return a port based on an input stream by filename
	 * if port is a node return an anonymous port based on a value
	 * 
	 */
	public InputPort getInput(XValue port) throws  IOException, InvalidArgumentException  {

		if( port == null )
			return getStdin();
		if( port.isAtomic()){
			String name = port.toString().trim();
			if( name.equals("-"))
				return getStdin();


			InputPort p = mShell.newInputPort(name);
			// Port is not managed, add to autorelease
			addAutoRelease( p );
			return p;


		}
		else
		{
			VariableInputPort p = new VariableInputPort(  new XVariable(null, port) );
			// Port is not managed, add to autorelease
			addAutoRelease(p);
			return p;
		}

	}

	/*
	 * Get an input port explicitly by its name 
	 */
	public InputPort getInputPort(String name)
	{
		return getIO().getInputPort(name);
	}

	public InputPort getInput( String name ) throws CoreException, IOException
	{
		return getInput( XValue.newXValue(name));
	}

	public OutputPort getOutputPort(String name){
		return getIO().getOutputPort(name);
	}


	public String getAbsoluteURI(String sysid) throws URISyntaxException 
	{
		URI uri = new URI(sysid);
		if( uri.isAbsolute())
			return sysid ;

		URI absolute = getBaseURI().resolve(sysid );
		return absolute.toString();


	}


	public URI getBaseURI() {
		return getCurdir().toURI();
	}



	public OutputPort getOutputPort(String portname, boolean append) {
		// TODO: Add append mode to output ports 
		return getIO().getOutputPort(portname);
	}


	public InputSource getInputSource(XValue value, SerializeOpts opts) throws CoreException, FileNotFoundException, IOException {
		InputPort in = getInput(value);
		return in.asInputSource(opts);

	}


	public boolean isDefined(String name) {
		return mVars.containsKey(name);

	}


	public Variables pushLocalVars() {
		Variables current = mVars ;
		mVars = mVars.pushLocals();
		return current ;

	}


	public void popLocalVars(Variables vars) {
		mVars = vars ;

	}


	/**
	 * @return the savedIO
	 */
	public XIOEnvironment getSavedIO() {
		return mSavedIO.peek();
	}




	// "1>&2" 
	public void dupOutput(String portLeft, String portRight ) throws IOException {
		getIO().dupOutput( portLeft , portRight );

	}


	public void dupInput(String portLeft, String portRight) throws IOException {
		getIO().dupInput( portLeft , portRight );

	}


	public static boolean isSpecialVarname( String name ) {
	     switch( name ) {
	     case "#" :
	     case "$" :
	     case "?" :
	     case "!" :
	     case "*" : 
	     case "@" :
	         return true ;
	      default:
	          return false ;
	      
	     }
	 }


	public StaticContext getStaticContext() {
		return mStaticContext ;
	}


	public Modules getModules(boolean bCreate ) {
		return mStaticContext.getModules(bCreate);
	}


	public ModuleHandle getModuleByPrefix(String prefix) {
		
		mLogger.entry(prefix);
		return mLogger.exit(mStaticContext.getModules(true).getExistingModuleByPrefix(prefix));
		
	}


	public FunctionDefinitions getFunctions(boolean bCreate) {
		return mStaticContext.getFunctions(bCreate);
	}



	public ModuleHandle pushModule(ModuleHandle module) {
		mLogger.entry(module , mModuleStack.size() );
		module.addRef();
		mModuleStack.push(module);
		return mLogger.exit(module) ;
	}


	public ModuleHandle popModule() throws IOException {
		mLogger.entry(mModuleStack.size());
		assert( !mModuleStack.isEmpty());
	    ModuleHandle mh = mModuleStack.pop();
		mh.release();
		return mLogger.exit(mh);
	}


	public StaticContext exportStaticContext() {
		return mStaticContext.clone();
		
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
