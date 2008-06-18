/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.commands;

/**
 * Command: xsplit
 * Usage: xsplit [options] [input]
 * Options:
 * 		-p	prefix
 * 		-s	suffix
 * 		-e	extension
 * 		-c	num
 * 			Number of child XML elements to output (def 1)
 * 		-n  
 * 			Do not wrap in root element (requires -c=1)
 * 		-w  elem
 * 			Wrap in element instead of root element
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLException;

public class xsplit extends XCommand {

	
	private 	XMLOutputFactory mOutputFactory = XMLOutputFactory.newInstance();
	private		XMLEventFactory  mEventFactory  = XMLEventFactory.newInstance();

	/*
	 * Runtime data
	 */
	
	private		XMLEvent		mRootNode = null;
	private		String 			mPrefix = "x";
	private		int				mSeq 	= 0;
	private		String			mSuffix = "";
	private		String			mExt 	= ".xml";
	
	private		boolean			mNoRoot = false ;
	private		int				mNumChildren = 1;
	
	private		List<XMLEvent>	mHeader = new ArrayList<XMLEvent>();
	
	private		XEnvironment	mEnv ;

	/**
	 * 
	 * 
	 * 
	 * @param args
	 * @throws XMLException 
	 */
	public static void main(String[] args) throws Exception {
		xsplit  cmd = new xsplit();

		
		
		cmd.run( args );
		
		
	}
	

	
	
	
	public int run( List<XValue> args, XEnvironment env )	throws Exception
	{
		mEnv = env;


		Options opts = new Options( "w:,n,p:" , args );
		opts.parse();
		
		// root node
		OptionValue ow = opts.getOpt("w");
		XValue wrapper = null;
		if( ow != null ){
			wrapper = ow.getValue();
			mRootNode = mEventFactory.createStartElement( new QName(null,wrapper.toString()), null, null);
		}
		
		
		
		if( opts.hasOpt("c")){
			mNumChildren = Util.parseInt(opts.getOpt("c").getValue().toString(),1);
		}
		
		
		if( opts.hasOpt("n") ){ 
			mNoRoot = true ;
			mNumChildren = 1;
		}

		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		if( xvargs.size() > 1 ){
			usage(env);
			return 1;
		}
		
		InputStream is = 
			xvargs.size() == 1 ? 
					new FileInputStream(env.getFile(xvargs.get(0))): 
					env.getStdin() ;
		split(is ,env );
		
		
		
		
		
		
		
		return 0;


	}





	private void split(InputStream is,XEnvironment env) throws XMLStreamException, IOException {
	
		XMLInputFactory inputFactory=XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.valueOf(true));
		XMLEventReader  xmlreader  =inputFactory.createXMLEventReader(is);
		
		
		/* 
		 * Read up to root elem collecting events to repeat on each file
		 * 
		 */
		while( xmlreader.hasNext()   ){
			XMLEvent e = xmlreader.nextEvent();
			if( e.getEventType() != XMLStreamConstants.START_ELEMENT ){
				mHeader.add(e);
				continue;
			}
			
			// Found document root
			if( mRootNode == null )
				mHeader.add(e);
			else
				mHeader.add( mRootNode );
			break;
			
			
		}
		
		/*
		 * For each element write it to a new file
		 */
		
		while( xmlreader.hasNext() ){
			XMLEvent e = xmlreader.nextEvent();
			
			int type = e.getEventType() ;
			
			if( type == XMLStreamConstants.START_ELEMENT ){
				write( xmlreader , e );
				
				
			} else
			if( type == XMLStreamConstants.END_ELEMENT ||
				type == XMLStreamConstants.END_DOCUMENT 
				
			)
			{
				// ignore
			}
			
			else
			{
				
				env.printErr("Skipping XML node: " + e.toString());
			}
		}
		
		xmlreader.close();
				
			
		
	}


	/* 
	 * Write out a single element and all its children to the next file
	 * 
	 */



	private void write(XMLEventReader xmlreader, XMLEvent first) throws XMLStreamException, IOException {
		File fout = nextFile();
		XMLEventWriter w = mOutputFactory.createXMLEventWriter( new FileOutputStream(fout));
		
		
		/*
		 * Write the common header elements
		 */
		for( XMLEvent e : mHeader )
			w.add(e);
		
		
		
		w.add(first);
		
		int depth = 0;
		while( xmlreader.hasNext() ){
			XMLEvent e = xmlreader.nextEvent();
			w.add(e);

			if( e.getEventType() == XMLStreamConstants.START_ELEMENT )
				depth++;
			else
			if( e.getEventType() == XMLStreamConstants.END_ELEMENT ){
				if( depth-- <= 0 )
					break;

			}
			
		}
		
		/*
		 * End with end element and end document
		 */
		w.add( mEventFactory.createEndElement( first.asStartElement().getName(), null));
		w.add( mEventFactory.createEndDocument());
		w.close();
		
		
		
	}





	private File nextFile() throws IOException {
		File f = mEnv.getFile( mPrefix + mSeq++ + mSuffix + mExt );
		return f;
	}





	private int usage( XEnvironment env ) {
		env.getShell().printErr("Usage: xsplit [-w wrap] [-c children] [-n]  [-p prefix] [file]");
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
