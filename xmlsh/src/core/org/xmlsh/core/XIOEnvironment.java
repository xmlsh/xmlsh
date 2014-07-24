/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.util.AutoReleasePool;
import org.xmlsh.util.INameValue;
import org.xmlsh.util.NameValue;
import org.xmlsh.util.NameValueList;
import org.xmlsh.util.PipedPort;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;


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
	private volatile NameValueList<PipedPort>        mPipes;
	private volatile AutoReleasePool  mAutoRelease = null;

	
	private <T>  T getPort(IHandle<T> hPort ){
		if( hPort == null )
			return null ;
		return hPort.get();
	}
	
	
	public	InputPort getStdin() 
	{
		return getPort( getInput(kSTDIN));
	}
	
	/*
	 * Stdandard output stream - created on first request
	 */
	public	OutputPort	getStdout() 
	{
		return getPort(mOutputs.getPort( kSTDOUT ));
	}
	
	/*
	 * Standard error stream - created on first request
	 */
	public	OutputPort	getStderr() 
	{
		 return getPort(mOutputs.getPort(kSTDERR));
	}


	public InputPort setInput(String name, InputPort port) throws IOException  {
		
		if( name == null || name.equals(kSTDIN) ){
			name = kSTDIN ;
		}
		
		IHandle<InputPort> in 	= removeInput(name);
		
		if( in != null )
			in.release();
		
		addInput(name, port);
		return port ;
		
	}

	private void addInput(String name, InputPort port) {
		synchronized(mInputs) {
		  mInputs.add( name , port );
		}
	}

	private IHandle<InputPort> removeInput(String name) {
		synchronized(mInputs) {
    		return mInputs.removePort(name);
		}
    }
		
	
	



	public void setOutput(String name , OutputPort port)  {
		IHandle<OutputPort> out ;
		if( name == null )
			name = kSTDOUT ;
		
		out = removeOutput(name);

		if (out != null) 
			Util.safeRelease(out);
		
		addOutput(name, port);
	}

	private void addOutput(String name, OutputPort port) {
		synchronized( mOutputs ){
		  mOutputs.add(name , port);
		}
	}

	private IHandle<OutputPort> removeOutput(String name) {
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
		IHandle<OutputPort> stderr = mOutputs.removePort(kSTDERR);
		if( stderr != null )
			Util.safeRelease(stderr);
		addOutput(kSTDERR, err);
	}

	public void release() {
		try {
			synchronized( mInputs ) {
			   mInputs.close();
			   mInputs.clear();
			}
			synchronized( mOutputs) {
			  mOutputs.close();
			  mOutputs.clear();
			}
			
			if( mAutoRelease != null ) {
				mAutoRelease.close();
				mAutoRelease = null ;
			}
			
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
		
		// Dont copy the AutoRelease pool ... 
		
	}
	

	public void initStdio()  
	{

		mInputs.add( 
				 kSTDIN ,  new StreamInputPort(System.in,null,true) 
		);

		mOutputs.add( 
				kSTDOUT, new StreamOutputPort(System.out,false,true)  
		);


		mOutputs.add( 
				kSTDERR ,  new StreamOutputPort(System.err,false,true) 
		);

	}

	/* return a named input port 
	 * 
	 */
	public	InputPort	getInputPort( String name )
	{
		IHandle<InputPort> hPort = getInput(name);
		return hPort == null ? null : hPort.get();
	}
	
	private IHandle<InputPort> getInput(String name) {
		synchronized( mInputs ) {
			  return mInputs.getPort(name);
			}
	}


	/* return a named output port 
	 * 
	 */
	public	OutputPort	getOutputPort( String name )
	{
		IHandle<OutputPort> hPort = getOutput(name);
		return hPort == null ? null : hPort.get();

	}


	private IHandle<OutputPort> getOutput(String name) {
		synchronized( mOutputs ) {
		  return mOutputs.getPort(name);
		}
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
			INameValue<PipedPort> nv = mPipes.findName(name);
			return nv == null ? null : nv.getValue();
		}
	}
	
	public void closePipe( String name )
	{
		if( mPipes != null ){
			INameValue<PipedPort> nv = null;
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

	//"1>&2"

	public void dupOutput(String portLeft, String portRight) throws IOException {
		IHandle<OutputPort> hLeft =  removeOutput(portLeft);
		IHandle<OutputPort> hRight = getOutput(portRight);
    	
		if( hLeft != null )
    		hLeft.release();
    	
	    if( hRight != null ) { // just clear left
	    	addOutput( portLeft , hRight.get() );
	    }
	    	
	}


	public void dupInput(String portLeft, String portRight) throws IOException {
		IHandle<InputPort> hLeft =  removeInput(portLeft);
		IHandle<InputPort> hRight = getInput(portRight);
    	
		if( hLeft != null )
    		hLeft.release();
    	
	    if( hRight != null ) { // just clear left
	    	addInput( portLeft , hRight.get() );
	    }		
	}


	public void addAutoRelease(AbstractPort obj) {
		if( mAutoRelease == null ) {
			synchronized( this ) {
				if( mAutoRelease == null ) 
				   mAutoRelease = new AutoReleasePool();
			}
		}
		mAutoRelease.add(obj.newReference());		
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
