/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.AtomicValue;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.Shell;

public class XValue {
	private static Logger mLogger = Logger.getLogger( XValue.class);
	
	
	private XdmValue	mValue;			// s9 value

	
	
	
	public XValue()
	{
		mValue = null;
	}
	
	/*
	 *  Create an atomic string (xs:string)
	 */
	
	public XValue(String s)
	{
		mValue = new XdmAtomicValue( s );
	}
	/*
	 * Create an XValue from an XdmValue 
	 */
	public XValue( XdmValue v )
	{
		/*
		 * If value is an atomic type, then coerce to string
		 */
/*		
		ValueRepresentation value = v.getUnderlyingValue();
		if( value instanceof AtomicValue ){
			AtomicValue atom = (AtomicValue) value;
			try {
				mString = value.getStringValue();
			} catch (XPathException e) {
				
				// Any error then dont convert
				mValue = v ;
			}
			return ;
		}
*/		
		
		mValue = v;
	}
	
	/*
	 *  Create an XValue by combining a list of XValue objects into a single XValue
	 */
	public XValue( Iterable<XValue> args) {
		ArrayList<XdmItem> items = new ArrayList<XdmItem>();
		for( XValue arg : args ){
			XdmValue v = arg.toXdmValue();
			for( XdmItem item : v )
				items.add( item );
			
		}

		mValue =  new XdmValue(  items);
		
	}
	
	/*
	 * Return (cast) the variable to an XdmValue
	 * do not modify the variable itself. 
	 * 
	 */
	public XdmValue toXdmValue(){
		return mValue ;
		
	}

	
	public XValue(int n) {
		this( Integer.toString(n));
	}

	public XValue(long n) {
		this(Long.toString(n));
	}

	
	public String	toString(){
		if( mValue != null ){
			if( isAtomic() )
				return mValue.toString();
			else
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					serialize( out );
					return out.toString("UTF-8");
				} catch (Exception e )
				{
					mLogger.warn("Exception serializing XML value");
				}
				
				
			}
			
		}
			
		return "";
	}

	
	/**
	 * Variables are considered pure strings
	 * if the string element is not null
	 * @depreciated
	 */
	public boolean isString() {
		return isAtomic();
	}	
	
	
	public XValue	xpath( String expr ) throws UnexpectedException 
	{
		
		
		Processor  processor  = Shell.getProcessor();
	
		XPathCompiler compiler = processor.newXPathCompiler();

		
		try {
			XPathExecutable exec = compiler.compile( expr );

			XPathSelector eval = exec.load();
			eval.setContextItem( mValue.itemAt(0) );
			return new XValue( eval.evaluate());
			
		
		} catch( Exception e ){
			throw new UnexpectedException("Exception evaluating xpath: " + expr,e );
		}
	}
	
	public boolean isNull()
	{
		return  mValue == null ;
	}

	public boolean isXExpr()
	{
		return ! isAtomic();
	}
	
	public boolean equals(String s)
	{
		return isAtomic() && toString().equals(s);
	}
	public boolean equals( XValue that )
	{
		if( this == that )
			return true ;
		
		if( this.isAtomic() && that.isAtomic() )
			return toString().equals(that.toString());
		
		if( mValue != null && that.mValue != null )
			return mValue.equals( that.mValue );
		return false ;
	}

	public boolean isAtomic() {
		if( mValue == null )
			return true ;
		
		ValueRepresentation value = mValue.getUnderlyingValue();
		return value instanceof AtomicValue ;
	
		
	}

	public long toLong() {
		if( mValue == null )
			return 0;
		
		if( ! isAtomic() )
			return -1 ;
		return Long.parseLong(toString());
	}


	public void serialize(OutputStream out) throws UnsupportedEncodingException, IOException, SaxonApiException 
	{
		if( isString() )
			out.write( toString().getBytes("UTF-8") );
		else {
			Serializer ser = new Serializer();
			ser.setOutputStream( out );
			ser.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
			Shell.getProcessor().writeXdmValue( mValue, ser);
		}
			
			
		
		
	}

	public boolean toBoolean() throws UnexpectedException {
		if( mValue == null )
			return false ;
		
		// Sequence of > 1 length 
		if( mValue.size() > 1 )
			return true ;
		// Sequence of 0 length 
		if( mValue.size() == 0 )
			return false ;
		

		Processor  processor  = Shell.getProcessor();
	
		XPathCompiler compiler = processor.newXPathCompiler();

		
		try {
			XPathExecutable exec = compiler.compile( "." );

			XPathSelector eval = exec.load();
			eval.setContextItem( mValue.itemAt(0) );
			return eval.effectiveBooleanValue();
			
		
		} catch( Exception e ){
			throw new UnexpectedException("Exception evaluating boolean xpath" );
		}
		
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
