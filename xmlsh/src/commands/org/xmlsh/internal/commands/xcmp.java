/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.internal.commands;




import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
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

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xcmp extends XCommand {

	private boolean xopt = false;
	private boolean bopt = false ;	// blank (CR/LF) insensitive
	private	 boolean nopt = false;		// dont print anything, just exit
	private boolean copt = false ; 	// Ignore comments
	private boolean popt = false ;


	@Override
	public int run( List<XValue> args   )	throws Exception
	{


		Options opts = new Options( "x,b,n,c,p"  );
		opts.parse(args);


		xopt = opts.hasOpt("x");
		bopt = opts.hasOpt("b");
		nopt = opts.hasOpt("n");
		copt = opts.hasOpt("c");
		popt = opts.hasOpt("p");


		List<XValue> argv;
		if( (argv=opts.getRemainingArgs()).size() != 2 ){
			usage();
			return -1;
		}



		InputPort 	i1 =  getInput( argv.get(0));
		InputPort 	i2 =  getInput( argv.get(1));


		if( xopt )
			return xml_cmp(  i1 , i2 );
		else
			return bin_cmp(  i1, i2 );
	}

	private int readByte( InputStream is ) throws IOException
	{
		int c = is.read();
		if( c == '\r' && bopt )
			c = is.read();
		return c;

	}




	private int bin_cmp(InputPort f1, InputPort f2) throws CoreException, IOException  {
		BufferedInputStream is1 = new BufferedInputStream(f1.asInputStream(getSerializeOpts()));


		BufferedInputStream is2 = null;
		try {
			is2 = new BufferedInputStream(f2.asInputStream(getSerializeOpts()));

			int b = 0;
			int c;
			while( (c=readByte(is1)) >= 0  ){
				int c2 = readByte(is2);
				if( c != c2 ){
					if( ! nopt )
						printErr("Differs at byte: " + b);

					return 1;
				}
				b++;
			}

			if( is2.read() != -1 ){
				if( ! nopt )
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




	private int xml_cmp(InputPort f1, InputPort f2) throws InvalidArgumentException, CoreException, XMLStreamException {

		/*
		XMLInputFactory inputFactory=XMLInputFactory.newInstance();


		inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.valueOf(true));
		inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.valueOf(false));
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.valueOf(false));
		 */


		SerializeOpts opts = getSerializeOpts().clone();
		opts.setSupports_dtd(false);

		XMLEventReader  xmlreader1  = f1.asXMLEventReader(opts);

		XMLEventReader  xmlreader2  = f2.asXMLEventReader(opts);

		try {


			while( xmlreader1.hasNext() && xmlreader2.hasNext()   ){

				XMLEvent e1 = xmlreader1.nextEvent();

				/*
				 * DAL: Note: skip START_DOCUMENT tags on either e1 or e2
				 * StAX only produdes START_DOCUMENT tags if there is an <?xml?> declaration
				 * which doesnt change the logical meaning of the document wrt to comparison
				 */
				if( e1.getEventType() == XMLStreamConstants.START_DOCUMENT && xmlreader1.hasNext() )
					e1 = xmlreader1.nextEvent();
				XMLEvent e2 = xmlreader2.nextEvent();
				if( e2.getEventType() == XMLStreamConstants.START_DOCUMENT && xmlreader2.hasNext() )
					e2 = xmlreader2.nextEvent();

				/*
				 * If ignoring blanks then entirely skip any element which is charactors and entirely blank
				 */
				if( bopt || copt || popt ){
					while(((bopt && (e1.getEventType() == XMLStreamConstants.CHARACTERS ) && isBlank(e1.asCharacters()) ) || 
							(copt && e1.getEventType() == XMLStreamConstants.COMMENT )  ||
							(popt && e1.getEventType() == XMLStreamConstants.PROCESSING_INSTRUCTION ) ) 
							&& xmlreader1.hasNext() )
						e1 = xmlreader1.nextEvent();

					while(((bopt && (e2.getEventType() == XMLStreamConstants.CHARACTERS ) && isBlank(e2.asCharacters()) ) || 
							(copt && e2.getEventType() == XMLStreamConstants.COMMENT ) ||
							(popt && e2.getEventType() == XMLStreamConstants.PROCESSING_INSTRUCTION ) )  
							&& xmlreader2.hasNext() )
						e2 = xmlreader2.nextEvent();


				}




				if( ! isEqual( e1 , e2 )){
					Location loc = e1.getLocation();
					if( ! nopt )
						printErr("xml diff at file1: " + loc.toString() );


					return 2;

				}
			}
			return ((! xmlreader1.hasNext()) && (! xmlreader2.hasNext())) ? 0 : 1 ;
		} 


		finally {
			try {
				xmlreader1.close();

			} finally {} 
			try {
				xmlreader2.close();

			} finally {} 
		}
	}

	private boolean isBlank(Characters e) {
		if( e.isIgnorableWhiteSpace() )
			return true ;
		return Util.isBlank(e.getData());



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
		if( bopt )
			return isEqual(e1.getData().trim(), e2.getData().trim());
		else
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

	private boolean isEqual(ProcessingInstruction e1 , ProcessingInstruction e2)
	{
		String t1 = e1.getTarget();
		String t2 = e2.getTarget();
		if( ! Util.isEqual(t1,t2))
			return false ;
		t1 = e1.getData();
		t2 = e2.getData();
		return Util.isEqual(t1, t2);
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
