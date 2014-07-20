/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.util.NameValue;
import org.xmlsh.util.NameValueList;
import org.xmlsh.util.PipedPort;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


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
	private volatile NameValueList<PipedPort,NameValue<PipedPort>>        mPipes;
	
	public	InputPort getStdin() 
	{
		InputPort stdin = mInputs.getPort(kSTDIN);
		if( stdin == null )
			return null;
			
		return stdin;
	}
	
	/*
	 * Stdandard output stream - created on first request
	 */
	public	OutputPort	getStdout() 
	{
		OutputPort stdout = mOutputs.getPort( kSTDOUT );
		if( stdout == null )
			return null;
		return stdout ;
	}
	
	/*
	 * Standard error stream - created on first request
	 */
	public	OutputPort	getStderr() 
	{
		OutputPort stderr = mOutputs.getPort(kSTDERR);
		if( stderr == null )
			return null;
		return stderr ;
	}


	public InputPort setInput(String name, InputPort port) throws CoreException {
		InputPort in;
		
		
		if( name == null || name.equals(kSTDIN) ){
			name = kSTDIN ;
		}
		
		in	= removeInput(name);

		
		if( in != null )
			in.release();
		
		addInput(name, port);
		return port ;
		
	}

	private void addInput(String name, InputPort port) {
		mInputs.add( new NamedPort<InputPort>( name  , port  ));
	}

	private InputPort removeInput(String name) {
		return mInputs.removePort(name);
	}
		
	
	



	public void setOutput(String name , OutputPort port) throws CoreException {
		OutputPort out ;
		if( name == null )
			name = kSTDOUT ;
		
		out = removeOutput(name);

		if (out != null) 
			out.release();
		
		addOutput(name, port);
	}

	private void addOutput(String name, OutputPort port) {
		synchronized( mOutputs ){
		  mOutputs.add(new NamedPort<OutputPort>(name , port));
		}
	}

	private OutputPort removeOutput(String name) {
		synchronized (mOutputs) {
			return mOutputs.removePort(name);
		}
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
		OutputPort stderr = mOutputs.removePort(kSTDERR);
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
	
	
	
	public XIOEnvironment() {

		mInputs = new PortList<InputPort>();
		mOutputs = new PortList<OutputPort>();
	}
	public XIOEnvironment( XIOEnvironment that )
	{
		mInputs = new PortList<InputPort>( that.mInputs );
		mOutputs = new PortList<OutputPort>( that.mOutputs);
	}
	
	public boolean isSystemIn( InputPort ip ) {
		NamedPort<InputPort> np = mInputs.findValue(ip);
		return np != null && np.getSystem();
	}
	public boolean isSystemOut( OutputPort ip ) {
		NamedPort<OutputPort> np = mOutputs.findValue( ip );
		return np != null && np.getSystem();
	}


	public void initStdio()  
	{

		mInputs.add( 
				new NamedPort<InputPort>( kSTDIN ,  new StreamInputPort(System.in,null) , true )
		);

		mOutputs.add( 
				new NamedPort<OutputPort>( kSTDOUT, new StreamOutputPort(System.out,false) , true  )
		);


		mOutputs.add( 
				new NamedPort<OutputPort>( kSTDERR ,  new StreamOutputPort(System.err,false) , true )
		);

	}

	/* return a named input port 
	 * 
	 */
	public	InputPort	getInputPort( String name )
	{
		return mInputs.getPort(name);
	}
	
	/* return a named output port 
	 * 
	 */
	public	OutputPort	getOutputPort( String name )
	{
		return mOutputs.getPort(name);
	}
	
	protected PortList<InputPort>	getInputPorts()
	{
		return mInputs;
	}
	protected PortList<OutputPort>	getOutputPorts()
	{
		return mOutputs;
	}

	public void newPipe(String name, PipedPort pipe ) throws CoreException, IOException {

		setInput(name, pipe.getInput());
		setOutput(name, pipe.getOutput());
		if( mPipes == null ){
			synchronized(this) {
				if( mPipes ==  null )
					mPipes = new NameValueList<>();
			}
		}
		synchronized( mPipes ){
			mPipes.add( new NameValue<PipedPort>(name,pipe) );
		}
	}

	public PipedPort getPipe(String name) {
		if( mPipes == null )
			return null;
		synchronized( mPipes ){
			NameValue<PipedPort> nv = mPipes.findName(name);
			return nv == null ? null : nv.getValue();
		}
	}
	
	public void closePipe( String name )
	{
		if( mPipes != null ){
			NameValue<PipedPort> nv = null;
			PipedPort pipe;
			synchronized( mPipes ){
				 nv = mPipes.removeName( name );
				 pipe = nv == null ? null : nv.getValue();
			}
			if( pipe != null ){
				pipe.close();
				removeInput( name );
				removeOutput( name );
			}
			
		}
		
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
