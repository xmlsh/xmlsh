/**
 * $Id: xpwd.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javanet.staxutils.OutputFactory;

import javax.xml.crypto.dsig.TransformException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XdmNode;
import org.json.JSONObject;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

/*
 * 
 * Convert XML files to an CSV file
 * 
 * Arguments
 * 
 * -header		Add a header row
 * 
 * 
 */

public class xml2json extends XCommand
{

	private 	boolean bIndent = false ;
	private		int 	  mLevel = 0;	// indentation level

	private		SerializeOpts mSerializeOpts;
	
	private 	static	final	String kENCODING_UTF_8 = "UTF-8";

	
	private 	static final 	QName 	kATTR_ENCODING 	= new QName("encoding");
	private 	static final 	QName 	kATTR_NAME 		= new QName("name");
	private 	static final 	QName 	kATTR_VALUE		= new QName("value");
	private 	static final 	QName 	kATTR_SRC		= new QName("src");
	private 	static final 	QName 	kATTR_UNWRAP	= new QName("unwrap");
	private		static	final 	QName	kATTR_HTML		= new QName( "html" );		// A String formated as XHTML 
	
	
	private		static	final 	QName	kELEM_XJSON 	= new QName( "XJSON" );	
	private		static	final 	QName	kELEM_FILE 	   	= new QName( "FILE" );	
	private		static	final 	QName	kELEM_OBJECT	= new QName( "OBJECT" );	// A JSON Object
	private		static	final 	QName	kELEM_MEMBER	= new QName( "MEMBER" );	// A JSON Object Member
	private		static	final 	QName	kELEM_STRING 	= new QName( "STRING" );	// A JSON STRING
	private		static	final 	QName	kELEM_NUMBER	= new QName( "NUMBER" );	// A JSON NUMBER	
	private		static	final 	QName	kELEM_ARRAY		= new QName( "ARRAY" );		// A JSON ARRAY
	private		static	final 	QName	kELEM_BOOLEAN	= new QName( "BOOLEAN" );	// A JSON Literal (true,false)
	private		static	final 	QName	kELEM_NULL		= new QName( "NULL" );		// A JSON Literal null

	
	
	
	

	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("p=print",SerializeOpts.getOptionDefs());
		
		opts.parse(args);

		bIndent = opts.hasOpt("p");
		
		args = opts.getRemainingArgs();

		OutputPort stdout = getStdout();
		
		InputPort inp = args.isEmpty() ? getStdin() : getInput( args.get(0) );

		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XMLEventReader reader = inp.asXMLEventReader(serializeOpts);
		
		// Override the text encoding to UTF-8 - JSON is *always* USTF8
		mSerializeOpts =serializeOpts.clone();
		serializeOpts.setText_encoding(kENCODING_UTF_8);
		PrintWriter writer = stdout.asPrintWriter(serializeOpts);
		
		
		
		parse( reader , writer, false  );
		writer.flush();
		writer.close();

		
		// Consume input or we can get a Piped Close
		while( reader.hasNext() )
			reader.nextEvent();
		
		
		reader.close();
		inp.release();
		
		return 0;
		

		
		
	}

	private boolean parse(XMLEventReader reader, PrintWriter writer, boolean bComma ) throws XMLStreamException, CoreException, UnsupportedEncodingException, IOException, TransformException, SaxonApiException {
		mLevel++;
		
		while( reader.hasNext() ){
			XMLEvent e = reader.nextEvent();
			if( e.isStartElement() ){
				StartElement start = e.asStartElement();
				QName name = start.getName();

				
				if( name.equals(kELEM_XJSON)){
					if( mLevel != 1 )
						throw new UnexpectedException("XJSON element must be at document root");
					
					// Children become the new roots
					
					mLevel=0;
					while( parse( reader , writer , bComma ) )
						;
					return false;

					
				}
				else
				if( name.equals(kELEM_FILE)){
					if( ! writeFile( start , reader , writer ))
						return false ;
						
					}
					
				
				else				
				if( bComma )
					writer.print(",");
				
				if( name.equals(kELEM_OBJECT) ) 
					writeObject( start , reader , writer );
				else
				if( name.equals(kELEM_ARRAY))
					writeArray( start , reader ,  writer );
				else if(name.equals(kELEM_MEMBER) )
					writeMember( start , reader , writer );
				else if( name.equals(kELEM_NUMBER))
					writeNumber( start , reader , writer );
				else if( name.equals(kELEM_BOOLEAN))
					writeBoolean( start , reader , writer );
				else if( name.equals(kELEM_NULL) )
					writeNull( reader , writer );
				else if( name.equals(kELEM_STRING))
					writeString( start , reader , writer );
				else
					readToEnd(reader);
				
				mLevel--;
				return true ;
				
			}
			else
			if( e.isEndElement() ){
				mLevel--;
			
				return false ;
			}
		}
		mLevel--;
		return false ;
	}

	private boolean writeFile(StartElement start, XMLEventReader reader, PrintWriter writer) throws UnsupportedEncodingException, IOException, XMLStreamException, CoreException, TransformException, SaxonApiException {
		Attribute aname = start.getAttributeByName(kATTR_NAME);
		if( aname == null )
			throw new InvalidArgumentException("Element FILE requries attribute name");
		String name = aname.getValue();
		
		//Attribute aencoding = start.getAttributeByName(new QName("encoding"));
		//String encoding = (aencoding == null ? "UTF-8" : aencoding.getValue());
		
		PrintWriter w = getShell().getEnv().getOutput( getShell().getFile(name), false).asPrintWriter(mSerializeOpts);
		
		boolean ret = parse(reader,w,false);
		w.close();
		return ret ;
		
		
		
	}

	private void writeString(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException, UnsupportedEncodingException, FileNotFoundException, IOException, TransformException, SaxonApiException {
		String  value = getAttr( start , kATTR_VALUE);
		String 	src = getAttr( start , kATTR_SRC);
		String  encoding = getAttr( start, kATTR_ENCODING);
		String unwrap = getAttr( start , kATTR_UNWRAP);
		String html = getAttr( start , kATTR_HTML);
		boolean bReadToEnd = true ;
		String chars ;
		if( value != null )
			chars = value ;
		else
		if( src != null )
			chars = readFile( src , encoding );
		else
		{
			// readString eats the close tag
			bReadToEnd = false ;
			chars = readString( reader  , Util.parseBoolean(html));
			
			
		}
		
		
		// If Unwrap then trim off <html> and leading and trailing blanks
		if( Util.parseBoolean(unwrap)){
			value = unwrap(value);
			
		}
		
		
		
		
		writer.print( JSONObject.quote(chars) );
		if( bReadToEnd )
			readToEnd(reader);
		
	}
	/*
	 * Parse an HTML element as XML and reserialize as HTML, store as a JSON string
	 */

	private String readString(XMLEventReader reader, boolean bHTML ) throws TransformException, XMLStreamException, SaxonApiException, IOException
	{
	
		
		byte[]	bytes =  bHTML ?	serializeAsXML( reader ) : serializeAsString(reader);
		
		// String xs = new String(xhtml,klENCODING_UTF_8);
		if( bHTML )
			return formatAsHtml( bytes );
			
		else
			return new String( bytes , kENCODING_UTF_8);
		
		
	}
	
	
	
	/*
	 * Unwrap a string by 
	 * 1) Remove leading and trailing blanks
	 * 2) Remove any <html> (any case) from beginning and end
	 * 3) Remove leading and trailing blanks from the result
	 */
	private String unwrap(String value)
	{
		value = value.trim();
		if( "<html>".equalsIgnoreCase(value.substring(0,6)) )
			value = value.substring(6);
		if( "</html>".equalsIgnoreCase(value.substring(value.length() - 7 )))
			value = value.substring(0 , value.length() - 7 );
		
		return value.trim();
			
	
	}


	private String readFile(String file, String encoding) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		Reader r = 
			new InputStreamReader(
					getShell().getEnv().getInputStream(file) , 
					encoding == null ? mSerializeOpts.getText_encoding() : encoding );
		StringBuffer sb = new StringBuffer();
		
		char	cbuf[] = new char[1000];
		int n;
		while((n=r.read(cbuf)) > 0 )
			sb.append(cbuf, 0, n);
		
		r.close();
		return sb.toString();
		
		
		
	}

	private String getAttr(StartElement start, QName attr) {
		Attribute a = start.getAttributeByName(attr);
		if(a == null )
			return null;
		return a.getValue();
	}

	private void writeNull( XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		writer.print("null");
		readToEnd(reader);
	}

	private void writeBoolean(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		writeNumber( start , reader , writer );
		
	}

	private void writeNumber(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException {
		
		String chars ;
		Attribute v = start.getAttributeByName(kATTR_VALUE);
		if( v != null )
			chars = v.getValue();
		else	
			chars = readChars( reader );
		
		chars = chars.trim();
		
		writer.print( chars );
		readToEnd(reader);
		
		
	}

	private void writeMember(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException, UnsupportedEncodingException, CoreException, IOException, TransformException, SaxonApiException {
		
		indent(writer);
		String name = start.getAttributeByName( new QName("name")).getValue();
		writer.print( JSONObject.quote(name) );
		writer.print(":");
		
		if( parse( reader , writer ,false))
			readToEnd(reader);
		

	}

	private void writeArray(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException, UnsupportedEncodingException, CoreException, IOException, TransformException, SaxonApiException {
		indent(writer);
		writer.print("[");
		boolean bFirst = true ;
		do {

			if( ! parse( reader , writer , ! bFirst  ) )
				break ;
			bFirst = false ;
		}
		while( true  ) ;
		writer.print("]");


		
	}

	private void writeObject(StartElement start, XMLEventReader reader, PrintWriter writer) throws XMLStreamException, UnsupportedEncodingException, CoreException, IOException, TransformException, SaxonApiException {
		indent(writer);
		
		writer.print("{");
		boolean bFirst = true ;
		do {

			if( ! parse( reader , writer , ! bFirst  ) )
				break ;
			bFirst = false ;
		}
		while( true  ) ;
		indent(writer);
		writer.print("}");

		
	}
	
	/*
	 * Serialize the body as HTML and return as a string
	 */
	
	
	private String formatAsHtml(byte[] xhtml) throws SaxonApiException, UnsupportedEncodingException
	{
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Serializer ser = Shell.getProcessor().newSerializer();
		ser.setOutputProperty( Serializer.Property.OMIT_XML_DECLARATION, "yes" );
		ser.setOutputProperty(Serializer.Property.INDENT , "no");
		
		ser.setOutputProperty(Serializer.Property.METHOD, "html");
		ser.setOutputProperty(Serializer.Property.ENCODING, kENCODING_UTF_8);
		ser.setOutputStream(bos);
		
		Processor processor  = Shell.getProcessor();
		DocumentBuilder builder = processor.newDocumentBuilder();
		builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.ALL);
		XdmNode node = builder.build(new StreamSource( new ByteArrayInputStream(xhtml)));
		processor.writeXdmValue(node, ser);
		return bos.toString(kENCODING_UTF_8).trim();
		
		
	}

	
	/*
	 * Serialize as XML
	 */
	private byte[] serializeAsXML(XMLEventReader reader ) throws XMLStreamException
	{
		
		ByteArrayOutputStream	bos = new ByteArrayOutputStream();
		XMLOutputFactory fact = new OutputFactory();
		
		XMLEventWriter writer =  fact.createXMLEventWriter(bos, kENCODING_UTF_8);
		while( reader.hasNext() ){
			XMLEvent event = reader.nextEvent();
			
			if( event.isEndElement() && event.asEndElement().getName().equals(kELEM_STRING))
				break ;
			writer.add(event);
		}
		
		writer.flush();
		writer.close();
		return bos.toByteArray();

	}

	private byte[] serializeAsString(XMLEventReader reader ) throws XMLStreamException, UnsupportedEncodingException, IOException
	{
		
		ByteArrayOutputStream	bos = new ByteArrayOutputStream();
		
		
		while( reader.hasNext() ){
			XMLEvent event = reader.nextEvent();
			
			if( event.isEndElement() && event.asEndElement().getName().equals(kELEM_STRING))
				break ;
			if( event.isCharacters() )
				bos.write( event.asCharacters().getData().getBytes( "UTF-8" ));
		}
		
	
		return bos.toByteArray();

	}
	

	private void indent(PrintWriter writer) {
		if( bIndent ){
			writer.println();
			for( int i = 0 ; i < mLevel ; i++ )
				writer.print(' ');
			
		}
		
	}

	private void readToEnd(XMLEventReader reader) throws XMLStreamException {
		while( reader.hasNext() && ! reader.peek().isEndElement() )
			reader.nextEvent();
		
		if( reader.hasNext())
			reader.nextEvent();
		
	}

	private String readChars(XMLEventReader reader) throws XMLStreamException {

		StringBuffer sb = new StringBuffer();
		while( reader.hasNext() && reader.peek().isCharacters() ){
			Characters ch = reader.nextEvent().asCharacters();
			sb.append( ch.getData() );
			
		}
		return sb.toString();

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
