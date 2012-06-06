/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.xmlsh.core.ThrowException;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.shell.Shell;

public class ShellThread extends Thread {

	private Shell mShell = null;

	private JTextArea mResultTextArea;
	private JButton mStopButton;
	private JButton mStartButton; 

	private BlockingQueue<String> mCommandQueue = new ArrayBlockingQueue<String>(2, true);
	private OutputStream mResultOutputStream;

	private void print(String s) throws UnsupportedEncodingException, IOException {
		mResultOutputStream.write(s.getBytes("UTF8"));
	}

	public ShellThread(JTextArea resultTextArea, JButton startButton , JButton stopButton) {
		super();

		mResultTextArea = resultTextArea;
		mStartButton = startButton ;
		mStopButton = stopButton ;
		
	}

	private void setRunning(final boolean bRunning) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mStartButton.setEnabled(!bRunning);
				
				mStopButton.setEnabled(bRunning);

			}
		});
	}
	private void clearResult()
	{

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mResultTextArea.setText("");

			}
		});
		
	}

	public void run() {

		mResultTextArea.setText("");

		Command c = null;

		mResultOutputStream = new TextAreaOutputStream(mResultTextArea);
		mStopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( mShell != null )
				   mShell.exit(0);
			}

			
		});

		try {				

			String sCmd ;
			while ((sCmd = mCommandQueue.take()) != null){
				setRunning(false);
				clearResult();

				try {
					
					mShell = new Shell(false);
					mShell.getSerializeOpts().setInputTextEncoding("UTF-8");
					mShell.getSerializeOpts().setOutputTextEncoding("UTF-8");

					mShell.getEnv().setStdout(mResultOutputStream);


					mShell.getEnv().setStderr(mResultOutputStream);

					setRunning(true);
					c = mShell.parseEval(sCmd);

					mShell.exec(c);
					mResultOutputStream.flush();
					setRunning(false);

				} catch (ThrowException e) {
					print("Ignoring thrown value: " + e.getMessage());

				} catch (Exception e) {

					SourceLocation loc = c != null ? c.getLocation() : null;

					if (loc != null) {
						String sLoc = loc.toString();

						print(sLoc);
					}

					print(e.getMessage());

				} catch (Error e) {
					print("Error: " + e.getMessage());
					SourceLocation loc = c != null ? c.getLocation() : null;

					if (loc != null) {
						String sLoc = loc.toString();

						print(sLoc);
					}

				}
				finally {
					mShell.close();
					setRunning(false);
				}
			}
		
		} 
		catch( Exception e ) {
			
		}finally {

		}

	}

	private synchronized void printError(Exception e) {
		e.printStackTrace(new PrintStream(mResultOutputStream));
		try {
			mResultOutputStream.flush();
		} catch (IOException e1) {
			;
		}
	}

	/**
	 * @return the blockingQueue
	 * @throws InterruptedException
	 */

	boolean putCommand(String command) {
		try {
			mCommandQueue.put(command);
		} catch (InterruptedException e) {
			printError(e);
			return false;
		}
		return true;
	}

}

//
//
// Copyright (C) 2008-2012 David A. Lee.
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
