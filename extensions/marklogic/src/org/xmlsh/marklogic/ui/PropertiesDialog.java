/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JList;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

import org.xmlsh.marklogic.util.MLUtil;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.types.XdmVariable;
import javax.swing.JTextPane;
import javax.swing.JTextArea;

public class PropertiesDialog extends JDialog {

	private final JPanel mcontentPanel = new JPanel();
	private ExplorerShell mShell;
	private String mUrl;
	private JList mListCollections;
	private DefaultListModel mCollectionsModel;
	private JTextField mtextUrl;
	private DefaultListModel mPermissionsModel;
	private JTextArea mTextProperties;


	/**
	 * Create the dialog.
	 * @param url 
	 * @param explorerShell 
	 * @throws InterruptedException 
	 */
	public PropertiesDialog(ExplorerShell shell, String url) throws InterruptedException {
		mShell = shell;
		mUrl = url ;
		
		setBounds(100, 100, 538, 234);
		getContentPane().setLayout(new BorderLayout());
		mcontentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(mcontentPanel, BorderLayout.NORTH);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {25, 400};
		gbl_contentPanel.rowHeights = new int[]{14, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		mcontentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblUrl = new JLabel("URL");
			GridBagConstraints gbc_lblUl = new GridBagConstraints();
			gbc_lblUl.insets = new Insets(0, 0, 5, 5);
			gbc_lblUl.anchor = GridBagConstraints.WEST;
			gbc_lblUl.gridx = 0;
			gbc_lblUl.gridy = 0;
			mcontentPanel.add(lblUrl, gbc_lblUl);
			
		}
		{
			
			mCollectionsModel = new DefaultListModel();
			{
				mtextUrl = new JTextField();
				mtextUrl.setEditable(false);
				GridBagConstraints gbc_textUrl = new GridBagConstraints();
				gbc_textUrl.insets = new Insets(0, 0, 5, 0);
				gbc_textUrl.fill = GridBagConstraints.HORIZONTAL;
				gbc_textUrl.gridx = 1;
				gbc_textUrl.gridy = 0;
				mcontentPanel.add(mtextUrl, gbc_textUrl);
				mtextUrl.setColumns(50);
				mtextUrl.setText(mUrl);
			}
			{
				JLabel lblCollections = new JLabel("Collections");
				GridBagConstraints gbc_lblCollections = new GridBagConstraints();
				gbc_lblCollections.anchor = GridBagConstraints.WEST;
				gbc_lblCollections.insets = new Insets(0, 0, 5, 5);
				gbc_lblCollections.gridx = 0;
				gbc_lblCollections.gridy = 1;
				mcontentPanel.add(lblCollections, gbc_lblCollections);
			}
		}
		mListCollections = new JList(mCollectionsModel);
		
		GridBagConstraints gbc_listCollections = new GridBagConstraints();
		gbc_listCollections.insets = new Insets(0, 0, 5, 0);
		gbc_listCollections.fill = GridBagConstraints.BOTH;
		gbc_listCollections.gridx = 1;
		gbc_listCollections.gridy = 1;
		mcontentPanel.add(mListCollections, gbc_listCollections);
		{
			JLabel lblPermissions = new JLabel("Permissions");
			GridBagConstraints gbc_lblPermissions = new GridBagConstraints();
			gbc_lblPermissions.anchor = GridBagConstraints.WEST;
			gbc_lblPermissions.insets = new Insets(0, 0, 5, 5);
			gbc_lblPermissions.gridx = 0;
			gbc_lblPermissions.gridy = 2;
			mcontentPanel.add(lblPermissions, gbc_lblPermissions);
		}
		mPermissionsModel = new DefaultListModel();

		{
			JList listPermissions = new JList(mPermissionsModel);
			GridBagConstraints gbc_listPermissions = new GridBagConstraints();
			gbc_listPermissions.insets = new Insets(0, 0, 5, 0);
			gbc_listPermissions.fill = GridBagConstraints.BOTH;
			gbc_listPermissions.gridx = 1;
			gbc_listPermissions.gridy = 2;
			mcontentPanel.add(listPermissions, gbc_listPermissions);
		}
		{
			JLabel lblProperties = new JLabel("Properties");
			GridBagConstraints gbc_lblProperties = new GridBagConstraints();
			gbc_lblProperties.anchor = GridBagConstraints.WEST;
			gbc_lblProperties.insets = new Insets(0, 0, 5, 5);
			gbc_lblProperties.gridx = 0;
			gbc_lblProperties.gridy = 3;
			mcontentPanel.add(lblProperties, gbc_lblProperties);
		}
		{
			mTextProperties = new JTextArea();
			mTextProperties.setEditable(false);
			GridBagConstraints gbc_textProperties = new GridBagConstraints();
			gbc_textProperties.insets = new Insets(0, 0, 5, 0);
			gbc_textProperties.fill = GridBagConstraints.BOTH;
			gbc_textProperties.gridx = 1;
			gbc_textProperties.gridy = 3;
			mcontentPanel.add(mTextProperties, gbc_textProperties);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		
		mShell.putCommand( new MLQueryRequest("Getting Collections...", 
				"declare variable $url as xs:string external ;" + 
		        "xdmp:document-get-collections($url)" 						
				, new XdmVariable[] { MLUtil.newVariable("url" , mUrl ) } , null) {

			@Override
			void onComplete(ResultSequence rs) throws Exception {
				final String collections[] = rs.asStrings();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					
						for( String s: collections )
							mCollectionsModel.addElement(s);

					}
				});	
				
			} } );
		
		
		mShell.putCommand( new MLQueryRequest("Getting Permissions...", 
				QueryCache.getInstance().getQuery("getPermissions.xquery"),					
				new XdmVariable[] { MLUtil.newVariable("url" , mUrl ) } , 
				null) {

			@Override
			void onComplete(ResultSequence rs) throws Exception {
				final String collections[] = rs.asStrings();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					
						for( String s: collections )
							mPermissionsModel.addElement(s);

					}
				});	
				
			} } );
		
		
		
		mShell.putCommand( new MLQueryRequest("Getting Properties...", 
				"declare variable $url as xs:string external ;" + 
				        "xdmp:document-properties($url)" , 		
				new XdmVariable[] { MLUtil.newVariable("url" , mUrl ) } , 
				null) {

			@Override
			void onComplete(ResultSequence rs) throws Exception {
				final String props = rs.asString();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
					
						mTextProperties.setText(props);
						

					}
				});	
				
			} } );
		
		
		
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