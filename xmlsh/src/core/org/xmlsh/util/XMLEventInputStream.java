/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javanet.staxutils.io.XMLWriterUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.xmlsh.sh.shell.SerializeOpts;

/**
 * XMLEventInputStream converts an XMLEventReader into an InputStream
 * 
 * @author David A. Lee
 */

public class XMLEventInputStream extends InputStream {

	private XMLEventReader mReader;
	private	 byte[] 		mBuffer = null;
	private	int		 				mPos	= -1;
	private	boolean					mEOF = false ;
	private	boolean					mTextMode = false ;
	private	SerializeOpts mOpts ;
	
	
	public XMLEventInputStream(XMLEventReader reader, SerializeOpts opts , boolean textMode ) {
		mReader = reader;
		mTextMode = textMode ;
		mOpts = opts;
	}

	@Override
	public int read() throws IOException {
		if( mEOF )
			return -1;
		
		while( mBuffer == null || mPos >= mBuffer.length )
			if( ! fill() )
				return -1;
		
		return mBuffer[mPos++];
			
		
		
	}

	/*
	 * Read some more data from the input
	 */
	private boolean fill() throws IOException 
	{
		if( ! mReader.hasNext()){
			mEOF = true ;
			return false;
		}
		
		try {
			XMLEvent event = mReader.nextEvent();
			//@TODO: HACK: skip start document
			if( event.getEventType() == XMLEvent.START_DOCUMENT)
				event = mReader.nextEvent();
			
			if( mTextMode && event.getEventType() == XMLEvent.CHARACTERS )
				mBuffer = event.asCharacters().getData().getBytes("UTF8");
			else {
				StringWriter w = new StringWriter();
				
				
				
				
				XMLWriterUtils.writeEvent( event , w );
				mBuffer = w.toString().getBytes( "UTF8" );
			}
			mPos = 0;
		} catch ( Exception e )
		{
			throw new IOException(e);
		}
		return true ;
		
		
		
	}

}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
