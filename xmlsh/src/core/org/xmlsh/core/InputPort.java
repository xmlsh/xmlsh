/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlsh.sh.shell.SerializeOpts;

/*
 * An InputPort represents an input source of data, either Stream (bytes) or XML data
 * 
 */


public abstract class InputPort  extends IPort
{
	


	

	public	abstract InputStream asInputStream(SerializeOpts opts) throws InvalidArgumentException, SaxonApiException, IOException ;
	

	public abstract void close() throws IOException ;
	
	
	public abstract Source asSource(SerializeOpts opts) throws InvalidArgumentException, SaxonApiException, IOException;
	

	public abstract XdmNode asXdmNode(SerializeOpts opts) throws SaxonApiException, InvalidArgumentException, IOException;
	


	
	public abstract Document asDocument(SerializeOpts opts) throws ParserConfigurationException, SAXException, IOException, InvalidArgumentException, SaxonApiException;
	


	public abstract boolean isStream();

	public  abstract void copyTo(OutputStream out, SerializeOpts opts ) throws IOException, SaxonApiException, InvalidArgumentException;
	
	
	
	
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
