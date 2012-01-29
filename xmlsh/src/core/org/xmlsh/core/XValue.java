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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
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
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.Value;
import org.apache.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.StringPair;
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

	public XValue(boolean n) {
		mValue = new XdmAtomicValue( n );
	}
	
	public XValue(BigDecimal n) {
		mValue = new XdmAtomicValue( n );
	}
	
	public XValue(String value , ItemType type) throws SaxonApiException {
		mValue = new XdmAtomicValue( value , type  );
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
				opts.setOutputTextEncoding( encoding );
				
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
			if( isAtomic() || isObject() )
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

	
	
	
	public XValue	xpath( Shell shell , String expr ) throws UnexpectedException 
	{
		if( mValue == null || ! (mValue instanceof XdmValue) )
			return null ;
		
		Processor  processor  = Shell.getProcessor();
	
		XPathCompiler compiler = processor.newXPathCompiler();
		
		Namespaces ns = shell.getEnv().getNamespaces(); 
		if (ns != null) {
			for (String prefix : ns.keySet()) {
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);

			}

		}
		
		
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
	
	public boolean equals( Object that )
	{
		if( this == that )
			return true ;
		
		return super.equals(that);
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
	
	public int hashCode()
	{
		if( mValue == null )
			return 0;
		return mValue.hashCode();
	}
	

	public boolean isAtomic() {
		if( mValue == null )
			return true ;
		
		// Non-XdmValues not considered atomic.
		if( ! (mValue instanceof XdmValue ))
			return false ;
		
		
		ValueRepresentation value = asXdmValue().getUnderlyingValue();
		boolean isAtom = ( value instanceof AtomicValue ) || ( value instanceof NodeInfo && ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isAtom;
	
		
	}

	public long toLong() throws XPathException {
		if( mValue == null )
			return 0;
		
		if( ! isAtomic() )
			return -1 ;
		
		if( mValue instanceof IntegerValue )
			return ((IntegerValue)mValue).longValue();
			
		
		return Long.parseLong(toString());
	}


	public BigDecimal toBigDecimal() throws XPathException {
		if( mValue == null )
			return null ;
		
		if( ! isAtomic() )
			return null ;
		
		if( mValue instanceof AtomicValue ){
			AtomicValue av = (AtomicValue) mValue ;
			if( av instanceof DecimalValue )
				return ((DecimalValue) av).getDecimalValue();
			if( av instanceof DoubleValue )
				return BigDecimal.valueOf( ((DoubleValue)av).getDoubleValue() );
			if( av instanceof FloatValue )
				return BigDecimal.valueOf( ((FloatValue)av).getDoubleValue() );
			if( av instanceof IntegerValue )
				return BigDecimal.valueOf( ((IntegerValue)av).longValue() );
			
		} 
		return BigDecimal.valueOf( Double.valueOf(mValue.toString() ));

	}
	
	
	
	public void serialize(OutputStream out, SerializeOpts opt) throws UnsupportedEncodingException, IOException, SaxonApiException 
	{
		if( isAtomic() || isObject() )
			out.write( toString().getBytes(opt.getOutputXmlEncoding()) );
		else 
		if( mValue instanceof XdmValue )
		
		{
			Serializer ser = Util.getSerializer(opt);
			ser.setOutputStream( out );

			// Shell.getProcessor().writeXdmValue( mValue, ser);
			Util.writeXdmValue( asXdmValue(), ser );
		}
	}
	


    public boolean toBoolean() throws UnexpectedException, XPathException {
		if( mValue == null )
			return false ;
		
		
		/*
		 * Check for Java boolean and integer values
		 */
		if( mValue == null )
			return false ;
		if( ! (mValue instanceof XdmValue ) ){
			
			if( canConvert( mValue.getClass() , Boolean.class ) >= 0 )
				 return ((Boolean)convert(Boolean.class)).booleanValue() ;
			
			if( canConvert( mValue.getClass() , Long.class ) >= 0 )
				return ((Long)convert(Long.class)).longValue() != 0L ;
			

		}
		
		
		
		
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

	/*
	 * Returns 0 if there is an exact match between value.class and c
	 * Returns 1 if value can be converted to c
	 * Returns -1 if value can not be converted to c
	 */


	public  int canConvert( Class<?> c) throws XPathException {
		Object value = mValue ;
		if( value == null )
			return -1;
		
		Class<? extends Object> vclass = value.getClass();

		int ret = canConvert( vclass , c );
		if( ret >= 0 )
			return ret ;
		
		// Try converting 
		if( value instanceof XdmValue ){
			value = getJavaNative();
			if( value == null )
				return -1 ;
			 vclass = value.getClass();
			ret = canConvert( vclass , c );
		
		}
		
		
		return ret ;
		
		
		
	}

	
	/*
	 * Returns 0 if there is an exact match between source and target
	 * Returns 1 if source can be converted to target
	 * Returns -1 if source can not be converted to target
	 */


	private static  int canConvert( Class<?> sourceClass ,  Class<?> targetClass) throws XPathException {

		// Equal class
		if( sourceClass.equals(targetClass))
			return 0 ;
	
		// Directly assignable
		if( targetClass.isAssignableFrom(sourceClass))
			return 1 ;
	
		
		// Boxable 
		// int <-> Integer
		if( JavaUtils.isIntClass(sourceClass) && JavaUtils.isIntClass(targetClass))
			return 2 ;
		
		
		
		
		return -1;
		
		
	}

	
	/*
	 * Returns true if the class is an Integer like class
	 */
	
	public Object convert( Class<?> c) throws XPathException{
			
			Object value = mValue ;
			if( value == null )
				return null;
			

			if( c.isInstance(value))
				return c.cast(value);
			
			if( value instanceof XdmValue && ! XdmValue.class.equals(c))
				value = getJavaNative();
			
			
			
			return JavaUtils.convert(value, c);
			
			

	}

	public Object getJavaNative() throws XPathException
	{
		if( mValue == null )
			return null ;
		
		// Already a java type 
		if( !( mValue instanceof XdmValue) )
			return mValue ;
		
		XdmValue xv = (XdmValue)mValue ;
		
		ValueRepresentation value = xv.getUnderlyingValue();
		// Special case for text nodes treat as String
		if( value instanceof NodeInfo &&  ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) 
			return value.getStringValue();
		
		if( ! ( value instanceof AtomicValue ))
			return value ;
		
		AtomicValue av = (AtomicValue) value ;
		Object java = AtomicValue.convertToJava(av);
			
		
		return java;
	}

	public QName asQName(Shell shell) {
		if( mValue == null )
			return null ;
		
		if( mValue instanceof QName )
			return (QName) mValue ;
		
		String qn = mValue.toString();
		if( qn.startsWith("{") || qn.indexOf(':' ) <= 0 )
			return Util.qnameFromClarkName( mValue.toString() );
		
		StringPair pair = new StringPair(qn,':');
			
		String uri = shell.getEnv().getNamespaces().get(pair.getLeft());
		return new QName( pair.getLeft() , uri , pair.getRight() );
		
		
		
	}


	

	
	
	
	
}
//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
