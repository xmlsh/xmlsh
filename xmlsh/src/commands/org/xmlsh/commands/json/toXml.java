/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.json;

import org.apache.commons.io.output.XmlStreamWriter;
import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XMLStreamWriterDelegate;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JSONUtils;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class toXml	extends BuiltinFunctionCommand {
	 
	
	public toXml()
		{
			super("to-xml");
		}
		
		@Override
		public XValue run(Shell shell, List<XValue> args) throws InvalidArgumentException, SaxonApiException, IOException, XMLStreamException {


			Processor proc = Shell.getProcessor();
			BuildingStreamWriter bw = proc.newDocumentBuilder().newBuildingStreamWriter();
			
			XMLStreamWriterDelegate xd = new XMLStreamWriterDelegate( bw );
			
			XmlMapper module = JSONUtils.getXmlMapper();

			bw.writeStartDocument();
				for( XValue arg : args ) {
					module.writeValue(xd, arg.asObject());
				}

			bw.writeEndDocument();
			
			return new XValue(bw.getDocumentNode());
			
		}

	}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */