/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.FlowLayout;
import javax.swing.JTextPane;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import java.awt.TextArea;
import java.awt.Checkbox;
import java.awt.BorderLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.grammar.ShellParser;
import org.xmlsh.sh.grammar.ShellParserReader;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.PipedStreamPort;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import java.awt.Button;
import javax.swing.JButton;

public class XShell {

	private JFrame mframe;
	private TextArea mCommandTextArea;
	private Shell  mShell = null ;

	private TextArea mResultTextArea;
	
	private void print(String s){
		mResultTextArea.append(s);
	}
	

	private void run() {
			String   sCmd = mCommandTextArea.getText();

            mResultTextArea.setText("");

			Command c = null ;
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			try {
	            mShell.getEnv().setStdout( os );
	            mShell.getEnv().setStderr(os);
				
	            
				c =  mShell.parseEval(sCmd);

				mShell.exec( c );

				String result = os.toString( mShell.getSerializeOpts().getOutputTextEncoding() );
				print(result); 
				
					


			} 
			catch (ThrowException e) {
				print("Ignoring thrown value: " + e.getMessage());


			}
			catch (Exception e) {


				SourceLocation loc = c != null ? c.getLocation() : null ;

				if( loc != null ){
					String sLoc = loc.toString();

					print( sLoc );
				}

				print(e.getMessage());


			} catch (Error e) {
				print("Error: " + e.getMessage());
				SourceLocation loc = c != null ? c.getLocation() : null ;

				if( loc != null ){
					String sLoc = loc.toString();


					print( sLoc );
				}


			}
		}
	



	


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					XShell window = new XShell();
					window.mframe.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws Exception 
	 */
	public XShell() throws Exception {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws Exception 
	 */
	private void initialize() throws Exception {

		

		
		
		
		
		
		mframe = new JFrame();
		mframe.setBounds(100, 100, 709, 604);
		mframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		mframe.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmExit);
		
		JToolBar toolBar = new JToolBar();
		menuBar.add(toolBar);
		
		JButton btnInvoke = new JButton("Run");
		btnInvoke.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
			}

			
		});
		toolBar.add(btnInvoke);
		mframe.getContentPane().setLayout(new BoxLayout(mframe.getContentPane(), BoxLayout.X_AXIS));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mframe.getContentPane().add(splitPane);
		
		mResultTextArea = new TextArea();
		splitPane.setRightComponent(mResultTextArea);
		
		mCommandTextArea = new TextArea();
		splitPane.setLeftComponent(mCommandTextArea);
		
		
		mShell = new Shell(false);
		mShell.getSerializeOpts().setInputTextEncoding("UTF-8");
		mShell.getSerializeOpts().setOutputTextEncoding("UTF-8");
			
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
