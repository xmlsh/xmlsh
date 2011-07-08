/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.util.EnumSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import org.xml.sax.SAXException;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

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
	private		XQueryExecutable		mTieExpr;	// Tie expression

	public XVariable( String name , XValue value , EnumSet<XVarFlag> flags)
	{
		mName = name ;
		mValue = value;
		mFlags = flags;
		
	}
	public XVariable clone()
	{
		XVariable that = new XVariable(mName,mValue,mFlags);
		that.mTieExpr = mTieExpr ;
		return that ;
		
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
		String type ;
		if( value == null )
			type = "null";
		else
		if( value.isObject() )
			type = value.asObject().getClass().getName();
		else
		if( value.isAtomic())
			type = "string";
		else
			type = "xml"; 
		
		
		writer.writeAttribute(sType,type);
		writer.writeAttribute(sFlags, flagStr );
		writer.writeEndElement();
		
		
		
	}

	
	
	
	public void clear() throws InvalidArgumentException {

		setValue( null );

		
	}

	public boolean isNull() {
		return mValue == null ;
	}

	public void shift(int n) {
		
		if( n <= 0 || mValue == null )
			return ;
		
		mValue = mValue.shift( n );
		

	}

	public void tie(Shell shell , String expr) throws SaxonApiException {

		if( expr == null )
		{
			mTieExpr = null ;
			return ;
			
		}
		
		
		
		Processor processor = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();

		// Declare the extension function namespace
		// This can be overridden by user declarations
		compiler.declareNamespace("xmlsh", EvalDefinition.kXMLSH_EXT_NAMESPACE);
		
		NameValueMap<String> ns = shell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}
		

		StringBuffer sb = new StringBuffer();

		sb.append("declare variable $_ external;\n");
		sb.append(expr);
			
		mTieExpr = compiler.compile( sb.toString() );
			
		
		
		
	}
	
	private  XValue	 getTiedValue( Shell shell , XdmItem  item  , XValue arg)
	{
		

		
		
		Shell saved_shell = ShellContext.set(shell);

		XQueryEvaluator eval = mTieExpr.load();
			
			
		try {
					
			eval.setExternalVariable( new QName("_") , arg.asXdmValue() );
			eval.setContextItem(item);
			
			XdmValue result =  eval.evaluate();

			
			return new XValue(result) ;
			
			
			
			
		} catch (SaxonApiException e) {
			shell.printErr("Error expanding xml expression",e);
		}
		finally {
			ShellContext.set(saved_shell);
		
		}

		return null;
		
		
	}
	
	/*
	 * Get a variable value with an optional index and tie expression
	 */

	public XValue getValue(Shell shell, String ind, XValue arg) {
	
		XValue xvalue = getValue();
		if( xvalue == null )
			return null;
		
		if( xvalue.isObject() || xvalue.isNull() ){
			if( arg == null )
				return xvalue ;
			
			/*
			 * Get method and args by evaluating arg
			 */
			
			
			
			
			return xvalue ;
			
			
			
		}
		
		
		
		XdmValue value = xvalue.asXdmValue(ind);
		

		
		if( value == null )
			return null ;
		
		// TIE expression
		if( arg != null &&  mTieExpr != null  ){
			if( value instanceof XdmItem )
				return this.getTiedValue(shell, (XdmItem)value , arg);
			
			
		}
		return new XValue(value);
				
		
		
	}
	public boolean isExport() {
		return getFlags().contains(XVarFlag.EXPORT);
	}


	
}


//
//
//Copyright (C) 2008,2009,2010,2011 , David A. Lee.
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