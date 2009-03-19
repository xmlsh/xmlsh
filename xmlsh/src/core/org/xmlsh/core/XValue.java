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
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.AtomicValue;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

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
	 * Flattens sequences
	 *
	public XValue( List<XdmValue> args) {
		ArrayList<XdmItem> items = new ArrayList<XdmItem>();
		for( XdmValue arg : args ){
			for( XdmItem item : arg )
				items.add( item );
			
		}

		mValue =  new XdmValue(  items);
		
	}
	*/
	/*
	 *  Create an XValue by combining a list of XdmValue objects into a single XValue
	 */
	public XValue( Iterable<XValue> args) {
		ArrayList<XdmItem> items = new ArrayList<XdmItem>();
		for( XValue arg : args ){
			XdmValue v = arg.asXdmValue();
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
	public XdmValue asXdmValue(){
		return mValue ;
		
	}

	
	public XValue(int n) {
		this( Integer.toString(n));
	}

	public XValue(long n) {
		this(Long.toString(n));
	}

	
	public byte[]	toBytes(String encoding)
	{
		if( mValue != null ){
			try {
			if( isAtomic() )
				return mValue.toString().getBytes(encoding);
			else
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				SerializeOpts opts = new SerializeOpts();
				opts.setEncoding( encoding );
				
				serialize( out , opts );
				return out.toByteArray();
				
				
			}
			} catch (Exception e )
			{
				mLogger.warn("Exception serializing XML value");
			}
			
			
		}
			
		return null;
	}
	public String	toString(){
		if( mValue != null ){
			if( isAtomic() )
				return mValue.toString();
			else
			{
				try {
					// The encoding can be anything that is round-tripable the end result
					// is a String which is encoding neutral
					return new String(toBytes("UTF-8"),"UTF-8");
				} catch (UnsupportedEncodingException e) {
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


	public void serialize(OutputStream out, SerializeOpts opt) throws UnsupportedEncodingException, IOException, SaxonApiException 
	{
		if( isString() )
			out.write( toString().getBytes(opt.getEncoding()) );
		else {
			Serializer ser = Util.getSerializer(opt);
			ser.setOutputStream( out );

			// Shell.getProcessor().writeXdmValue( mValue, ser);
			Util.writeXdmValue( mValue , ser );
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
	

	public XdmNode asXdmNode() throws InvalidArgumentException
	{
		if( isXExpr() ){
			// If this is a sequence then return the first item
			return (XdmNode) mValue.itemAt(0);
			// return ((XdmNode)mValue);
		}
		else
			throw new InvalidArgumentException("Value is not a Node");
	
		
	}
	
	
	public Source asSource() throws InvalidArgumentException {
		return asXdmNode().asSource();

	}
	
	/*
	 * Return a new XValue which is an appending of "this" value
	 * and another XdmValue as a sequence
	 * If This is null or the empty sequence then return the value
	 */
	
	public XValue append(XdmValue xvalue)
	{
		if( mValue == null )
			return new XValue(xvalue);
		
		
		List<XdmItem> items = new ArrayList<XdmItem>();
		for (XdmItem item : asXdmValue())
			items.add(item);
		for( XdmItem item : xvalue )
			items.add(item);
		
		return new XValue(new XdmValue(items));
		
	}

	public void append(XValue v) {
		append( v.mValue );
		
	}

	public NodeInfo asNodeInfo() throws InvalidArgumentException {
		
		return asXdmNode().getUnderlyingNode();
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
