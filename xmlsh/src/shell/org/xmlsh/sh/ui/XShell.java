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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class XShell {

	private JFrame mframe;
	private JTextArea mCommandTextArea;
	private ShellThread  mShell = null ;
	private JTextArea mResultTextArea;
	
	
	




	


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
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mShell.putCommand(mCommandTextArea.getText());
			}

			
		});
		toolBar.add(btnRun);
		
		JButton btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		toolBar.add(btnStop);
		mframe.getContentPane().setLayout(new BoxLayout(mframe.getContentPane(), BoxLayout.X_AXIS));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mframe.getContentPane().add(splitPane);
		
		mResultTextArea = new JTextArea();
		mResultTextArea.setRows(10);
		
		mShell = new ShellThread( mResultTextArea , btnRun , btnStop);
		
		mCommandTextArea = new JTextArea();
		
		
        JScrollPane scrollCommand = new JScrollPane(mCommandTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JScrollPane scrollResult = new JScrollPane(mResultTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		splitPane.setRightComponent(scrollResult);
		splitPane.setLeftComponent(scrollCommand);
		mCommandTextArea.setRows(10);
		splitPane.setDividerLocation(0.5);

		
		mResultTextArea.setEditable(false);
		
		new TextAreaPopupMenu( mResultTextArea );
		new TextAreaPopupMenu( mCommandTextArea );
		mShell.start();
		
	}

}



//
//
//Copyright (C) 2008-2012 David A. Lee.
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
