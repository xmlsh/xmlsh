/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.nio.charset.Charset;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;

public class SerializeOpts {
	private 	boolean		indent	= true ;
	private		boolean		omit_xml_declaration = true ;
	private		String		encoding = "UTF-8"; // default encoding
	private		String 	 	text_encoding = System.getProperty("file.encoding");
	
	private		boolean		supports_dtd = true ;
	
	
	public SerializeOpts() {}
	
	public SerializeOpts clone()
	{
		return new SerializeOpts(this);
	}
	
	public SerializeOpts( SerializeOpts that ) {
		
		indent = that.isIndent();
		omit_xml_declaration = that.isOmit_xml_declaration();
		encoding = that.getEncoding();
		text_encoding = that.text_encoding;

	}
	
	
	
	public void set( String name , boolean value )
	{
		if( name.equals("omit-xml-declaration" ) )
			omit_xml_declaration = value;
		if( name.equals("indent"))
			indent = value ;
		
	}
	

	public boolean isIndent() {
		return indent;
	}

	
	public boolean isOmit_xml_declaration() {
		return omit_xml_declaration;
	}


	public String getEncoding() {
		return encoding;
	}

	/**
	 * @return the text_encoding
	 */
	public String getText_encoding() {
		return text_encoding;
	}


	public void set(String opt, XValue value) throws InvalidArgumentException {
		
		// If 'encoding' set both text and xml encoding
		// if text-encoding then set text encoding only
		// if xml-encoding then set xml encoding only 
		
		if( opt.equals("text-encoding") || opt.equals("encoding"))
			setText_encoding(value.toString());
		
		if( opt.equals("xml-encoding") || opt.equals("encoding") )
			setEncoding(value.toString());
		
	}

	/**
	 * @param indent the indent to set
	 */
	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	/**
	 * @param omit_xml_declaration the omit_xml_declaration to set
	 */
	public void setOmit_xml_declaration(boolean omit_xml_declaration) {
		this.omit_xml_declaration = omit_xml_declaration;
	}

	/**
	 * @param encoding the encoding to set
	 * @throws InvalidArgumentException 
	 */
	public void setEncoding(String enc) throws InvalidArgumentException {
		
		if( ! Charset.isSupported(enc))
			throw new InvalidArgumentException("encoding not supported: " + enc);
		encoding = enc ;

	}
	public void setText_encoding(String enc) throws InvalidArgumentException {
		
		if( ! Charset.isSupported(enc))
			throw new InvalidArgumentException("encoding not supported: " + enc);
		text_encoding = enc ;

	}

	/**
	 * @return the supports_dtd
	 */
	public boolean isSupports_dtd() {
		return supports_dtd;
	}

	/**
	 * @param supports_dtd the supports_dtd to set
	 */
	public void setSupports_dtd(boolean supports_dtd) {
		this.supports_dtd = supports_dtd;
	}
	
	
}



//
//
//Copyright (C) 2008, David A. Lee.
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
