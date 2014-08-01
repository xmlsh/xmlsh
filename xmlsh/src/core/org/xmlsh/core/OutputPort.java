/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;

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

/*
 * An OutputPort represents an output sync of data, either Stream (bytes) or XML data
 * 
 */



public abstract class OutputPort extends AbstractPort implements IOutputPort 
{

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public	abstract OutputStream asOutputStream(SerializeOpts opts) throws CoreException;


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asPrintStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public synchronized PrintStream asPrintStream(SerializeOpts opts) throws CoreException 
	{
		return new PrintStream(asOutputStream(opts));
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asDestination(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract Destination asDestination(SerializeOpts opts) throws CoreException;


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asPrintWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public synchronized PrintWriter asPrintWriter(SerializeOpts opts) throws UnsupportedEncodingException, CoreException {
		return new PrintWriter( 		
				new OutputStreamWriter(asOutputStream(opts) , opts.getOutputTextEncoding() ));
	}


	// These 2 shouldnt really go on the port 
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#writeSequenceSeperator(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract void writeSequenceSeperator(SerializeOpts serializeOpts) throws IOException, InvalidArgumentException, CoreException, SaxonApiException;
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#writeSequenceTerminator(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract void writeSequenceTerminator(SerializeOpts serializeOpts) throws IOException, CoreException, SaxonApiException ;



	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asXMLStreamWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract XMLStreamWriter asXMLStreamWriter(SerializeOpts opts) throws InvalidArgumentException, XMLStreamException, SaxonApiException, CoreException;
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asXMLEventWriter(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract XMLEventWriter asXMLEventWriter(SerializeOpts opts) throws InvalidArgumentException, XMLStreamException, SaxonApiException, IOException, CoreException;

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asXdmItemOutputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract	IXdmItemOutputStream	asXdmItemOutputStream(SerializeOpts opts) throws CoreException;

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#asContentHandler(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract	ContentHandler			asContentHandler( SerializeOpts opts) throws XPathException, SaxonApiException, CoreException;

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#isFile()
	 */
	@Override
	public	boolean		isFile() { return false ; }

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#getFile()
	 */
	@Override
	public File			getFile() throws UnimplementedException
	{
		throw new UnimplementedException("OutputPort.getFile() is not implmented() in class: " + this.getClass().getName() );
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IOutputPort#isNull()
	 */
	@Override
	public boolean isNull()
	{
		// TODO Auto-generated method stub
		return false;
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
