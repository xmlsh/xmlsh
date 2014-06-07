/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import javax.swing.ScrollPaneConstants;

public class XShell {

	private JFrame mframe;
	private JTextArea mScriptTextArea;
	private ShellThread mShell = null;
	private TextResultPane mResultArea ;
	private File mCurdir;
	private LogFrame mLogWindow = null;
	private JTextField mCommandInputField;
	private SerializeOpts mSerializeOpts ;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		run(args);
	}

	public static void run(final String[] args) {

		run(Util.toXValueList(args));
	}

	public static void run(List<XValue> args) {
		run(new File("."), args ,new SerializeOpts() );

	}

	public static void run(File curdir , List<XValue> args) {
		run(curdir, args ,new SerializeOpts() );

	}

	public static void run(final File curdir, final List<XValue> args , final SerializeOpts sopts   ) {
		
	
		 
		 try {
	            // Set System L&F
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (Exception e) {
	       // handle exception
	    }

		 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					XShell window = new XShell(curdir, args , sopts );
					window.mframe.setVisible(true);
					window.mScriptTextArea.requestFocusInWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public XShell(File curdir, List<XValue> args,SerializeOpts sopts) throws Exception {
		mCurdir = curdir;
		Options opts = new Options( "c=command:,cf=command-file:,f=file:", SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		sopts.setOptions(opts);
		
		mSerializeOpts = sopts ;
		String command = opts.getOptString("c", null);
		if( command == null ){
			String fname = opts.getOptString("cf", null );
			if( fname != null ){
					command =  Util.convertPath(fname , false );
				
			}
			if( command == null ){
			   fname = opts.getOptString("f", null);
		   	   if( fname != null )
				command = Util.readString(new File(curdir, fname ) ,  sopts.getInput_text_encoding() );
			}
	
		}
		initialize(command,opts.getRemainingArgs());
	}


	/**
	 * Initialize the contents of the frame.
	 * @param list 
	 * @param command 
	 * 
	 * @throws Exception
	 */
	private void initialize(String command, List<XValue> args) throws Exception {

		
		
		
		mframe = new JFrame();
		mframe.setMinimumSize(new Dimension(300, 200));
		mframe.setType(Type.UTILITY);
		mframe.getContentPane().setMinimumSize(new Dimension(280, 180));
		mframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				mShell.close();
			}
		});
		mframe.setBounds(100, 100, 709, 604);
		mframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		mframe.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mframe.dispose();
			}
		});
		
		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(mCurdir);

				// Show open dialog; this method does not return until the dialog is closed
				if( fc.showSaveDialog(mframe) == JFileChooser.APPROVE_OPTION ){
					File selFile = fc.getSelectedFile();
					String data = readFrom( selFile );
					if( data != null )
						mScriptTextArea.setText(data);
				}
				
				


			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				JFileChooser fc = new JFileChooser(mCurdir);

				// Show open dialog; this method does not return until the dialog is closed
				fc.showSaveDialog(mframe);
				if( fc.showSaveDialog(mframe) == JFileChooser.APPROVE_OPTION ){
					File selFile = fc.getSelectedFile();
					saveTo( mScriptTextArea.getText() , selFile );
					
				}
				
			}
		});
		mnFile.add(mntmSaveAs);
		
		JMenuItem mntmSaveResults = new JMenuItem("Save Results..");
		mntmSaveResults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(mCurdir);

				// Show open dialog; this method does not return until the dialog is closed
				if( fc.showSaveDialog(mframe) == JFileChooser.APPROVE_OPTION ){
					File selFile = fc.getSelectedFile();
					saveTo( mResultArea.getAsText() , selFile );
					
				}

				
			}


		});
		mnFile.add(mntmSaveResults);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		mnFile.add(mntmExit);
		
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		
		JMenuItem mntmLogWindow = new JMenuItem("Log Window");
		mntmLogWindow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				mLogWindow.setVisible( ! mLogWindow.isVisible());
			}
		});
		mnView.add(mntmLogWindow);
		
		JToolBar toolBar = new JToolBar();
		menuBar.add(toolBar);
		
		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mShell.putCommand(mScriptTextArea.getText());
			}

			
		});
		toolBar.add(btnRun);
		
		JButton btnStop = new JButton("Stop");
		btnStop.setEnabled(false);
		toolBar.add(btnStop);
		mframe.getContentPane().setLayout(new BoxLayout(mframe.getContentPane(), BoxLayout.X_AXIS));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setMinimumSize(new Dimension(250, 150));
		splitPane.setPreferredSize(new Dimension(280, 180));
		splitPane.setAlignmentY(Component.CENTER_ALIGNMENT);
		splitPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mframe.getContentPane().add(splitPane);
		
	     mScriptTextArea = new JTextArea();
	     mScriptTextArea.setPreferredSize(new Dimension(100, 22));
        JScrollPane scrollCommand = new JScrollPane(mScriptTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollCommand.setPreferredSize(new Dimension(200, 100));
        scrollCommand.setMinimumSize(new Dimension(200, 50));
        scrollCommand.setAlignmentY(Component.TOP_ALIGNMENT);
		
        JPanel resultPanel = new JPanel( );
        resultPanel.setMinimumSize(new Dimension(200, 100));
        resultPanel.setBorder(null);
        resultPanel.setPreferredSize(new Dimension(300, 100));
		GridBagLayout gbl_resultPanel = new GridBagLayout();
		gbl_resultPanel.rowHeights = new int[]{323, 0};
		gbl_resultPanel.columnWeights = new double[]{1.0};
		gbl_resultPanel.rowWeights = new double[]{1.0, 0.0};
		resultPanel.setLayout(gbl_resultPanel);
		
		mResultArea = getResultArea();
		JTextComponent textPane = mResultArea.getTextComponent();
		
		
		
		//textPane.setPreferredSize(new Dimension(4, 100));
		JPanel textOuterPanel = new JPanel( new BorderLayout());
		textOuterPanel.add(textPane);
		
	
		
		
        JScrollPane scrollResult = new JScrollPane(textOuterPanel,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        textPane.setEditable(false);
        
        new TextComponentPopupMenu( textPane );
        GridBagConstraints gbc_scrollResult = new GridBagConstraints();
        gbc_scrollResult.fill = GridBagConstraints.BOTH;
        gbc_scrollResult.gridx = 0;
        gbc_scrollResult.gridy = 0;
        resultPanel.add(scrollResult, gbc_scrollResult);
		

	
		
		

        mScriptTextArea.setRows(5);
        
    	
		splitPane.setLeftComponent(scrollCommand);
		splitPane.setRightComponent(resultPanel);
		
		

		mCommandInputField = new JTextField();
		mCommandInputField.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		mCommandInputField.setMargin(new Insets(0, 0, 0, 0));
		mCommandInputField.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_CommandText = new GridBagConstraints();
		gbc_CommandText.ipady = 2;
		gbc_CommandText.ipadx = 2;
		gbc_CommandText.insets = new Insets(2, 2, 2, 2);
		gbc_CommandText.fill = GridBagConstraints.HORIZONTAL;
		gbc_CommandText.anchor = GridBagConstraints.SOUTH;
		gbc_CommandText.gridx = 0;
		gbc_CommandText.gridy = 1;
		resultPanel.add(mCommandInputField, gbc_CommandText);
		mCommandInputField.setColumns(50);
		splitPane.setDividerLocation(0.5);
		
		
        new TextComponentPopupMenu( mScriptTextArea );
		

		mLogWindow = new LogFrame();
		
		if( command != null  )
			mScriptTextArea.setText( command + " "  + Util.stringJoin( Util.toStringList(args)," ") );

		
		mShell = new ShellThread( null ,  mResultArea , mCommandInputField  ,  btnRun , btnStop , mSerializeOpts );
		
		mShell.start();
		
	}


	private TextResultPane getResultArea() {
	
		return new TextResultPane();
		
		
	}

	protected void saveTo(String text, File selFile) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(selFile),
					"utf8");
			writer.write(text);
			writer.close();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(mframe,
					"Error writing to file: " + e.getLocalizedMessage());

		}

	}

	protected String readFrom(File selFile) {
		try {
			return Util.readString(selFile, "utf8");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(mframe,
					"Error reading from file: " + e.getLocalizedMessage());

			return null;
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
