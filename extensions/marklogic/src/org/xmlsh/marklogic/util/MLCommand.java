/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.SerializeOpts;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.exceptions.XccConfigException;
import com.marklogic.xcc.types.ItemType;

public abstract class MLCommand extends XCommand {

	public MLCommand() {
		super();
	}

	protected ContentSource getConnection(Options opts) throws URISyntaxException, XccConfigException,
			InvalidArgumentException {
				XValue vc = null;
				String connect;
				OptionValue ov = opts.getOpt("c");
				if( ov != null )
					vc = ov.getValue();
				else
					vc = getEnv().getVarValue("MLCONNECT");
				if( vc == null){
					throw new InvalidArgumentException("No connection");
				}
				connect = vc.toString();
			
				URI serverUri = new URI (connect);
				ContentSource cs = ContentSourceFactory.newContentSource (serverUri);
				return cs;
			}



	protected void writeResult(ResultSequence rs, OutputPort out, boolean asText) throws FactoryConfigurationError,
			IOException, InvalidArgumentException, XMLStreamException {
				SerializeOpts sopts = getSerializeOpts();
			
			    XMLInputFactory factory = XMLInputFactory.newInstance();
			    
			    
			    while (rs.hasNext()) {
			        ResultItem rsItem = rs.next();
			        
			          
			        ItemType type = rsItem.getItemType();
			        if( asText || type.isAtomic() ){
			        	OutputStream os = out.asOutputStream();
			        	rsItem.writeTo(os);
			        	os.close();
			        } else {
			        	InputStream isItem = rsItem.asInputStream();
			                
			    		XMLEventWriter writer =  out.asXMLEventWriter(sopts);
			
			        	XMLEventReader xmlItem = factory.createXMLEventReader(isItem);
			        
			        	writer.add( xmlItem );
			            writer.close();
			        	xmlItem.close();
			        }
			        
			      out.writeSequenceSeperator();
			    }
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