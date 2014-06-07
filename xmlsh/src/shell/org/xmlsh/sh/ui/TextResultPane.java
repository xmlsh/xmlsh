/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument.Content;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.icl.saxon.functions.Position;

/**
 * Sample 
 * 
 *  
 *  JTextPane text_panel = new JTextPane();
    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument doc = new HTMLDocument();
    text_panel.setEditorKit(kit);
    text_panel.setDocument(doc);
    kit.insertHTML(doc, doc.getLength(), "<b>hello", 0, 0, HTML.Tag.B);
    kit.insertHTML(doc, doc.getLength(), "<font color='red'><u>world</u></font>", 0, 0, null)
    
 * @author David A. Lee
 */

public class TextResultPane implements IOutputText  {
	private JTextPane mText;
	private javax.swing.text.Position endPosition;
	private static Logger mLogger = LogManager.getLogger(XShell.class);
	
	private boolean bHTML = false ;
	private String sHTML_ROOT = "<html><body><div id='root'></div></html>";
	
	
	private String getContentType(){
		return bHTML ? "text/html" : "text/plain";
		
	}

	public TextResultPane() {
		
		mText = new JTextPane( createDocument());

		setClear();
		
	}


	
	public JTextComponent getTextComponent() {
		return mText;
	}
	
	private static String htmlEscape( String s ){
			
			return s.replace("'", "''").replace("&", "&amp;").replace("<", "&lt;").replace("\n","<br>") ;

	}

	/* (non-Javadoc)
	 * @see org.xmlsh.sh.ui.OutputTextComponent#addText(java.lang.String)
	 */
	@Override
	public void addText( String text ){
		  try {
		if( bHTML ){
		
			HTMLEditorKit kit = (HTMLEditorKit) mText.getEditorKit();
			HTMLDocument doc  = (HTMLDocument) mText.getDocument();
			Element elem = doc.getElement("root");
			
		  
				doc.insertBeforeEnd(elem, htmlEscape(text));
				
			
		} else {
			SimpleAttributeSet keyWord = new SimpleAttributeSet();
			StyleConstants.setForeground(keyWord, Color.BLUE);
			StyleConstants.setBackground(keyWord, Color.WHITE);
			StyleConstants.setBold(keyWord, true);
			
			StyledDocument doc = mText.getStyledDocument();
			doc.insertString(doc.getLength(),text, keyWord);
		}
			
		  } catch (Exception e) {
				mLogger.info(e);
			}
	    
		//kit.insertHTML(doc, offset, html, popDepth, pushDepth, insertTag);
	}

	/**
	 * @param preferredSize
	 * @see javax.swing.JComponent#setPreferredSize(java.awt.Dimension)
	 */
	public void setPreferredSize(Dimension preferredSize) {
		mText.setPreferredSize(preferredSize);
	}

	/**
	 * @param maximumSize
	 * @see javax.swing.JComponent#setMaximumSize(java.awt.Dimension)
	 */
	public void setMaximumSize(Dimension maximumSize) {
		mText.setMaximumSize(maximumSize);
	}

	/**
	 * @param minimumSize
	 * @see javax.swing.JComponent#setMinimumSize(java.awt.Dimension)
	 */
	public void setMinimumSize(Dimension minimumSize) {
		mText.setMinimumSize(minimumSize);
	}

	/**
	 * @param b
	 * @see javax.swing.text.JTextComponent#setEditable(boolean)
	 */
	public void setEditable(boolean b) {
		mText.setEditable(b);
	}

	/**
	 * @param width
	 * @param height
	 * @see java.awt.Component#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		mText.setSize(width, height);
	}

	/**
	 * @param enabled
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		mText.setEnabled(enabled);
	}

	public String getAsText() {
		return mText.getDocument().toString();
	}

	private StyledDocument createDocument() {
		DefaultStyledDocument doc = bHTML ? new HTMLDocument( ) :
			  new DefaultStyledDocument();
		return doc ;
			
		
	}

	
	
	public void clear() {
		 

		mText.setDocument( createDocument() );
		setClear();
		
	}

	private void setClear() {
		if( bHTML )
		   mText.setText(sHTML_ROOT );
		 else
			mText.setText("");
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