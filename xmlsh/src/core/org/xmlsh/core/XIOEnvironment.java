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
	 * @param systemid 
	 * @param stdin the stdin to set
	 * @throws IOException 
	 */
	public void setStdin(InputStream in) throws CoreException {
		setStdin( new StreamInputPort(in));
	}

	public void setStdin(XVariable variable) throws CoreException {
		
		setStdin( new VariableInputPort(variable));
	}
	
	
	public void setStdin(InputPort port) throws CoreException {
		
		mStdinRedirected = true ;
		InputPort stdin = mInputs.getDefault();

		
		if( stdin != null ){
			mInputs.removePort( stdin );
			stdin.release();
		}

		mInputs.add( new NamedPort<InputPort>( null , true , port  ));
		
	}
		
	
	/**
	 * @param stdout the stdout to set
	 * @throws IOException 
	 * @throws InvalidArgumentException 
	 */
	public void setStdout(OutputStream out) throws CoreException {
		setStdout( new StreamOutputPort(out));
		
	}


	public void setStdout(XVariable xvar) throws CoreException {
		setStdout( new VariableOutputPort(xvar));
	}

	public void setStdout(OutputPort port) throws CoreException {
		OutputPort stdout = mOutputs.getDefault();

		if (stdout != null) {
			mOutputs.removePort(stdout);
			stdout.release();
		}

		mOutputs.add(new NamedPort<OutputPort>(null, true, port));
	}

	
	
	
	
	/**
	 * @param stderr the stderr to set
	 * @throws IOException 
	 * @throws InvalidArgumentException 
	 */
	public void setStderr(OutputStream err) throws CoreException {
		OutputPort stderr = mOutputs.get(kSTDERR);

		
		if( stderr != null ){
			mOutputs.removePort( stderr );
			stderr.release();
		}
			
		
		stderr = new StreamOutputPort(err);
		mOutputs.add(new NamedPort<OutputPort>(kSTDERR,false,stderr));

	}


	public void release() {
		try {
			mInputs.close();
			mOutputs.close();
			mInputs.clear();
			mOutputs.clear();
			
		} catch (Exception e) {
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
				new NamedPort<InputPort>( null , true , new StreamInputPort(System.in) )
		);

		mOutputs.add( 
				new NamedPort<OutputPort>( null , true , new StreamOutputPort(System.out) )
		);


		mOutputs.add( 
				new NamedPort<OutputPort>( kSTDERR , false , new StreamOutputPort(System.err) )
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
