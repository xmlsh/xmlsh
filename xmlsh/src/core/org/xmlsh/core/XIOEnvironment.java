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


/*
 * IO Environment contains a named set of input and output ports.
 *  
 */



public class XIOEnvironment {
	private static Logger mLogger = LogManager.getLogger( XIOEnvironment.class );
	
	public static final String kSTDERR ="error";
	
	
	
	PortList<InputPort>		mInputs ;
	PortList<OutputPort>	mOutputs ;
	

	private	 boolean				 mStdinRedirected = false;

	public	InputPort getStdin() throws IOException 
	{
		InputPort stdin = mInputs.getDefault();
		if( stdin == null )
			return null;
			
		return stdin;
	}
	
	/*
	 * Stdandard output stream - created on first request
	 */
	public	OutputPort	getStdout() throws IOException 
	{
		OutputPort stdout = mOutputs.getDefault();
		if( stdout == null )
			return null;
		return stdout ;
	}
	
	/*
	 * Standard error stream - created on first request
	 */
	public	OutputPort	getStderr() throws IOException 
	{
		OutputPort stderr = mOutputs.get(kSTDERR);
		if( stderr == null )
			return null;
		return stderr ;
	}

	/**
	 * @param stdin the stdin to set
	 * @throws IOException 
	 */
	public void setStdin(InputStream in) throws IOException {
		mStdinRedirected = true ;
		InputPort stdin = mInputs.getDefault();

		
		if( stdin != null ){
			mInputs.removePort( stdin );
			stdin.release();
		}
			
		stdin = new InputPort(in);
		mInputs.add( new NamedPort<InputPort>( null , true , stdin ));
	}

	/**
	 * @param stdout the stdout to set
	 * @throws IOException 
	 */
	public void setStdout(OutputStream out) throws IOException {
		
		OutputPort stdout = mOutputs.getDefault();

		
		if( stdout != null ){
			mOutputs.removePort( stdout );
			stdout.release();
		}
			
		
		stdout = new OutputPort(out);
		mOutputs.add(new NamedPort<OutputPort>(null,true,stdout));
		
	}

	/**
	 * @param stderr the stderr to set
	 * @throws IOException 
	 */
	public void setStderr(OutputStream err) throws IOException {
		OutputPort stderr = mOutputs.get(kSTDERR);

		
		if( stderr != null ){
			mOutputs.removePort( stderr );
			stderr.release();
		}
			
		
		stderr = new OutputPort(err);
		mOutputs.add(new NamedPort<OutputPort>(kSTDERR,false,stderr));

	}


	public void release() {
		try {
			mInputs.close();
			mOutputs.close();
			mInputs.clear();
			mOutputs.clear();
			
		} catch (IOException e) {
			mLogger.error("Exception closing environment",e);
		}
	}
	
	
	public boolean isStdinRedirected() { 
		return mStdinRedirected ; 
		}
	
	public XIOEnvironment() {

		
		mInputs = new PortList<InputPort>();
		mOutputs = new PortList<OutputPort>();
		
		
		
	}
	public XIOEnvironment( XIOEnvironment that )
	{
		mInputs = new PortList<InputPort>( that.mInputs );
		mOutputs = new PortList<OutputPort>( that.mOutputs);
		mStdinRedirected = that.mStdinRedirected;
	}
	
	

	public void initStdio() throws IOException {

		mInputs.add( 
				new NamedPort<InputPort>( null , true , new InputPort(System.in) )
		);

		mOutputs.add( 
				new NamedPort<OutputPort>( null , true , new OutputPort(System.out) )
		);


		mOutputs.add( 
				new NamedPort<OutputPort>( kSTDERR , false , new OutputPort(System.err) )
		);

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
