/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.TransformerHandler;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.S9Util;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.tinytree.TinyBuilder;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.SynchronizedOutputStream;
import org.xmlsh.util.Util;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public class OutputPort implements IPort
{
	private static byte kNEWLINE_BYTES[] = { '\n' };
	
	// OutputPort can be to a stream or a variable
	private	 XVariable		 mVariable;
	private OutputStream	 mStream;
	
	// Transient classes 
	private		XdmDestination	 		mXdmDestination;
	private		ByteArrayOutputStream 	mByteArrayOutputStream;
	private		Builder					mBuilder;
	
	private int mRef = 1;
	
	public OutputPort( OutputStream os ) 
	{
		mStream = os;
	}

	public OutputPort( XVariable variable )
	{
		mVariable = variable;
	}
	
	
	/*
	 * Standard input stream - created on first request
	 */
	
	public	synchronized OutputStream asOutputStream() 
	{
		/*
		 * If going to a variable, then create a variable stream
		 */
		if( mVariable != null )
			return ( mByteArrayOutputStream = new ByteArrayOutputStream()); 	// BOS is synchroized 
		else
			return new SynchronizedOutputStream(mStream,mStream != System.out);
	}
	
	
	
	public synchronized void release() throws IOException, InvalidArgumentException {
		
		if( mStream != null )
			mStream.flush();
		
		if( --mRef <= 0 ){
			if( mStream != null )
				mStream.close();
			if( mVariable != null && mXdmDestination != null )
				mVariable.setValue( new XValue(mXdmDestination.getXdmNode()) );
			
			if( mByteArrayOutputStream != null )
				mVariable.setValue( new XValue( mByteArrayOutputStream.toString(Shell.getTextEncoding())));
			if( mBuilder != null )
				mVariable.setValue( new XValue(S9Util.wrapNode(mBuilder.getCurrentRoot())));
		}

	
		
	}
	public synchronized void addRef() 
	{
		mRef++;
		
	}
	public synchronized TransformerHandler asTransformerHandler() throws TransformerConfigurationException, IllegalArgumentException, TransformerFactoryConfigurationError
	{
		if( mVariable != null ){
			
			mBuilder = new TinyBuilder();
	        PipelineConfiguration pipe = Shell.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration();
	        mBuilder.setPipelineConfiguration(pipe);
	            
			return Util.getTransformerHander(mBuilder);
			
		} else 
			return Util.getTransformerHander(asOutputStream());
	}
	
	public synchronized PrintStream asPrintStream()
	{
		return new PrintStream(asOutputStream());
	}

	public synchronized Destination asDestination()
	{
		if( mVariable != null ){
			 mXdmDestination = new XdmDestination();
			 Configuration config = Shell.getProcessor().getUnderlyingConfiguration();
			 try {
				Receiver r = mXdmDestination.getReceiver(config);
		        PipelineConfiguration pipe = config.makePipelineConfiguration();

				r.setPipelineConfiguration(pipe);
			} catch (SaxonApiException e) {
				;
			}
		
			 return mXdmDestination;
		}
		
		else
			return Util.streamToDestination(asOutputStream());
	}

	public synchronized PrintWriter asPrintWriter() throws UnsupportedEncodingException {
		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream() , 
						Shell.getTextEncoding() ));
	}
	
	
	public synchronized void writeSequenceSeperator() throws IOException
	{
		if( this.mXdmDestination == null )
			asOutputStream().write(kNEWLINE_BYTES  );
		
	}

	public void writeSequenceTerminator() throws IOException {
		if( this.mXdmDestination == null )
			asOutputStream().write(kNEWLINE_BYTES  );
		
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
