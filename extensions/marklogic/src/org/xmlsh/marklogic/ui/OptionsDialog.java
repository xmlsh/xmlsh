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

@SuppressWarnings("serial")
public class OptionsDialog extends JDialog {

	private final JPanel mcontentPanel = new JPanel();
	private JTextField mtextConnection;

	private ExplorerOptions mOptions ;
	private JFormattedTextField mtextBatchSize;
	private JFormattedTextField mtextMaxRows;

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
		gbl_contentPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		mcontentPanel.setLayout(gbl_contentPanel);
		{
			JLabel lblXccConnection = new JLabel("XCC Connection");
			lblXccConnection.setHorizontalAlignment(SwingConstants.LEFT);
			GridBagConstraints gbc_lblXccConnection = new GridBagConstraints();
			gbc_lblXccConnection.insets = new Insets(0, 0, 5, 5);
			gbc_lblXccConnection.anchor = GridBagConstraints.EAST;
			gbc_lblXccConnection.gridx = 0;
			gbc_lblXccConnection.gridy = 0;
			mcontentPanel.add(lblXccConnection, gbc_lblXccConnection);
		}
		{
			mtextConnection = new JTextField();
			mtextConnection.setText( mOptions.mConnectString );
			GridBagConstraints gbc_textConnection = new GridBagConstraints();
			gbc_textConnection.insets = new Insets(0, 0, 5, 0);
			gbc_textConnection.fill = GridBagConstraints.HORIZONTAL;
			gbc_textConnection.gridx = 1;
			gbc_textConnection.gridy = 0;
			mcontentPanel.add(mtextConnection, gbc_textConnection);
			mtextConnection.setColumns(10);
		}
		{
			JLabel lblBatchSize = new JLabel("Batch Size");
			GridBagConstraints gbc_lblBatchSize = new GridBagConstraints();
			gbc_lblBatchSize.anchor = GridBagConstraints.EAST;
			gbc_lblBatchSize.insets = new Insets(0, 0, 5, 5);
			gbc_lblBatchSize.gridx = 0;
			gbc_lblBatchSize.gridy = 1;
			mcontentPanel.add(lblBatchSize, gbc_lblBatchSize);
		}
		{
			mtextBatchSize = new JFormattedTextField( NumberFormat.getIntegerInstance());
			mtextBatchSize.setValue(mOptions.mBatchSize);
			GridBagConstraints gbc_textBatchSize = new GridBagConstraints();
			gbc_textBatchSize.insets = new Insets(0, 0, 5, 0);
			gbc_textBatchSize.anchor = GridBagConstraints.WEST;
			gbc_textBatchSize.gridx = 1;
			gbc_textBatchSize.gridy = 1;
			mcontentPanel.add(mtextBatchSize, gbc_textBatchSize);
			mtextBatchSize.setColumns(10);
		}
		{
			JLabel lblMaxRows = new JLabel("Max Rows");
			GridBagConstraints gbc_lblMaxRows = new GridBagConstraints();
			gbc_lblMaxRows.anchor = GridBagConstraints.EAST;
			gbc_lblMaxRows.insets = new Insets(0, 0, 0, 5);
			gbc_lblMaxRows.gridx = 0;
			gbc_lblMaxRows.gridy = 2;
			mcontentPanel.add(lblMaxRows, gbc_lblMaxRows);
		}
		{
			mtextMaxRows = new JFormattedTextField(NumberFormat.getIntegerInstance());
			mtextMaxRows.setValue(mOptions.mMaxRows);
			mtextMaxRows.setColumns(10);
			GridBagConstraints gbc_mtextMaxRows = new GridBagConstraints();
			gbc_mtextMaxRows.anchor = GridBagConstraints.WEST;
			gbc_mtextMaxRows.gridx = 1;
			gbc_mtextMaxRows.gridy = 2;
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
						mOptions.mConnectString = mtextConnection.getText();
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