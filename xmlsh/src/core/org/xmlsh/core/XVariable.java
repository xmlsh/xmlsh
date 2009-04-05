/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.EnumSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.SAXException;

public class XVariable {
	
	private static final String sName = "name";
	private static final String sVariable = "variable";
	
	private static final String sType = "type";
	private static final String sFlags = "flags";

	public enum XVarFlag {
		EXPORT , 		// to be exported to child shells
		XEXPR ,			// participates in XEXPRs
		READONLY
		
	};
	
	
	private		String	mName;
	private		XValue	mValue;
	private		EnumSet<XVarFlag>	mFlags;

	public XVariable( String name , XValue value , EnumSet<XVarFlag> flags)
	{
		mName = name ;
		mValue = value;
		mFlags = flags;
		
	}
	
	public XVariable( String name , XValue value )
	{
		this( name , value , EnumSet.of( XVarFlag.EXPORT , XVarFlag.XEXPR ));
		
	}
	
	protected XVariable( String name , EnumSet<XVarFlag> flags )
	{
		this( name , null , flags );
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		mName = name;
	}

	/**
	 * @return the value
	 */
	public XValue getValue() {
		return mValue;
	}

	/**
	 * @param value the value to set
	 * @throws InvalidArgumentException 
	 */
	public void setValue(XValue value) throws InvalidArgumentException {
		if( mFlags.contains( XVarFlag.READONLY ))
			throw new InvalidArgumentException("Cannot modify readonly variable: " + getName());
		
		mValue = value;
	}

	/**
	 * @return the flags
	 */
	public EnumSet<XVarFlag> getFlags() {
		return mFlags;
	}

	/**
	 * @param flags the flags to set
	 */
	public void setFlags(EnumSet<XVarFlag> flags) {
		mFlags = flags;
	}

	
	public void serialize(XMLStreamWriter writer) throws SAXException, XMLStreamException {
/*
		AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", sName, sName, "CDATA", getName());
		XValue value = this.getValue();
		String type =
			value.isString() ? "string" : "xml";
		
		atts.addAttribute("", sType, sType, "CDATA", type );
		
		
	//	atts.addAttribute("", "value", "value", "CDATA", value.toString() );
		String flagStr = mFlags.toString();
		
		
		atts.addAttribute("", sFlags, sFlags, "CDATA", flagStr );

		
		
		writer.startElement("", sVariable, sVariable, atts);

		
		
		writer.endElement("", sVariable, sVariable);
*/	
		XValue value = this.getValue();
		String flagStr = mFlags.toString();
		
		writer.writeStartElement(sVariable);
		writer.writeAttribute(sName, getName());
		writer.writeAttribute(sType, (value == null || value.isString()) ? "string" : "xml");
		writer.writeAttribute(sFlags, flagStr );
		writer.writeEndElement();
		
		
		
	}

	
	
	
	public void clear() throws InvalidArgumentException {

		setValue( null );

		
	}

	public boolean isNull() {
		return mValue == null ;
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