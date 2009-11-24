/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.nio.charset.Charset;
import java.util.List;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;

public class SerializeOpts {
	private 	boolean		indent	= true ;
	private		boolean		omit_xml_declaration = true ;
	private		String		encoding = "UTF-8"; // default encoding
	private		String 	 	text_encoding = System.getProperty("file.encoding");
	private		boolean		supports_dtd = true ;
	private 	boolean		xinclude = false ;
	private		String		content_type = "text/plain";
	private		String		method = "xml";
	
	
	/*
	 * Parsed standardized serialization option definitions
	 */
	private static final List<Options.OptionDef>  mOptionDefs =		
		Options.parseDefs("+indent,+omit-xml-declaration,encoding:,text-encoding:,xml-encoding:,+xinclude,content-type:,method:,+supports-dtd" );
			
			
	public static List<Options.OptionDef> getOptionDefs() { return mOptionDefs ; }
	
	public SerializeOpts() {}
	
	public SerializeOpts clone()
	{
		return new SerializeOpts(this);
	}
	
	
	
	public SerializeOpts( SerializeOpts that ) {
		
		indent = that.indent;
		omit_xml_declaration = that.omit_xml_declaration;
		encoding = that.encoding;
		text_encoding = that.text_encoding;
		supports_dtd = that.supports_dtd;
		xinclude = that.xinclude;
		content_type = that.content_type;
		method = that.method ;

	}
	
	/*
	 * Set serialize options based on a parsed options 
	 */
	public void setOptions( Options opts ) throws InvalidArgumentException
	{
			for( OptionValue ov : opts.getOpts() )
				setOption( ov );
		
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
	
	

	public void setOption(OptionValue ov) throws InvalidArgumentException {
		
		
		if( ov.getOptionDef().hasArgs )
			setOption( ov.getOptionDef().name , ov.getValue() );
		else	
			setOption( ov.getOptionDef().name , ov.getFlag() );
		
	}
	
	
	public void setOption( String name , boolean value )
	{
		if( name.equals("omit-xml-declaration" ) )
			omit_xml_declaration = value;
		else
		if( name.equals("indent"))
			indent = value ;
		else
		if( name.equals("xinclude"))
			xinclude = value;
		else
		if( name.equals("supports-dtd"))
			supports_dtd = value ;
		
		
	}
	

	public void setOption(String opt, XValue value) throws InvalidArgumentException {
		
		// If 'encoding' set both text and xml encoding
		// if text-encoding then set text encoding only
		// if xml-encoding then set xml encoding only 
		
		if( opt.equals("text-encoding") || opt.equals("encoding"))
			setText_encoding(value.toString());
		
		if( opt.equals("xml-encoding") || opt.equals("encoding") )
			setEncoding(value.toString());
		if( opt.equals("content-type"))
			setContent_type(value.toString());
		if(opt.equals("method"))
			setMethod( value.toString());
		
		
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

	/**
	 * @return the xinclude
	 */
	public boolean isXinclude() {
		return xinclude;
	}

	/**
	 * @param xinclude the xinclude to set
	 */
	public void setXinclude(boolean xinclude) {
		this.xinclude = xinclude;
	}

	/**
	 * @return the content_type
	 */
	public String getContent_type() {
		return content_type;
	}

	/**
	 * @param content_type the content_type to set
	 */
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 * @throws InvalidArgumentException 
	 */
	public void setMethod(String method) throws InvalidArgumentException {
		if( method.equals("xml" ) || method.equals("html") || method.equals("xhtml") || method.equals("text"))
			this.method = method;
		else
			throw new InvalidArgumentException("Invalid value for serialization method: must be xml, html, xhtml, text");
		
	}
	
	
}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
