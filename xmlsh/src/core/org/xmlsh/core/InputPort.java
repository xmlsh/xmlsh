/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.xml.sax.InputSource;
import org.xmlsh.core.io.AbstractPort;
import org.xmlsh.core.io.IInputPort;
import org.xmlsh.core.io.IXdmItemInputStream;
import org.xmlsh.core.io.ValueXdmItemInputStream;
import org.xmlsh.sh.shell.SerializeOpts;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML data
 * 
 */


public abstract class InputPort extends AbstractPort implements IInputPort
{
	/*
     * An InputPort represents an input source of data, either Stream (bytes) or XML
     * data
     * 
     */
    
    


    /* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asInputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public	abstract InputStream asInputStream(SerializeOpts opts) throws CoreException, IOException ;


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asSource(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract Source asSource(SerializeOpts opts) throws CoreException;
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asInputSource(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract InputSource	asInputSource(SerializeOpts opts) throws CoreException, IOException;


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asXdmNode(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract XdmNode asXdmNode(SerializeOpts opts) throws CoreException, IOException;

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#copyTo(java.io.OutputStream, org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public  abstract void copyTo(OutputStream out, SerializeOpts opts ) throws  CoreException, IOException;

	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asXMLEventReader(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException;
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asXMLStreamReader(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws  CoreException, IOException;


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asXdmItem(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException, IOException;


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asReader(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public Reader 	asReader( SerializeOpts serializeOpts ) throws CoreException, IOException {
		return new InputStreamReader( asInputStream(serializeOpts) , serializeOpts.getInputTextEncoding()); 
	}

	// Default implementation uses a singleton as the input stream
	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asXdmItemInputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public IXdmItemInputStream asXdmItemInputStream(SerializeOpts serializeOpts)
			throws CoreException, IOException {

		return new ValueXdmItemInputStream( asXdmItem( serializeOpts),serializeOpts);
	}


	/* (non-Javadoc)
	 * @see org.xmlsh.core.IInputPort#asJson(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public abstract JsonNode asJson(SerializeOpts serializeOpts) throws IOException, CoreException ;

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
