/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
 
public class TextAreaPopupMenu {
 
    TextAreaPopupMenu(final JTextArea textArea) {

 
        final JPopupMenu popup = new JPopupMenu();
 
        JMenuItem menuItem = new JMenuItem("Select All");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
 
            public void actionPerformed(java.awt.event.ActionEvent evt) {
 
            	textArea.selectAll();
 
            }
        });
        popup.add(menuItem);
        
        
        
        menuItem = new JMenuItem("Copy");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
 
            public void actionPerformed(java.awt.event.ActionEvent evt) {
 
            	textArea.copy();
 
            }
        });
        popup.add(menuItem);
 
        popup.add(new JSeparator());
        
        
        if( textArea.isEditable() ){
	 
	 
	        menuItem = new JMenuItem("Cut");
	        menuItem.addActionListener(new java.awt.event.ActionListener() {
	 
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	textArea.cut();
	               
	 
	            }
	        });
	        popup.add(menuItem);
	 
	        popup.add(new JSeparator());
	 
	        menuItem = new JMenuItem("Paste");
	        menuItem.addActionListener(new java.awt.event.ActionListener() {
	 
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	textArea.paste();
	            }
	        });
	        popup.add(menuItem);
        }
 
        
        menuItem = new JMenuItem("Clear");
        menuItem.addActionListener(new java.awt.event.ActionListener() {
 
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	textArea.setText("");
            }
        });
        popup.add(menuItem);
        
        textArea.setComponentPopupMenu(popup);
 
 
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
