/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;

public class xsdcomment extends XCommand {
	
	private static final String kSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
	private static final String kJXON_NS  = 	"http://www.xmlsh.org/jxon";
	
	private XMLEventFactory mEventFactory = XMLEventFactory.newInstance();
	private XMLInputFactory mInputFactory = XMLInputFactory.newInstance();

	

	public class NoDocXMLEventReader implements XMLEventReader {

		private		XMLEventReader	mReader ;
		
		
		
		public NoDocXMLEventReader(XMLEventReader reader) {
			super();
			mReader = reader;
		}



		/**
		 * @return
		 * @throws XMLStreamException
		 * @see javax.xml.stream.XMLEventReader#nextEvent()
		 */
		public XMLEvent nextEvent() throws XMLStreamException {
			while( mReader.hasNext() ){
				XMLEvent e = mReader.nextEvent();
				if( e.isStartDocument() || e.isEndDocument() )
					continue ;
				return e;

				
			}
			return null ;
		}



		/**
		 * @return
		 * @see javax.xml.stream.XMLEventReader#hasNext()
		 */
		public boolean hasNext() {
			if( mReader.hasNext() ){
				try {
					if( mReader.peek().isEndDocument() )
						return false ;
				} catch (XMLStreamException e) {
					return false ;
				}
			}
			return true ;
		}



		/**
		 * @return
		 * @throws XMLStreamException
		 * @see javax.xml.stream.XMLEventReader#peek()
		 */
		public XMLEvent peek() throws XMLStreamException {
			while( mReader.hasNext() ){
				XMLEvent e = mReader.peek();
				if( e.isStartDocument() || e.isEndDocument() )
					mReader.nextEvent();
				else
					return e ;
			}
			return null ;
		}



		/**
		 * @return
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			try {
				return nextEvent();
			} catch (XMLStreamException e) {
				return null ;
			}
		}



		/**
		 * @return
		 * @throws XMLStreamException
		 * @see javax.xml.stream.XMLEventReader#getElementText()
		 */
		public String getElementText() throws XMLStreamException {
			return mReader.getElementText();
		}



		/**
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			mReader.remove();
		}



		/**
		 * @return
		 * @throws XMLStreamException
		 * @see javax.xml.stream.XMLEventReader#nextTag()
		 */
		public XMLEvent nextTag() throws XMLStreamException {
			return mReader.nextTag();
		}



		/**
		 * @param name
		 * @return
		 * @throws IllegalArgumentException
		 * @see javax.xml.stream.XMLEventReader#getProperty(java.lang.String)
		 */
		public Object getProperty(String name) throws IllegalArgumentException {
			return mReader.getProperty(name);
		}



		/**
		 * @throws XMLStreamException
		 * @see javax.xml.stream.XMLEventReader#close()
		 */
		public void close() throws XMLStreamException {
			mReader.close();
		}

	}

	
	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options( SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		
		InputPort stdin = null;
		if( args.size() > 0 )
			stdin = getInput( args.get(0));
		else
			stdin = getStdin();
		if( stdin == null )
			throw new InvalidArgumentException("Cannot open input");
		
			
		try {
			
			SerializeOpts sopts = getSerializeOpts(opts);
			
			XMLEventReader	reader = stdin.asXMLEventReader(sopts);
			OutputPort stdout = getStdout();
			XMLEventWriter  writer = stdout.asXMLEventWriter(sopts);
			
			stdout.setSystemId(stdin.getSystemId());
			XMLEvent e;
			
			writer.setPrefix("jxon", kJXON_NS );
			
			XMLEventReader 	commentReader = null ;
			while( reader.hasNext() ){
				e = (XMLEvent) reader.next();
				
				
				if( e.getEventType() == XMLStreamConstants.COMMENT){
					commentReader =  processComments( writer , (Comment) e ) ;
					if( commentReader != null )
						continue ;
				}
				
				
				writer.add(e);
				if( commentReader != null && e.isStartElement()){
					writer.add(commentReader);
					commentReader.close();
					commentReader = null ;
					
				}

			
			}
			// writer.add(reader);
			reader.close();
			writer.close();
		} 
		finally {
			
			stdin.close();
		}
		return 0;
		
		
	}


	private XMLEventReader processComments(XMLEventWriter writer, Comment comment ) throws XMLStreamException, IOException {
		
		
		String text = comment.getText().trim();
		if( ! text.startsWith("<jxon:"))
			return null ;
		
		/*
		 * Wrap the comment in an xs:annotation
		 * Use String manipulation here instead of events so that we can declare the jxon namespace
		 * 
		 */
		
		String xml = 
			"<xs:annotation xmlns:xs=\"" + kSCHEMA_NS + "\" xmlns:jxon=\""  + kJXON_NS + "\"> " + 
				"<xs:appinfo>" + 
					text + 
				"</xs:appinfo>" +
			"</xs:annotation>" ;
				
		
		
		InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		XMLEventReader	reader = new NoDocXMLEventReader( mInputFactory.createXMLEventReader(is));
		
		return reader ;
		
		
	}

}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
