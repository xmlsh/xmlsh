/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.xmlsh.marklogic.util.MLUtil;
import java.text.Format;

@SuppressWarnings("serial")
public class OptionsDialog extends JDialog {

	private final JPanel mcontentPanel = new JPanel();
	private JTextField mtextHost;

	private ExplorerOptions mOptions ;
	private JFormattedTextField mtextBatchSize;
	private JFormattedTextField mtextMaxRows;
	private JTextField mtextUser;
	private JTextField mtextPassword;
	private JTextField mtextDatabase;
	private JFormattedTextField mtextPort;

	/**
	 * Create the dialog.
	 */
	OptionsDialog(ExplorerOptions opts) {
		mOptions = opts ;
		setTitle("Options");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		mcontentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(mcontentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		mcontentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lbllHost = new JLabel("Host");
			lbllHost.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lbllHost = new GridBagConstraints();
			gbc_lbllHost.insets = new Insets(0, 0, 5, 5);
			gbc_lbllHost.anchor = GridBagConstraints.EAST;
			gbc_lbllHost.gridx = 0;
			gbc_lbllHost.gridy = 0;
			mcontentPanel.add(lbllHost, gbc_lbllHost);
		}
		{
			mtextHost = new JTextField();
			mtextHost.setText( mOptions.mHost );
			GridBagConstraints gbc_textHost = new GridBagConstraints();
			gbc_textHost.insets = new Insets(0, 0, 5, 0);
			gbc_textHost.fill = GridBagConstraints.HORIZONTAL;
			gbc_textHost.gridx = 1;
			gbc_textHost.gridy = 0;
			mcontentPanel.add(mtextHost, gbc_textHost);
			mtextHost.setColumns(10);
		}
		{
			JLabel lblPort = new JLabel("Port");
			GridBagConstraints gbc_lblPort = new GridBagConstraints();
			gbc_lblPort.insets = new Insets(0, 0, 5, 5);
			gbc_lblPort.anchor = GridBagConstraints.EAST;
			gbc_lblPort.gridx = 0;
			gbc_lblPort.gridy = 1;
			mcontentPanel.add(lblPort, gbc_lblPort);
		}
		{
			mtextPort = new JFormattedTextField(NumberFormat.getIntegerInstance());
			mtextPort.setValue( mOptions.mPort );
			mtextPort.setColumns(10);
			GridBagConstraints gbc_mtextPort = new GridBagConstraints();
			gbc_mtextPort.insets = new Insets(0, 0, 5, 0);
			gbc_mtextPort.fill = GridBagConstraints.HORIZONTAL;
			gbc_mtextPort.gridx = 1;
			gbc_mtextPort.gridy = 1;
			mcontentPanel.add(mtextPort, gbc_mtextPort);
		}
		{
			JLabel lblUser = new JLabel("User");
			GridBagConstraints gbc_lblUser = new GridBagConstraints();
			gbc_lblUser.insets = new Insets(0, 0, 5, 5);
			gbc_lblUser.anchor = GridBagConstraints.EAST;
			gbc_lblUser.gridx = 0;
			gbc_lblUser.gridy = 2;
			mcontentPanel.add(lblUser, gbc_lblUser);
		}
		{
			mtextUser = new JTextField();
			mtextUser.setText(mOptions.mUser);
			mtextUser.setColumns(10);
			GridBagConstraints gbc_textUser = new GridBagConstraints();
			gbc_textUser.insets = new Insets(0, 0, 5, 0);
			gbc_textUser.fill = GridBagConstraints.HORIZONTAL;
			gbc_textUser.gridx = 1;
			gbc_textUser.gridy = 2;
			mcontentPanel.add(mtextUser, gbc_textUser);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("Password");
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 3;
			mcontentPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		}
		{
			mtextPassword = new JTextField();
			mtextPassword.setText( mOptions.mPassword);
			mtextPassword.setColumns(10);
			GridBagConstraints gbc_textPassword = new GridBagConstraints();
			gbc_textPassword.insets = new Insets(0, 0, 5, 0);
			gbc_textPassword.fill = GridBagConstraints.HORIZONTAL;
			gbc_textPassword.gridx = 1;
			gbc_textPassword.gridy = 3;
			mcontentPanel.add(mtextPassword, gbc_textPassword);
		}
		{
			JLabel lblNewLabel_2 = new JLabel("Database");
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 4;
			mcontentPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		}
		{
			mtextDatabase = new JTextField();
			mtextDatabase.setText(mOptions.mDatabase);
			mtextDatabase.setColumns(10);
			GridBagConstraints gbc_textDatabase = new GridBagConstraints();
			gbc_textDatabase.insets = new Insets(0, 0, 5, 0);
			gbc_textDatabase.fill = GridBagConstraints.HORIZONTAL;
			gbc_textDatabase.gridx = 1;
			gbc_textDatabase.gridy = 4;
			mcontentPanel.add(mtextDatabase, gbc_textDatabase);
		}
		{
			JLabel lblBatchSize = new JLabel("Batch Size");
			GridBagConstraints gbc_lblBatchSize = new GridBagConstraints();
			gbc_lblBatchSize.anchor = GridBagConstraints.EAST;
			gbc_lblBatchSize.insets = new Insets(0, 0, 5, 5);
			gbc_lblBatchSize.gridx = 0;
			gbc_lblBatchSize.gridy = 5;
			mcontentPanel.add(lblBatchSize, gbc_lblBatchSize);
		}
		{
			mtextBatchSize = new JFormattedTextField( NumberFormat.getIntegerInstance());
			mtextBatchSize.setValue(mOptions.mBatchSize);
			GridBagConstraints gbc_textBatchSize = new GridBagConstraints();
			gbc_textBatchSize.insets = new Insets(0, 0, 5, 0);
			gbc_textBatchSize.anchor = GridBagConstraints.WEST;
			gbc_textBatchSize.gridx = 1;
			gbc_textBatchSize.gridy = 5;
			mcontentPanel.add(mtextBatchSize, gbc_textBatchSize);
			mtextBatchSize.setColumns(10);
		}
		{
			JLabel lblMaxRows = new JLabel("Max Rows");
			GridBagConstraints gbc_lblMaxRows = new GridBagConstraints();
			gbc_lblMaxRows.anchor = GridBagConstraints.EAST;
			gbc_lblMaxRows.insets = new Insets(0, 0, 5, 5);
			gbc_lblMaxRows.gridx = 0;
			gbc_lblMaxRows.gridy = 6;
			mcontentPanel.add(lblMaxRows, gbc_lblMaxRows);
		}
		{
			mtextMaxRows = new JFormattedTextField(NumberFormat.getIntegerInstance());
			mtextMaxRows.setValue(mOptions.mMaxRows);
			mtextMaxRows.setColumns(10);
			GridBagConstraints gbc_mtextMaxRows = new GridBagConstraints();
			gbc_mtextMaxRows.insets = new Insets(0, 0, 5, 0);
			gbc_mtextMaxRows.anchor = GridBagConstraints.WEST;
			gbc_mtextMaxRows.gridx = 1;
			gbc_mtextMaxRows.gridy = 6;
			mcontentPanel.add(mtextMaxRows, gbc_mtextMaxRows);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						mOptions.mHost = mtextHost.getText() ;
						mOptions.mUser = mtextUser.getText();
						mOptions.mDatabase = mtextDatabase.getText();
						mOptions.mPassword = mtextPassword.getText();
						mOptions.mPort =  MLUtil.getIntValue( mtextPort.getValue());
						mOptions.mBatchSize = MLUtil.getIntValue( mtextBatchSize.getValue());
						mOptions.mMaxRows= MLUtil.getIntValue(mtextMaxRows.getValue());
						
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
	}

}



/*
 * Copyright (C) 2008-2014 David A. Lee.
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