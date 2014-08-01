/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.ui;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.util.Util;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;

/**
 * Sample
 * 
 * 
 * JTextPane text_panel = new JTextPane(); HTMLEditorKit kit = new
 * HTMLEditorKit(); HTMLDocument doc = new HTMLDocument();
 * text_panel.setEditorKit(kit); text_panel.setDocument(doc);
 * kit.insertHTML(doc, doc.getLength(), "<b>hello", 0, 0, HTML.Tag.B);
 * kit.insertHTML(doc, doc.getLength(), "<font color='red'><u>world</u></font>",
 * 0, 0, null)
 * 
 * @author David A. Lee
 */

public class TextResultPane implements ITextAreaComponent {
	private JTextPane mText;
	private static Logger mLogger = LogManager.getLogger(TextResultPane.class);

	public static enum OutputType {
		PLAIN_TEXT, STYLED_TEXT, HTML
	};

	private OutputType mOutputType = OutputType.PLAIN_TEXT;
	private String sHTML_ROOT = "<html><body><div id='root'></div></html>";
	private static SimpleAttributeSet mStdoutAttr = new SimpleAttributeSet();
	private static SimpleAttributeSet mStdErrAttr = new SimpleAttributeSet();

	static {

		StyleConstants.setForeground(mStdoutAttr, Color.BLACK);
		StyleConstants.setBackground(mStdoutAttr, Color.WHITE);
		StyleConstants.setBold(mStdoutAttr, false);
		StyleConstants.setFontFamily(mStdoutAttr, java.awt.Font.MONOSPACED);

		StyleConstants.setForeground(mStdErrAttr, Color.RED);
		StyleConstants.setBackground(mStdErrAttr, Color.WHITE);
		StyleConstants.setBold(mStdErrAttr, true);
		StyleConstants.setItalic(mStdErrAttr, true);
		StyleConstants.setFontFamily(mStdErrAttr, java.awt.Font.MONOSPACED);




	}

	private String getContentType() {

		switch (mOutputType) {
		case HTML:
			return "text/html";
		case PLAIN_TEXT:
			return "text/plain";
		default:
		case STYLED_TEXT:
			return "text/plain";
		}

	}

	public TextResultPane(OutputType type) {
		mOutputType = type;
		mText = new JTextPane();
		mText.setContentType(getContentType());
		setClear();

	}

	public void setOutputFormat(OutputType type) {
		if (type != mOutputType) {
			mOutputType = type;
			clear();
		}
	}

	@Override
	public JTextComponent getTextComponent() {
		return mText;
	}

	@SuppressWarnings("unused")
	private static String htmlEscape(String s) {

		return s.replace("'", "''").replace("&", "&amp;").replace("<", "&lt;")
				.replace("\n", "<br>");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.sh.ui.OutputTextComponent#addText(java.lang.String)
	 */
	@Override
	public void addText(String text, String port) {
		try {
			switch (mOutputType) {
			case HTML: {

				HTMLDocument doc = (HTMLDocument) mText.getDocument();
				Element elem = doc.getElement("root");
				doc.insertBeforeEnd(elem, text);
				break;
			}

			case PLAIN_TEXT:
			case STYLED_TEXT: {
				SimpleAttributeSet keyWord = getAttributeSet(port);
				StyledDocument doc = mText.getStyledDocument();
				doc.insertString(doc.getLength(), text, keyWord);
				break;
			}
			}

		} catch (Exception e) {
			mLogger.warn(e);
		}

	}

	private SimpleAttributeSet getAttributeSet(String port) {
		if (Util.isEqual(port, "stderr"))
			return mStdErrAttr;
		else
			return mStdoutAttr;
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

	/**
	 * private StyledDocument createDocument() { switch (mOutputType) { case
	 * HTML : return new HTMLDocument( ); case PLAIN_TEXT: case STYLED_TEXT :
	 * default: return new DefaultStyledDocument( ); }
	 * 
	 * }
	 */

	@Override
	public void clear() {
		mText.setContentType(getContentType());
		mText.setDocument(mText.getEditorKit().createDefaultDocument());
		setClear();
	}

	private void setClear() {

		switch (mOutputType) {
		case HTML:
			mText.setText(sHTML_ROOT);
			break;
		case PLAIN_TEXT:
		case STYLED_TEXT:
		default:
			mText.setText("");
		}

	}

	/**
	 * 
	 * @see javax.swing.text.JTextComponent#cut()
	 */
	@Override
	public void cut() {
		mText.cut();
	}

	/**
	 * 
	 * @see javax.swing.text.JTextComponent#copy()
	 */
	@Override
	public void copy() {
		mText.copy();
	}

	/**
	 * 
	 * @see javax.swing.text.JTextComponent#paste()
	 */
	@Override
	public void paste() {
		mText.paste();
	}

	/**
	 * @param selectionStart
	 * @param selectionEnd
	 * @see javax.swing.text.JTextComponent#select(int, int)
	 */
	public void select(int selectionStart, int selectionEnd) {
		mText.select(selectionStart, selectionEnd);
	}

	/**
	 * 
	 * @see javax.swing.text.JTextComponent#selectAll()
	 */
	@Override
	public void selectAll() {
		mText.selectAll();
	}

	@Override
	public boolean isEditable() {
		return mText.isEditable();
	}

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */