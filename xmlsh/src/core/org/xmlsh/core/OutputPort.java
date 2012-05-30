/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */




public abstract class OutputPort extends IPort
{

	public	abstract OutputStream asOutputStream(SerializeOpts opts);

	public abstract void flush() throws  CoreException, SaxonApiException;
	
	
	
	public abstract void close() throws CoreException;
	

	
	
	public synchronized PrintStream asPrintStream(SerializeOpts opts) 
	{
		return new PrintStream(asOutputStream(opts));
	}

	public abstract Destination asDestination(SerializeOpts opts) throws CoreException;
	

	public synchronized PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException {
		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream(opts) , opts.getOutputTextEncoding() ));
	}

	
	// These 2 shouldnt really go on the port 
	public abstract void writeSequenceSeperator(SerializeOpts serializeOpts) throws IOException, InvalidArgumentException, CoreException, SaxonApiException;
	public abstract void writeSequenceTerminator(SerializeOpts serializeOpts) throws IOException, CoreException, SaxonApiException ;

	
	
	public abstract XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws InvalidArgumentException, XMLStreamException, SaxonApiException;
	public abstract XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException, XMLStreamException, SaxonApiException;
	
	public abstract	IXdmItemOutputStream	asXdmItemOutputStream(SerializeOpts opts) throws CoreException;
	
	public abstract	ContentHandler			asContentHandler( SerializeOpts opts) throws XPathException, SaxonApiException;
	
	public	boolean		isFile() { return false ; }
	
	public File			getFile() throws UnimplementedException
	{
		throw new UnimplementedException("OutputPort.getFile() is not implmented() in class: " + this.getClass().getName() );
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
