/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.types;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class XFile implements XSerializable {
	private File	mFile;
	
	
	public XFile( String path )
	{
		this( new File(path));
	}
	
	public XFile( File file )
	{
		mFile = file;
	}
	
	public String getName()
	{
		return mFile.getName();
		
	}

	public String getPath()
	{
		try {
			return mFile.getCanonicalPath();
		} catch (IOException e) {
			return "";
		}
	}
	
	public void serialize(TransformerHandler writer) throws SAXException {
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "name", "name", "CDATA", getName());
		atts.addAttribute("", "path", "path", "CDATA", getPath());

		writer.startElement("", "file", "file", atts);
		writer.endElement("", "file", "file");
		
	}
	
	/*
	
	public void serialize( XMLStreamWriter writer ) throws XMLStreamException
	{
		writer.writeStartElement("file");
		writer.writeAttribute("name", name);
		writer.writeAttribute("path", path);
		writer.writeEndElement();
	
	}
	*/

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
