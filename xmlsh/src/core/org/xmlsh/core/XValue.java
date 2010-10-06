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

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SingletonIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Value;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.Util;

public class XValue {
	private static Logger mLogger = Logger.getLogger( XValue.class);
	
	
	private		Object	mValue;		// String , XdmValue , Object 
	
	
	
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
	
	// Create XValue from an array of strings
	public XValue( String[] astring )
	{
		ArrayList<XdmItem> items = new ArrayList<XdmItem>(astring.length);
		for( String s: astring ){
			items.add(new XdmAtomicValue(s));
			
		}
		mValue = new XdmValue(items);
		
		
	}
	
	
	
	/*
	 * Create an XValue from an XdmValue 
	 */
	public XValue( XdmValue v )
	{
		mValue = v;
	}
	
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
	
	public XValue( Object obj )
	{
		mValue = obj ;
	}
	
	
	/*
	 * Return (cast) the variable to an XdmValue
	 * do not modify the variable itself. 
	 * 
	 */
	public XdmValue asXdmValue(){
		if( mValue != null && mValue instanceof XdmValue )
			return (XdmValue) mValue ;
		else
			return null ;
		
	}

	
	public XValue(int n) {
		mValue = new XdmAtomicValue(  n  );
	}

	public XValue(long n) {
		mValue = new XdmAtomicValue( n );
	}

	
	public XValue(Item item) {
		this( S9Util.wrapItem(item));
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

	
	
	
	public XValue	xpath( String expr ) throws UnexpectedException 
	{
		if( mValue == null || ! (mValue instanceof XdmValue) )
			return null ;
		
		Processor  processor  = Shell.getProcessor();
	
		XPathCompiler compiler = processor.newXPathCompiler();

		
		try {
			XPathExecutable exec = compiler.compile( expr );

			XPathSelector eval = exec.load();
			eval.setContextItem( ((XdmValue)mValue).itemAt(0) );
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
		if( ! (mValue instanceof XdmValue ))
			return true ;
		
		ValueRepresentation value = asXdmValue().getUnderlyingValue();
		boolean isAtom = ( value instanceof AtomicValue ) || ( value instanceof NodeInfo && ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isAtom;
	
		
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
		if( isAtomic() )
			out.write( toString().getBytes(opt.getEncoding()) );
		else 
		if( mValue instanceof XdmValue )
		
		{
			Serializer ser = Util.getSerializer(opt);
			ser.setOutputStream( out );

			// Shell.getProcessor().writeXdmValue( mValue, ser);
			Util.writeXdmValue( asXdmValue(), ser );
		}
	}
	


    public boolean toBoolean() throws UnexpectedException {
		if( mValue == null )
			return false ;
		
		XdmValue value = asXdmValue();
		if( value == null )
			return false ;
				
		// Sequence of > 1 length 
		if( value.size() > 1 )
			return true ;
		// Sequence of 0 length 
		if( value.size() == 0 )
			return false ;
		

		Processor  processor  = Shell.getProcessor();
	
		XPathCompiler compiler = processor.newXPathCompiler();

		
		try {
			XPathExecutable exec = compiler.compile( "." );

			XPathSelector eval = exec.load();
			eval.setContextItem( value.itemAt(0) );
			return eval.effectiveBooleanValue();
			
		
		} catch( Exception e ){
			throw new UnexpectedException("Exception evaluating boolean xpath" );
		}
		
	}
	

	public XdmNode asXdmNode() throws InvalidArgumentException
	{
		XdmItem item = asXdmItem();
		if( item instanceof XdmNode )
			return (XdmNode) item ;
		else
			throw new InvalidArgumentException("Value is not a Node");
	}
	
	public XdmItem asXdmItem()
	{
		XdmValue value = asXdmValue();
		if( value == null)
			return null  ;
		
		try {
			return  value.itemAt(0);
		} catch (IndexOutOfBoundsException e) {
			return null;
		} catch (SaxonApiUncheckedException e) {
			return null;
		}

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
		
		if( xvalue == null )
			return this ;
		
		List<XdmItem> items = new ArrayList<XdmItem>();
		for (XdmItem item : asXdmValue())
			items.add(item);
		
		for( XdmItem item : xvalue )
			items.add(item);
		
		return new XValue(new XdmValue(items));
		
	}

	public void append(XValue v) {
		append( v.asXdmValue() );
		
	}

	public NodeInfo asNodeInfo() throws InvalidArgumentException {
		
		return asXdmNode().getUnderlyingNode();
	}
	
	public SequenceIterator asSequenceIterator()
	{
		XdmValue value = asXdmValue();
		if( value == null )
			return null ;
		
		try {
			ValueRepresentation v = value.getUnderlyingValue();
			if (v instanceof Value) {
				return  ((Value)v).iterate();
			} else {
				return SingletonIterator.makeIterator((NodeInfo)v);
			}
		} catch (XPathException e) {
			throw new SaxonApiUncheckedException(e);
		}
	}

	public XValue shift(int n) {
		if( mValue == null )
			return this ;
		XdmValue value = asXdmValue();
		if( value == null )
			return this ;
		
		XdmItemSubsequence	iter = new XdmItemSubsequence( value , n );
		return new XValue(  new XdmValue(  iter ) );
	}

	public List<String> asStringList() {
		XdmValue value = asXdmValue();
		if( value == null )
			return null ;
		
		
		XdmSequenceIterator iter = value.iterator();
		List<String> list = new ArrayList<String>( value.size() );
		while( iter.hasNext()){
			XdmItem item = iter.next();
			list.add( item.toString() );
			
		}
		return list;
	}

	public boolean isEmpty() {
		
		if( this.isNull() )
			return true ;
		XdmValue value = asXdmValue();
		if( value == null )
			return true ;
		
		return value.size() == 0 ;
	}

	/*
	 * Gets a value with optional indice
	 */
	public XdmValue asXdmValue(String ind) {
		if( mValue == null )
			return null ;
		else
		if( ! ( mValue instanceof XdmValue) )
			return null;
		else
		if( ind == null || ind.equals("*") )
			return (XdmValue) mValue ;
		else 
			return ((XdmValue)mValue).itemAt( Util.parseInt(ind, 0) - 1 );
	
		
	}
	
	public boolean isObject()
	{
		return ( mValue != null && !(mValue instanceof XdmValue)  );
	}
	public	Object	asObject()
	{
		return mValue;
	}

	public boolean canConvert(Class<?> c) {
		return Util.canConvert( mValue ,  c);
		
		
	}
	
	public Object convert( Class<?> c){
		return Util.convert( mValue , c );
	}
	
}
//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
