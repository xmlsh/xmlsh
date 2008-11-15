/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands;




import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class xcmp extends XCommand {
	
	private boolean xopt = false;
	private boolean bopt = false ;	// blank (CR/LF) insensitive

	

	
	public int run( List<XValue> args   )	throws Exception
	{
		

		Options opts = new Options( "x,b" , args );
		opts.parse();
		
		

		if( opts.getOpt("x") != null )
			xopt = true ;
		if( opts.getOpt("b") != null )
			bopt = true ;
		

		List<XValue> argv;
		if( (argv=opts.getRemainingArgs()).size() != 2 )
			return usage();
		
		
		File 	f1 = getFile( argv.get(0));
		File 	f2 =  getFile( argv.get(1));
		
		if( ! f1.exists() || ! f1.canRead()){
			printErr("Cannot open or read: " + f1.getName() );
			return 1;
		}
		if( ! f2.exists() || ! f2.canRead()){
			printErr("Cannot open or read: " + f2.getName() );
			return 1;
		}
			
		if( xopt )
			return xml_cmp(  f1 , f2 );
		else
			return bin_cmp(  f1, f2 );
			
		


	}
	
	private int readByte( InputStream is ) throws IOException
	{
		int c = is.read();
		if( c == '\r' && bopt )
			c = is.read();
		return c;
		
	}




	private int bin_cmp(File f1, File f2) throws IOException {
		BufferedInputStream is1 = new BufferedInputStream(new FileInputStream(f1));
	
		
		BufferedInputStream is2 = null;
		try {
			is2 = new BufferedInputStream(new FileInputStream(f2));
			
			int b = 0;
			int c;
			while( (c=readByte(is1)) >= 0  ){
				int c2 = readByte(is2);
				if( c != c2 ){
			
					printErr("Differs at byte: " + b);
				
					return 1;
				}
				b++;
			}
			
			if( is2.read() != -1 ){
				printErr("Differs at byte: " + b);
				return 2;
			}
			return 0;
					
					
		} finally {
			is1.close();
			if( is2 != null )
				is2.close();
		}
	
	}




	private int xml_cmp(File f1, File f2) throws FileNotFoundException, XMLStreamException  {
		
		XMLInputFactory inputFactory=XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.valueOf(true));
		
		InputStream is1 =new FileInputStream(f1);
		XMLEventReader  xmlreader1  =inputFactory.createXMLEventReader(is1);
		

		InputStream is2 =new FileInputStream(f2);
		XMLEventReader  xmlreader2  =inputFactory.createXMLEventReader(is2);
		
		try {
			
			
			while( xmlreader1.hasNext() && xmlreader2.hasNext()   ){
				
				XMLEvent e1 = xmlreader1.nextEvent();
				XMLEvent e2 = xmlreader2.nextEvent();

				
				if( ! isEqual( e1 , e2 )){
					Location loc = e1.getLocation();
					printErr("xml diff at file1: " + loc.toString() );
					
					
					return 2;
					
				}
			}
			return ((! xmlreader1.hasNext()) && (! xmlreader2.hasNext())) ? 0 : 1 ;
		} finally {
			try {
				xmlreader1.close();
			} finally {} 
			try {
				xmlreader2.close();
			} finally {} 
		}
	}

	private boolean isEqual(StartElement e1, StartElement e2)
	{
		if( e1 == e2 )
			return true ;
		
		if( ! isEqual( e1.getName() , e2.getName() ))
			return false ;
		
		if( ! compareAttributes( e1.getAttributes() , e2.getAttributes() ) )
			return false ;
		
		// Do not isEqual namespace declarations for now, it is not relevent 
		// namespaces are propogated into the attribute and element QName
		return true ;
		
		
	}
	

	
	@SuppressWarnings("unchecked")
	private boolean compareAttributes(Iterator attributes1, Iterator attributes2) {
		/*
		 * Compare attributes in no order
		 */
		
		/*
		 * Put the attributes1 into a map by qname
		 * For each element in attributes2 isEqual against the set and remove
		 * If there are any left in the set then non-equal otherwise equal
		 */
		
		HashMap<QName, Attribute> set = new HashMap<QName,Attribute>();
		while( attributes1.hasNext()){
			Attribute attr = (Attribute) attributes1.next();
			
			set.put( attr.getName() , attr );
		}
		while( attributes2.hasNext()){
			Attribute attr2 = (Attribute) attributes2.next();
			Attribute attr1 = set.remove( attr2.getName());
			if( attr1 == null )
				return false ;
			if( ! isEqual( attr1 , attr2 ))
				return false ;
				
		}
		
		return set.isEmpty();
		
		
		
	}




	private boolean isEqual(QName name1, QName name2) {
		return name1.equals(name2);
	}




	private boolean isEqual(EndElement e1, EndElement e2)
	{
		return isEqual( e1.getName() , e2.getName() );
	}
	
	private boolean isEqual(Characters e1, Characters e2)
	{
		return isEqual(e1.getData(), e2.getData());
		
	}
	
	private boolean isEqual(String s1, String s2) 
	{
		// Compare strings considering null and "" equal
		return Util.isEqual(s1,s2);

	}




	private boolean isEqual(Attribute e1, Attribute e2)
	{
		return isEqual( e1.getName() , e2.getName() ) &&
				isEqual( e1.getValue() , e2.getValue() ) ;
		
	}
	private boolean isEqual(Namespace e1, Namespace e2)
	{
		// Namespaces are equal if their URI's are equal
		return isEqual( e1.getNamespaceURI() , e2.getNamespaceURI() );
	}
	private boolean isEqual(Comment e1, Comment e2)
	{
		return isEqual( e1.getText() , e2.getText());
	}
	private boolean isEqual(StartDocument e1, StartDocument e2)
	{
		return true ; // ignore document stuff
	}
	private boolean isEqual(EndDocument e1, EndDocument e2)
	{
		return true ;
	}
	
	private boolean isEqual(DTD e1, DTD e2)
	{
		return true ;
	}
	
	

	private boolean isEqual(XMLEvent e1, XMLEvent e2) {
		if( e1.getEventType() != e2.getEventType() )
			return false ;
		
		switch( e1.getEventType() )
		{
		case	XMLStreamConstants.START_ELEMENT:
			return isEqual( e1.asStartElement() , e2.asStartElement());
		case	XMLStreamConstants.END_ELEMENT:
			return isEqual( e1.asEndElement() , e2.asEndElement());
		
		case	XMLStreamConstants.CHARACTERS:
			return isEqual( e1.asCharacters() , e2.asCharacters() );
		
		case	XMLStreamConstants.ATTRIBUTE:
			return	isEqual( (Attribute) e1 , (Attribute) e2 );
		
			
		case	XMLStreamConstants.NAMESPACE:
			return	isEqual( (Namespace) e1 , (Namespace) e2  );
			
		case	XMLStreamConstants.PROCESSING_INSTRUCTION:
			return isEqual( (ProcessingInstruction) e1 , (ProcessingInstruction) e2);
		
		case	XMLStreamConstants.COMMENT:
			return isEqual( (Comment) e1 , (Comment) e2 );
		
		case	XMLStreamConstants.START_DOCUMENT:
			return isEqual( (StartDocument) e1 , (StartDocument) e2 );
		
		case	XMLStreamConstants.END_DOCUMENT:
			return isEqual( (EndDocument)e1 , (EndDocument) e2 );
		
		case	XMLStreamConstants.DTD:
			return isEqual( (DTD) e1 , (DTD) e2 );
		
		default :
			return false ;
		
		}
		
		
		
		
	}




	private int usage(  ) {
		printErr("Usage: xcmp [-x] [-b] file1 file2");
		return 1;
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
