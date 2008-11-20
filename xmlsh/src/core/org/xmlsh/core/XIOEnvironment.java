/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.util.SynchronizedInputStream;
import org.xmlsh.util.SynchronizedOutputStream;


/*
 * IO Environment contains a named set of input and output ports.
 *  
 */



public class XIOEnvironment {
	private static Logger mLogger = LogManager.getLogger( XIOEnvironment.class );
	
	/**
	 * @TODO: convert to using InputPort and OutputPort
	 */
	
	
	/*
	 * Standard IO
	 */
	
	private SynchronizedInputStream	 mStdin;
	private	 boolean				 mStdinRedirected = false;
	private SynchronizedOutputStream mStdout;
	private SynchronizedOutputStream mStderr;
	
	

	/*
	 * Standard input stream - created on first request
	 */
	
	public	InputStream getStdin() 
	{
		if( mStdin == null )
			mStdin = new SynchronizedInputStream(System.in);
		return mStdin;
	}
	
	/*
	 * Stdandard output stream - created on first request
	 */
	public	OutputStream	getStdout() 
	{
		if( mStdout == null )
			mStdout =  new SynchronizedOutputStream(System.out);
		return mStdout ;
	}
	
	/*
	 * Standard error stream - created on first request
	 */
	public	OutputStream	getStderr() 
	{
		if( mStderr == null )
			mStderr = new SynchronizedOutputStream(System.err);
		return mStderr ;
	}

	/**
	 * @param stdin the stdin to set
	 * @throws IOException 
	 */
	public void setStdin(InputStream stdin) throws IOException {
		mStdinRedirected = true ;
		if( mStdin != null )
			mStdin.close();
		mStdin = new SynchronizedInputStream(stdin);
	}

	/**
	 * @param stdout the stdout to set
	 * @throws IOException 
	 */
	public void setStdout(OutputStream stdout) throws IOException {
		if( mStdout != null )
			mStdout.close();
		mStdout = new SynchronizedOutputStream(stdout);
	}

	/**
	 * @param stderr the stderr to set
	 * @throws IOException 
	 */
	public void setStderr(OutputStream stderr) throws IOException {
		if( mStderr != null )
			mStderr.close();
		mStderr = new SynchronizedOutputStream(stderr);
	}


	public void close() {
		try {
			if( this.mStdout != null)
				this.mStdout.close();
			
			if( this.mStderr != null )
				this.mStderr.close();
			
			if( this.mStdin != null )
				this.mStdin.close();
			
			this.mStderr = null;
			this.mStdout = null;
			this.mStdin = null;
			
		} catch (IOException e) {
			mLogger.error("Exception closing environment",e);
		}
	}
	
	
	public boolean isStdinRedirected() { return mStdinRedirected ; }
	
	public XIOEnvironment clone()
	{
		XIOEnvironment that = new XIOEnvironment();
		
		
		// Copy streams, assume they are thread safe streams
		that.mStderr = this.mStderr;
		if( that.mStderr != null )
			that.mStderr.addRef();
		
		
		that.mStdin  = this.mStdin;
		if( that.mStdin != null ){
			that.mStdin.addRef();
			that.mStdinRedirected = this.mStdinRedirected;
		}
		
		
		that.mStdout = this.mStdout;
		if( that.mStdout != null )
			that.mStdout.addRef();
		return that;
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
