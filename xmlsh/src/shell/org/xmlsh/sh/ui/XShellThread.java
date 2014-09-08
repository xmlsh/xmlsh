/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.StreamInputPort;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
// Use a different name to avoid confusion with sh.shell.ShellThread
public class XShellThread extends Thread {

	private volatile Shell mShell = null;
	private boolean mClosed = false ;

	private TextResultPane mResultTextArea;
	private JButton mStopButton;
	private JButton mStartButton; 

	private BlockingQueue<String> mCommandQueue = new ArrayBlockingQueue<String>(2, true);
	private OutputStream mResultOutputStream;
	private OutputStream mResultErrorStream;


	private List<XValue> mArgs;
	private JTextField mCommandField;
	private TextFieldStreamPipe cmdPipe ;
	private SerializeOpts mSerializeOpts;
	private XShell mXShell;
	private static Logger mLogger = LogManager.getLogger();


	private void print(String s) throws UnsupportedEncodingException, IOException {
		mResultErrorStream.write(s.getBytes("UTF8"));
	}




	public XShellThread(XShell xshell, ThreadGroup group , List<XValue> args, TextResultPane resultTextArea, JTextField commandField , 
			JButton startButton , JButton stopButton, SerializeOpts serializeOpts) throws IOException {
		super(group , "xmlshui" );

		mXShell = xshell ;
		mArgs = args ;
		mResultTextArea = resultTextArea;
		mStartButton = startButton ;
		mStopButton = stopButton ;
		mCommandField = commandField ;
		mSerializeOpts = serializeOpts;



	}

	private void setRunning(final boolean bRunning) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mStartButton.setEnabled(!bRunning);
				mStopButton.setEnabled(bRunning);
				cmdPipe.setReading(bRunning);
			}
		});
	}
	private void clearResult()
	{

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				mResultTextArea.clear();

			}
		});

	}

	@Override
	public void run() {


		final XShellThread thisThread = this ;

		mResultTextArea.clear();

		ICommandExpr c = null;





		mResultOutputStream = new TextComponentOutputStream(mResultTextArea, this.mSerializeOpts , "stdout");
		mResultErrorStream = new TextComponentOutputStream(mResultTextArea, this.mSerializeOpts , "stderr");
		mStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if( cmdPipe != null )
					cmdPipe.reset();

				if( mShell != null ) {
					if( thisThread != Thread.currentThread() ) {
						interrupt();
						Thread.yield();
					}
					mShell.shutdown(true,0);
				}				

			}


		});



		try {				
			cmdPipe = new TextFieldStreamPipe( mCommandField , mSerializeOpts);
			setRunning(false);

			String sCmd = null ;
			while (! mClosed ){

					if(( sCmd = mCommandQueue.take()) == null)
						break ;
						
				try (
						StreamInputPort inp =  new StreamInputPort(cmdPipe.getIn(), null) ){

					setRunning(false);
					clearResult();

					mShell = new Shell(false);
					mShell.setArgs(mArgs == null ? new ArrayList<XValue>() : mArgs );
					mShell.setArg0("xmlshui");

					mShell.getSerializeOpts().setInputTextEncoding("UTF-8");
					mShell.getSerializeOpts().setOutputTextEncoding("UTF-8");

					mShell.getEnv().setStdout(mResultOutputStream);
					mShell.getEnv().setStderr(mResultErrorStream);

					// setInput will create a reference
					mShell.getEnv().setInput(null, inp);


					setRunning(true);

					try ( 

					    InputStream sin = new ByteArrayInputStream( sCmd.getBytes("UTF8")) ){
						mShell.runScript(sin , "xmlshui", true );
						mResultOutputStream.flush();
						mResultErrorStream.flush();
						setRunning(false);
					}
					catch (Exception e) {
						mLogger.info("Exception running shell commands",e);
						print(e.getMessage());
					}

				} 
				
				catch (ThrowException e) {
					mLogger.info("Throw running shell commands",e);
					print("Ignoring thrown value: " + e.getMessage());

				} catch (Exception e) {

					mLogger .warn("Exception running shell commands",e);
					SourceLocation loc = c != null ? c.getLocation() : null;

					if (loc != null) {
						String sLoc = loc.toString();

						print(sLoc);
					}

					print(e.getMessage());

				} catch (Error e) {
					mLogger.info("Error running shell commands",e);

					print("Error: " + e.getMessage());
					SourceLocation loc = c != null ? c.getLocation() : null;

					if (loc != null) {
						String sLoc = loc.toString();

						print(sLoc);
					}

				}

				finally {
					if( mShell != null ) {

						mShell.shutdown(true,1000);
						setRunning(false);
						mShell = null ;
					}
					if( cmdPipe != null)
						cmdPipe.close();
					cmdPipe = null ;
					cmdPipe = new TextFieldStreamPipe( mCommandField , mSerializeOpts);
				}
			}

		} 

		catch( Exception e ) {
			mLogger .warn("Exception running shell commands",e);

		}finally {
			if( mShell != null ) {
				mShell.shutdown(true,100);
			}
			Util.safeClose( cmdPipe );
			if( mXShell != null )
				mXShell.onThreadExit( this );

			mXShell = null ;


		}

	}

	private synchronized void printError(Exception e) {
		try {
			mResultErrorStream.flush();
		} catch (IOException e1) {
			mLogger .warn("Exception running shell commands",e);

		}
	}

	/**
	 * @return the blockingQueue
	 * @throws InterruptedException
	 * 
	 * Called by the AWT event thread
	 */

	boolean putCommand(String command) {
		try {
			mCommandQueue.put(command);
		} catch (InterruptedException e) {
			mLogger .warn("Exception running shell commands",e);
			printError(e);
			return false;
		}
		return true;
	}

	public synchronized void close() {
		mClosed = true ;
		this.interrupt();
		if( mShell != null ) {
			mShell.shutdown(true,100);
		}


	}

}

//
//
// Copyright (C) 2008-2014   David A. Lee.
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
