/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {

	JTextArea	mTextArea;
	ByteArrayOutputStream mBytes = new ByteArrayOutputStream();
	
	
	public TextAreaOutputStream(JTextArea textArea) {
		super();
		mTextArea = textArea;
	}


	@Override
	public void write(int b) throws IOException {
		mBytes.write(b);
		
		
	}


	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		if( mBytes.size() >0){
			String s = mBytes.toString("UTF8");
			mTextArea.append(s);
			mBytes.reset();
		}
	}


	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		flush();
		super.close();
	}  
	
	

}



//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
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
