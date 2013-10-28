/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.types.XdmVariable;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.marklogic.util.MLUtil;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.Shell;

public class MLRequestThread extends Thread {

	private boolean mClosed = false ;

	private BlockingQueue<MLRequest> mCommandQueue;

	private ExplorerOptions mOpts;

	private ExplorerShell mShell;



	public MLRequestThread(ExplorerShell shell , ExplorerOptions opts , BlockingQueue<MLRequest> requestQueue ) {
		super();
		mOpts = opts ;
		mShell = shell ;
		mCommandQueue = requestQueue;
	}
	
	protected ContentSource getConnection() throws Exception  {

		URI serverUri = new URI(mOpts.mConnectString);
		ContentSource cs = ContentSourceFactory.newContentSource(serverUri,MLUtil.newTrustOptions(serverUri));
		return cs;
	}
	
	

	public void run() {

		try {				
			mShell.setStatus("Idle");


			MLRequest  command ;
			while (! mClosed && (command = mCommandQueue.take()) != null){

				mShell.setStatus( command.getOperation() );
				command.run(this);
				mShell.setStatus("Idle");
			}
		
		} 
		catch( InterruptedException e){
			;
		}
		catch( Exception e ) {
			printError("Exception running command: " + e.getLocalizedMessage()  , e);
		}
		finally {

		}

	}


	private synchronized void printError(String msg , Exception e) 
	{
		mShell.printError(msg, e);
		
	}

	


	public synchronized void close() {
		mClosed = true ;
		
		this.interrupt();

		
	}

}

//
//
// Copyright (C) 2008-2013   David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
