/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.grammar;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;


public class ShellParserReader extends FilterReader {
	
	private char	mReadAhead = (char)-1;
	
	
	// Skip UTF8 BOM if its the first char
	private void skipbom() throws IOException
	{
		int c = super.read();
		if( c != '\ufeff' )
			mReadAhead = (char)c; 
			
	}

	protected ShellParserReader(Reader in) throws IOException {
		super(in);
		skipbom();
		
	}
	public ShellParserReader(InputStream is, String encoding) throws IOException {
		super( new InputStreamReader(is,encoding));

	}

	
	public ShellParserReader(InputStream is, String encoding, boolean bSkipBOM ) throws IOException {
		super( new InputStreamReader(is,encoding));
		if( bSkipBOM )
		   skipbom();
	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read()
	 */
	@Override
	public int read() throws IOException {
		if( mReadAhead != (char)-1 ){
			char ret = mReadAhead ;
			mReadAhead = (char)-1;
			return ret;
		}
		int c ;
		while ( ( c = super.read()) != -1 ){

			if( c == '\r')
				continue ;
			if( c == '\\') {
				c = super.read();
				if( c == '\r')
					c = super.read();
				if( c == '\n'){
					// skip \\ \n 
					continue ;
				}
				mReadAhead = (char)c ;
				return '\\';
			}
			else
				break ;
		}
		return c;
		
	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int nc = 0;
		int c;
		while( nc < len && (c=read()) != -1 ){
			cbuf[off++] = (char)c;
			nc++;
			if( c == '\n')
				break ;
		}
		return( nc > 0 ? nc : -1 );
		
	}


}



//
//
//Copyright (C) 2008-2012  David A. Lee.
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
