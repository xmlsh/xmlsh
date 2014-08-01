/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.JTextField;

public class TextFieldStreamPipe implements Closeable {


	private static final int PIPE_SIZE = 20 ;

	private JTextField mField;
	private PipedOutputStream mOut;
	private PipedInputStream mIn;

	private static Logger mLogger  = LogManager.getLogger(TextFieldStreamPipe.class);

	/**
	 * @wbp.parser.entryPoint
	 */
	TextFieldStreamPipe( JTextField field ,  final SerializeOpts opts ) throws IOException{
		mField = field ;
		mOut = new PipedOutputStream();
		mIn = new PipedInputStream(mOut,PIPE_SIZE);

		mField.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e){
				try {
					if( mOut == null ) {
						mLogger.error("output is closed");
						return ;
					}

					mOut.write( mField.getText().getBytes(opts.getInput_text_encoding()) );
					mOut.write(  Util.getNewline(opts));
					mOut.flush();
					mField.setText("");
				} catch (Exception ex) {
					mLogger.error("Exception writting text from command window",ex);
				}

			}});

	}


	/**
	 * @return the in
	 */
	PipedInputStream getIn() {
		return mIn;
	}


	@Override
	public void close() {
		setReading(false);
		mField.setText("");

		try {
			if( mIn != null )
				mIn.close();
			if( mOut != null)
				mOut.close();

		} catch (IOException e) {
			mLogger.error("Exception closing command pipe ",e);
		} finally {
			mIn = null ;
			mOut = null ;
		}


	}


	public void reset() {

		close();

		try {
			mOut = new PipedOutputStream();
			mIn = new PipedInputStream(mOut,PIPE_SIZE);

		}  catch (IOException e) {
			mLogger.error("Exception closing command pipe ",e);
		} 
	}


	public void setReading(boolean bRead){
		mField.setEditable(bRead);
		mField.setEnabled(bRead);

	}



}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */