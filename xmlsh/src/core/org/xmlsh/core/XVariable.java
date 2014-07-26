/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import static org.xmlsh.util.Util.nullIfBlank;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.ITypeFamily;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

import java.util.EnumSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XVariable {
	
	private static final String sName = "name";
	private static final String sVariable = "variable";
	
	private static final String sType = "type";
	private static final String sFlags = "flags";
	private static final String sTypeFamily = "type-family";



	private static Logger mLogger = LogManager.getLogger(XVariable.class);

	public enum XVarFlag {
		EXPORT , 		// to be exported to child shells
		XEXPR ,			// participates in XEXPRs
		READONLY,
		LOCAL,
		UNSET
		
	};
	
	/*
	 * Predefined env and shell variable names
	 */
    public static final String XLOGFILE = "XLOGFILE";
    public static final String XLOG4JPATH = "XLOG4JPATH";
    public static final String XMLSHRC = "XMLSHRC";
    public static final String HOME = "HOME";
    public static final String PATH = "PATH";
    public static final String XPATH = "XPATH";
    public static final String XMODPATH = "XMODPATH";
    public static final String PWD = "PWD";
    public static final String PS1 = "PS1";
    public static final String PS2 = "PS2";
    
    
    
	
	
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


	public void setFlag( XVarFlag flag )
	{
		mFlags.add(flag);
	}

	
	public void serialize(XMLStreamWriter writer) throws SAXException, XMLStreamException {

		XValue value = this.getValue();
		String flagStr = mFlags.toString();
		
		writer.writeStartElement(sVariable);
		writer.writeAttribute(sName, getName());
		writer.writeAttribute(sTypeFamily, value == null ? "null" : value.typeFamily().name() );
		
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
	
	private  XValue	 getTiedValue( Shell shell , XdmItem  item  , XValue arg) throws CoreException
	{
		
		
		Shell saved_shell = ShellContext.set(shell);

		XQueryEvaluator eval = mTieExpr.load();
			
			
		try {
					
			eval.setExternalVariable( new QName("_") , arg.asXdmValue() );
			eval.setContextItem(item);
			
			XdmValue result =  eval.evaluate();

			
			return new XValue(result) ;
			
			
			
			
		} catch (SaxonApiException e) {
			String msg = "Error expanding xml expression: " + arg ;
			mLogger.warn( msg , e );
		    throw new CoreException(msg  , e );
		    
		}
		finally {
			ShellContext.set(saved_shell);
		
		}

		
		
	}
	
	/*
	 * Get a variable value with an optional index and tie expression
	 */

	public XValue getValue(Shell shell, String ind, XValue arg) throws CoreException {
	
		XValue xvalue = getValue();
		if( xvalue == null || xvalue.isNull() )
			return null;
		
		switch( xvalue.typeFamily() ) {
		    case JAVA :
		        return xvalue ;
		    case JSON :
		        return xvalue ;
		    case XDM :
		        XdmValue value = xvalue.asXdmValue(ind);
		        if( value == null )
		            return null ;
		        // TIE expression
		        if( arg != null &&  mTieExpr != null  ){
		            if( value instanceof XdmItem )
		                return this.getTiedValue(shell, (XdmItem)value , arg);
		        }
		        return new XValue(value);
		    case XTYPE:{
		        if( Util.isEmpty(ind))
		            return xvalue ; // no copy ?
		        ITypeFamily tf = xvalue.typeFamilyInstance();
		        XValue v =  tf.getType(xvalue.asObject()).getIndexedValue( xvalue.asObject() , ind );
		        if( v != null )
		            return v ;
		        return xvalue ;
		    }
		    default :
		        return xvalue ;
		 
		}
				
		
		
	}
	public boolean isExport() {
		return mFlags.contains(XVarFlag.EXPORT) && ! mFlags.contains(XVarFlag.UNSET );
	}
	
	public void unset() throws InvalidArgumentException 
	{
		clear();
		mFlags.add( XVarFlag.UNSET );
	}


	
}


//
//
//Copyright (C) 2008-2014    David A. Lee.
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