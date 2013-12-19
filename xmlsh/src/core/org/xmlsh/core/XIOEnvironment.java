/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
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
	public static final String kSTDIN 	 ="input";
	public static final String kSTDOUT ="output";
	
	
	
	
	private PortList<InputPort>		mInputs ;
	private PortList<OutputPort>	mOutputs ;
	

	private	 boolean				 mStdinRedirected = false;

	public	InputPort getStdin() 
	{
		InputPort stdin = mInputs.get( kSTDIN );
		if( stdin == null )
			return null;
			
		return stdin;
	}
	
	/*
	 * Stdandard output stream - created on first request
	 */
	public	OutputPort	getStdout() 
	{
		OutputPort stdout = mOutputs.get( kSTDOUT );
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


	public InputPort setInput(String name, InputPort port) throws CoreException {
		InputPort in;
		
		
		if( name == null || name.equals(kSTDIN) ){
			name = kSTDIN ;
			
			mStdinRedirected = true ;
		}
		
		in	= mInputs.removeNamed(name);

		
		if( in != null )
			in.release();
		
		mInputs.add( new NamedPort<InputPort>( name  , port  ));
		return port ;
		
	}
		
	
	



	public void setOutput(String name , OutputPort port) throws CoreException {
		OutputPort out ;
		if( name == null )
			name = kSTDOUT ;
		
		out = mOutputs.removeNamed(name);

		if (out != null) 
			out.release();
		

		mOutputs.add(new NamedPort<OutputPort>(name , port));
	}
	
	
	
	/**
	 * @param stderr the stderr to set
	 * @throws IOException 
	 * @throws InvalidArgumentException 
	 */
	public void setStderr(OutputStream err) throws CoreException {
		setStderr(new StreamOutputPort(err));

	}
	public void setStderr(OutputPort err) throws CoreException {
		OutputPort stderr = mOutputs.removeNamed(kSTDERR);

		
		if( stderr != null )
			
			stderr.release();
		
	
		mOutputs.add(new NamedPort<OutputPort>(kSTDERR,err));

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
	
	

	public void initStdio()  {

		mInputs.add( 
				new NamedPort<InputPort>( kSTDIN ,  new StreamInputPort(System.in,null) )
		);

		mOutputs.add( 
				new NamedPort<OutputPort>( kSTDOUT, new StreamOutputPort(System.out,false) )
		);


		mOutputs.add( 
				new NamedPort<OutputPort>( kSTDERR ,  new StreamOutputPort(System.err,false) )
		);

	}

	/* return a named input port 
	 * 
	 */
	public	InputPort	getInputPort( String name )
	{
		return mInputs.get(name);
	}
	
	/* return a named output port 
	 * 
	 */
	public	OutputPort	getOutputPort( String name )
	{
		return mOutputs.get(name);
	}
	
	protected PortList<InputPort>	getInputPorts()
	{
		return mInputs;
	}
	protected PortList<OutputPort>	getOutputPorts()
	{
		return mOutputs;
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
