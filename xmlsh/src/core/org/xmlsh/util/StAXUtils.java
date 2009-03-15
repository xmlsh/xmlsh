/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import javanet.staxutils.XMLEventStreamWriter;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.type.Type;

/*
 * TODO:saxon classes StaxToEventBridge , EventIterator
 */
public class StAXUtils {
	
	public static void	 copy( NodeInfo node , XMLStreamWriter writer ) throws XMLStreamException
	{
		switch( node.getNodeKind() ){
		case	Type.ELEMENT :
		{
			String prefix = node.getPrefix();
			String localName = node.getLocalPart();
			String namespaceURI = node.getURI();
			writer.writeStartElement(prefix, localName, namespaceURI);
			
			
			// Get declared namespaces
			int [] ns = node.getDeclaredNamespaces(null);
			if( ns != null ){
				for( int code : ns ){
					NamePool np = node.getNamePool();
					String ns_prefix = np.getPrefixFromNamespaceCode(code);
					String ns_uri = np.getURIFromNamespaceCode(code);
					
					writer.writeNamespace( ns_prefix , ns_uri );
				}
			}
				
			
						
			// Write attributes
			AxisIterator iter = node.iterateAxis(Axis.ATTRIBUTE);
			Item item;
			while( ( item = iter.next() ) != null ){
				NodeInfo attr = (NodeInfo) item;
				copy( attr , writer );
			}
			
	
			
			// Child nodes 
			iter = node.iterateAxis(Axis.DESCENDANT);
			while( ( item = iter.next() ) != null ){
				NodeInfo child = (NodeInfo) item ;
				copy( child , writer );
			}
			writer.writeEndElement();

			
		
		}
		break;
		
			
			
		
		case	Type.ATTRIBUTE :
			writer.writeAttribute( node.getPrefix(), node.getURI() , node.getLocalPart(), node.getStringValue());
			break;
		
		case	Type.TEXT :
		case	Type.WHITESPACE_TEXT :
			writer.writeCharacters(node.getStringValue());
			break;
		case	Type.PROCESSING_INSTRUCTION :
			
			writer.writeProcessingInstruction(node.getDisplayName(), node.getStringValue());
			break;
		case	Type.COMMENT :
			writer.writeComment(node.getStringValue());
			break;
		case	Type.DOCUMENT :
		{
			// writer.writeStartDocument();
			AxisIterator iter = node.iterateAxis(Axis.DESCENDANT);
			Item item;
			while( ( item = iter.next() ) != null ){
				NodeInfo child = (NodeInfo) item ;
				copy( child , writer );
			}
			// writer.writeEndDocument();
		}	
			break;
		case	Type.NAMESPACE:
		{
			String prefix = node.getDisplayName();
			if( Util.isBlank(prefix))
				break ;
			String uri = node.getStringValue();
			if( Util.isBlank(uri))
				break;
			writer.writeNamespace( prefix , uri );
		
		}
			break;
		
		
		}
		
		
		
	}

	public static void copy(NodeInfo node, XMLEventWriter writer) throws XMLStreamException {
		copy( node ,(XMLStreamWriter) new XMLEventStreamWriter( writer) );
		
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
