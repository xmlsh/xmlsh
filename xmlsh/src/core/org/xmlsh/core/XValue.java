/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
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

import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.ITypeFamily;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

public class XValue {
	private static Logger mLogger = Logger.getLogger( XValue.class);

	private     TypeFamily mTypeFamily = null; // default null defers to a runtime evaluatiion
	private		 Object	mValue;		// String , XdmValue , Object , List<XValue> ... 


	public XValue(TypeFamily family) {
		mTypeFamily = family;
		mValue = null ;

	}
	public XValue(TypeFamily family, Object obj) {
		mTypeFamily = family ;
		mValue = obj;
	}

	public XValue()
	{
		mTypeFamily = null ;
		mValue = null ;
	}

	/*
	 *  Create an atomic string (xs:string)
	 */

	public XValue(BigDecimal n) {
		this( TypeFamily.XDM ,  new XdmAtomicValue( n ));
	}


	public XValue(boolean n) {
		this(  TypeFamily.XDM , new XdmAtomicValue( n) );
	}

	public XValue(int n) {
		this(  TypeFamily.XDM, new XdmAtomicValue( n) );
	}

	public XValue(Item<?> item) {
		this( S9Util.wrapItem(item));
	}

	/*
	 *  Create an XValue by combining a list of XdmValue objects into a single XValue
	 */
	public XValue( List<XValue> args) {

		if( isAllXdmValue( args )) {
			ArrayList<XdmItem> items = new ArrayList<XdmItem>();
			for( XValue arg : args ){
				XdmValue v = arg.asXdmValue();
				for( XdmItem item : v )
					items.add( item );

			}
			mValue =  new XdmValue(items);
			mTypeFamily = TypeFamily.XDM;
		} else {
			assert(false);
			mValue = args ;

		}
	}

	public XValue(long n) {
		this(TypeFamily.XDM,new XdmAtomicValue( n ));
	}


	public XValue(String s)
	{
		this( TypeFamily.XDM,new XdmAtomicValue( s ));
	}

	public XValue(String value , ItemType type) throws SaxonApiException {
		this( TypeFamily.XDM,new XdmAtomicValue( value , type  ));
	}

	// Create XValue from an array of strings
	public XValue( String[] astring )
	{
		mTypeFamily = TypeFamily.XDM;
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
		this(TypeFamily.XDM, v);
	}

	public static boolean isAllXdmValue( Iterable<XValue> args) {
		for( XValue arg : args ){
			if( ! arg.isXdmValue() ) {
				return false ;
			}
		}
		return true ;
	}


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

	public XValue append(XValue v) {
		return append( v.asXdmValue() );

	}

	/*


	public XValue append(XValue v) {
		if( mValue == null )
			return v ;


		ITypeFamily tf = typeFamilyInstance();
		if( tf != null )
			return tf.getMethods(mValue.getClass()).append( mValue ,  v );
		else 
		  return new XValue( toString() + v.toString() );

	}

	 */

	public JsonNode asJson() throws InvalidArgumentException  {
		if( mValue == null || mValue instanceof JsonNode )
			return (JsonNode) mValue ;

		return JSONUtils.toJsonNode( toString() );
	}




	public NodeInfo asNodeInfo() throws InvalidArgumentException {

		return asXdmNode().getUnderlyingNode();
	}

	public	Object	asObject()
	{
		return mValue;
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

	public SequenceIterator<?> asSequenceIterator()
	{
		XdmValue value = asXdmValue();
		if( value == null )
			return null ;

		try {
			ValueRepresentation<?> v = value.getUnderlyingValue();
			if (v instanceof Value) {
				return  ((Value<?>)v).iterate();
			} else {
				return SingletonIterator.makeIterator((NodeInfo)v);
			}
		} catch (XPathException e) {
			throw new SaxonApiUncheckedException(e);
		}
	}

	public Source asSource() throws InvalidArgumentException {
		return asXdmNode().asSource();

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

	public XdmItem asXdmItem()
	{
		return XMLUtils.asXdmItem(  asXdmValue() );

	}


	public XdmNode asXdmNode() throws InvalidArgumentException
	{
		XdmItem item = asXdmItem();
		if( item instanceof XdmNode )
			return (XdmNode) item ;
		else
			throw new InvalidArgumentException("Value is not a Node");
	}

	public XdmSequenceIterator asXdmSequenceIterator()
	{
		XdmValue value = asXdmValue();
		if( value == null )
			return null ;

		return value.iterator();		
	}


	/*
	 * Return (cast) the variable to an XdmValue
	 * do not modify the variable itself. 
	 * 
	 */
	public XdmValue asXdmValue(){
		if( mValue != null && mValue instanceof XdmValue )
			return (XdmValue) mValue ;
		else {
			assert(typeFamily() == TypeFamily.XDM);
			return null ;
		}

	}

	public  int canConvert( Class<?> c) throws InvalidArgumentException, UnexpectedException {
		Object value = mValue ;
		if( value == null )
			return -1;

		Class<? extends Object> vclass = value.getClass();

		int ret = JavaUtils.canConvertClass( vclass , c );
		if( ret >= 0 )
			return ret ;

		// Try converting 
		if( value instanceof XdmValue ){
			value = getJavaNative();
			if( value == null )
				return -1 ;
			vclass = value.getClass();
			ret = JavaUtils.canConvertClass( vclass , c );
		}

		return ret ;

	}


	public Object convert( Class<?> c) throws InvalidArgumentException {

		try {
			Object value = mValue ;
			if( value == null )
				return null;


			if( c.isInstance(value))
				return c.cast(value);

			if( value instanceof XdmValue && ! XdmValue.class.equals(c))
				value = getJavaNative();



			return JavaUtils.convert(value, c);
		} catch( Exception e ) {
			Util.wrapException(e, InvalidArgumentException.class );	

			return null; // SNH
		}
	}

	@Override
	public boolean equals( Object that )
	{
		if( this == that )
			return true ;

		return super.equals(that);
	}

	public boolean equals(String s)
	{
		return isAtomic() && toString().equals(s);
	}

	/*
	 * Return a new XValue which is an appending of "this" value
	 * and another XdmValue as a sequence
	 * If This is null or the empty sequence then return the value
	 */

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

	public Object getJavaNative() throws InvalidArgumentException, UnexpectedException
	{
		try {
			if( mValue == null )
				return null ;

			if( isJson() )
				return JSONUtils.asJavaNative( asJson() );

			// Already a java type 
			if( !( mValue instanceof XdmValue) )
				return mValue ;

			XdmValue xv = (XdmValue)mValue ;

			ValueRepresentation<?> value = xv.getUnderlyingValue();
			// Special case for text nodes treat as String
			if( value instanceof NodeInfo &&  ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) 
				return value.getStringValue();

			if( ! ( value instanceof AtomicValue ))
				return value ;

			AtomicValue av = (AtomicValue) value ;
			Object java = Value.convertToJava(av);


			return java;
		}
		catch( Exception e ){
			Util.wrapException("Exception getting java native value",e,UnexpectedException.class);	

		}
		// SNH

		return null ;
	}

	@Override
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
		return XMLUtils.isAtomic(asXdmValue());


	}



	public boolean isEmpty() {

		if( this.isNull() )
			return true ;
		XdmValue value = asXdmValue();
		if( value == null )
			return true ;

		return value.size() == 0 ;
	}

	public boolean isJson()
	{
		if( mTypeFamily == TypeFamily.JSON )
			return true ;
		return mValue != null &&
				XTypeUtils.getFamilyInstance(TypeFamily.JSON).isInstanceOfFamily(mValue);

	}

	public boolean isNull()
	{
		return  mValue == null ;
	}

	public boolean isObject()
	{
		return ( mValue != null && !(mValue instanceof XdmValue)  );
	}

	public boolean isString() {
		if( mValue == null )
			return false ;
		if( mValue instanceof String )
			return true ;

		// Non-XdmValues not considered atomic.
		if( ! (mValue instanceof XdmValue ))
			return false ;


		ValueRepresentation<? extends Item> value = asXdmValue().getUnderlyingValue();
		boolean isString = ( value instanceof net.sf.saxon.value.StringValue ) || ( value instanceof NodeInfo && ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isString ;


	}

	public boolean isXdmNode() {
		return 
				mValue != null && 
				typeFamily() == TypeFamily.XDM &&
				mValue instanceof XdmNode ;
	}
	public boolean isXdmValue() {
		return 
				mValue != null && 
				typeFamily() == TypeFamily.XDM &&
				mValue instanceof XdmValue  ;
	}



	/*
	 * Returns true if the class is an Integer like class
	 */

	public void serialize(OutputStream out, SerializeOpts opts) throws InvalidArgumentException , IOException  
	{
		if( mValue == null )
			return ;
		ITypeFamily tf = typeFamilyInstance();
		if( tf != null )
			tf.getMethods(mValue.getClass()).serialize( mValue , out , opts );
		else 
			out.write( toByteArray(opts) );
		out.flush();

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

	public boolean toBoolean() throws InvalidArgumentException, UnexpectedException  {
		if( mValue == null )
			return false ;


		/*
		 * Check for Java boolean and integer values
		 */
		if( mValue == null )
			return false ;

		try {
			if( isJson() )
				return asJson().asBoolean();
		}
		catch( Exception e ){
			Util.wrapException(e,InvalidArgumentException.class);	
			return false ; // SNH 
		}


		if( ! (mValue instanceof XdmValue ) ){

			if( JavaUtils.canConvertClass( mValue.getClass() , Boolean.class ) >= 0 )
				return ((Boolean)convert(Boolean.class)).booleanValue() ;

			if( JavaUtils.canConvertClass( mValue.getClass() , Long.class ) >= 0 )
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

	public byte[]	toByteArray(SerializeOpts opts) throws IOException {
		try {
			if( mValue != null ){
				switch( typeFamily() ) {
				case JAVA :
				case XTYPE :
					return JavaUtils.toByteArray( mValue  , opts );
				case JSON :
					return JSONUtils.toByteArray( asJson() , opts );
				case XDM :
					return XMLUtils.toByteArray( asXdmValue(), opts);

				}

			}
		} catch (JsonGenerationException | JsonMappingException | UnsupportedEncodingException
				| InvalidArgumentException |  SaxonApiException e) {
			Util.wrapIOException(e);
		}

		return null;
	}

	public double toDouble() {
		if( mValue == null )
			return 0.;

		if( ! isAtomic() )
			return 0. ;

		if( mValue instanceof DoubleValue )
			return ((DoubleValue)mValue).getDoubleValue();


		return Double.parseDouble(toString());		

	}

	public int toInt() throws XPathException {
		return (int) toLong();
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

	@Override
	public String	toString(){
		try {
			return typeFamilyInstance().getMethods(mValue.getClass()).asString( mValue );
		} catch (Exception e) {
			mLogger.debug("Exception in XValue.toString()",e);
		}
		return "";
	}
	public ITypeFamily typeFamilyInstance() {
		return   XTypeUtils.getFamilyInstance(typeFamily());
	}

	public TypeFamily  typeFamily()
	{
		if( mTypeFamily == null )
			mTypeFamily = XTypeUtils.inferFamily(mValue);
		return mTypeFamily ;
	}

	/*
	 * Type Family and 2.0.x extensions
	 */

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
	public boolean isTypeFamily(TypeFamily family) {
		return typeFamily() == family ;
	}
	public static XValue nullValue()
	{
		return new XValue();
	}
	public boolean isInstanceOf(Class<?> cls)
	{

		return mValue != null && 
				cls.isAssignableFrom( mValue.getClass() );
	}
	public <T> T asType(Class<T> cls)
	{
		return cls.cast( mValue );
	}

	public boolean isXType() {
		return isTypeFamily( TypeFamily.XTYPE ) ;
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
