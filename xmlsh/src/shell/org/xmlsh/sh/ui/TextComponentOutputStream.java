/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.xmlsh.sh.shell.SerializeOpts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.SwingUtilities;

public class TextComponentOutputStream extends OutputStream {
	private static Logger mLogger = LogManager.getLogger();

	ITextAreaComponent	mOutputText;
	ByteArrayOutputStream mBytes = new ByteArrayOutputStream();
	private String port;
	private SerializeOpts mSerializeOpts;



	public TextComponentOutputStream(ITextAreaComponent text, SerializeOpts serializeOpts, String port) {
		super();
		this.port = port;
		this.mSerializeOpts = serializeOpts;
		mOutputText = text;
	}


	@Override
	public void write(int b) throws IOException {
		mBytes.write(b);

	}


	/* (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void flush() throws IOException {
		if( mBytes.size() >0){
			final String s = mBytes.toString(mSerializeOpts.getOutput_text_encoding());
			mBytes.reset();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// Here, we can safely update the GUI
					// because we'll be called from the
					// event dispatch thread
					mOutputText.addText(s,port);

				}
			});


		}
	}


	/* (non-Javadoc)
	 * @see java.io.OutputStream#close()
	 */
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void close() throws IOException {
		flush();
		super.close();
	}


	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void write(byte[] b) throws IOException {
		mBytes.write(b);
	}


	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		mBytes.write(b,off,len);
	}  



}



//
//
//Copyright (C) 2008-2014   David A. Lee.
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
