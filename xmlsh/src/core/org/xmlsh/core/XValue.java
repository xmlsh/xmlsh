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

import org.apache.logging.log4j.Logger;

import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.IMethods;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * A XValue is a single value or a sequence of 0 or more values
 * 
 */
public class XValue implements Iterable<XValue>{
  
  
	private static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger( XValue.class);

	private     TypeFamily mTypeFamily = null; // default null defers to a runtime evaluatiion
	private		 Object	mValue;		// String , XdmItem , Object , List<XValue> ... 




	

  private void _init()
  {
    if( mTypeFamily == null )
      mTypeFamily = XTypeUtils.inferFamily(mValue);
    
    switch( mTypeFamily ) {
    case XDM :
      if( mValue == null )
        _initSequence(null);
      else
      if( mValue instanceof XdmValue ) {
        XdmValue v = (XdmValue) mValue ;
        switch( v.size() ){
        case 0:
          _initSequence(null);
           break;
        case 1:
          assert( mValue instanceof XdmAtomicValue || mValue instanceof XdmNode );
          break;
        default:
          _initSequence( new XValueSequence(XMLUtils.toXValueList( v ) ) );
         break;
        }
      }
      if( mValue instanceof XdmValue ) 
        assert( ( (XdmValue) mValue ).size() <= 1 );
      if( mValue instanceof QName )
        mValue = new XdmAtomicValue( (QName) mValue );
      else
      if( mValue instanceof String )
        mValue = new XdmAtomicValue( (String) mValue );
      else
      if( mValue instanceof URI )
        mValue = new XdmAtomicValue( (URI) mValue );
      break;
     
      case JAVA  :
      case XTYPE :
      case JSON  :
        break;
      default :
        assert(true );
     break;
    }
    
    
  }
  
	private void _initSequence(IXValueSequence seq)
  {
    this.mTypeFamily = TypeFamily.XTYPE ;
    this.mValue = seq == null ?  XValueSequence.emptySequence() : seq ;
    _init();
    
  }

  public static XValue empytSequence()
  {
 
    return new XValue(TypeFamily.XTYPE ,XValueSequence.emptySequence() );
    
  }

  public XValue(TypeFamily family) {
		mTypeFamily = family;
		mValue = null ;
    _init();


	}
	public XValue(TypeFamily family, Object obj) {
		mTypeFamily = family ;
		mValue = obj;
    _init();

	}

	public XValue()
	{
		mTypeFamily = null ;
		mValue = null ;
    _init();

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
	 *  Create an XValue by combining a list of XValue objects into a single XValue as a sequence
	 */
	public XValue( List<XValue> args) {
	  assert( args != null );
	  
	  if( args.isEmpty() ) {
	    _initSequence(null);

	  }
	  else
	  if( args.size() == 1 ) {
	    this.mTypeFamily = args.get(0).mTypeFamily;
	    this.mValue = args.get(0).mValue;
	  } else {

	    _initSequence(new XValueSequence( args ));
	  }
    _init();

    
	    
	}

  public XValue(long n) {
		this(TypeFamily.XDM,new XdmAtomicValue( n ));
	}


	public XValue(String s)
	{
		this( TypeFamily.JAVA,s);
	}

	public XValue(String value , ItemType type) throws SaxonApiException {
		this( TypeFamily.XDM,new XdmAtomicValue( value , type  ));
	}

	// Create XValue from an array of strings
	public XValue( String[] astring )
	{

	  this( new XValueSequence(Util.toXValueList(astring)));
	  
	  /*
	  
		mTypeFamily = TypeFamily.XDM;
		ArrayList<XdmItem> items = new ArrayList<XdmItem>(astring.length);
		for( String s: astring ){
			items.add(new XdmAtomicValue(s));

		}
		mValue = XMLUtils.asXdmValue(items);
   */

	}

	/*
	 * Create an XValue from an XdmValue 
	 */
	public XValue( XdmValue v )
	{
		this(TypeFamily.XDM, v == null ? null :  XMLUtils.simplify(v) );
	}

	public XValue(IXValueMap map) {
	    this(TypeFamily.XTYPE , map );
    }
   public XValue(IXValueList list) {
        this(TypeFamily.XTYPE , list );
    }
   
   public XValue(XValueProperty prop) {
       this(TypeFamily.XTYPE , prop );
   }
	
	
    public XValue(XValuePropertyList plist )
  {
     this(TypeFamily.XTYPE, plist );
  }
    
    /* 
     * Internal method to create an XValue from an object with no hints
     * as to family type 
     * 
     */
    private  XValue(Object obj)
    {

      this( null , obj );
    
    }

    public XValue( IXValueSequence seq ) {
     this( TypeFamily.XTYPE , seq ); 
      
    }
	public XValue append(XdmValue value)
	{
	  
		if( mValue == null )
			return new XValue(value);

		
		if( value == null )
			return this ;

		/*
		List<XdmItem> items = new ArrayList<XdmItem>();
		for (XdmItem item : asXdmValue())
			items.add(item);

		for( XdmItem item : xvalue )
			items.add(item);

		return new XValue(new XdmValue(items));
*/
		return append( new XValue( value ) );
	}
	
	/*

	public XValue append(XValue v) {
		return append( v.asXdmValue() );

	}
*/

	public XValue append(XValue v) throws InvalidArgumentException {
		if( mValue == null )
			return v ;

		return getTypeMethods().append( mValue ,  v );

	}

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
    String qn = null;
		if( mValue instanceof XdmAtomicValue ) {
		  Object v = ((XdmAtomicValue)mValue).getValue();
		  if( v instanceof QName )
		    return (QName)v ;
		  qn = v.toString();
		}
		if( qn == null && ! isAtomic() )
		  return null ;
    if( qn == null )
 		  qn = mValue.toString();
		if( qn.startsWith("{") || qn.indexOf(':' ) <= 0 )
			return Util.qnameFromClarkName( mValue.toString() );

		StringPair pair = new StringPair(qn,':');

		String uri = shell.getEnv().getNamespaces().get(pair.getLeft());
		return new QName( pair.getLeft() , uri , pair.getRight() );

	}

	/*
	public SequenceIterator<?> asSequenceIterator()
	{
		XdmValue value = toXdmValue();
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
	*/

	public Source asSource() throws InvalidArgumentException {
		return asXdmNode().asSource();

	}

	public List<String> asStringList() {
	  if( isNull() )
	    return null;
	   List<String> list = new ArrayList<String>(  );
	  for( XValue v : this ) 
	    list.add(v.toString());
		return list;
	}

	public XdmItem asXdmItem()
	{
	  if( mValue instanceof XdmItem )
	    return (XdmItem) mValue ;
	  return null ;
		// return XMLUtils.asXdmItem(  asXdmValue() );

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
		XdmValue value = toXdmValue();
		if( value == null )
			return null ;

		return value.iterator();		
	}


	public XdmValue toXdmValue()
  {
	  if( isSequence() ) 
	    return XMLUtils.toXdmValue( ((XValueSequence) mValue) );
	  else
	    
	  return XMLUtils.toXdmValue( mValue );
	  
  
  }
  /*
	 * Return (cast) the variable to an XdmValue
	 * do not modify the variable itself. 
	 * 
	 */
	/*
	public XdmValue asXdmValue(){
		if( mValue != null && mValue instanceof XdmValue )
			return (XdmValue) mValue ;
		else {
			assert(typeFamily() == TypeFamily.XDM);
			return null ;
		}

	}
*/
	public  int canConvert( Class<?> c) throws InvalidArgumentException, UnexpectedException {
		Object value = mValue ;
		if( value == null )
			return -1;

		Class<? extends Object> vclass = value.getClass();


		
		if( isSequence() ) {
		  if( c.isAssignableFrom( XdmValue.class ) ) {
		   if( isEmptySequence() )
		     return 1;
		    
		   int max = -1;
		   for( XValue v : this ) {
		     int ic = v.canConvert( c );
         if( ic < 0 ) break ;
         if( ic >= max )
           max = ic;
		   }
		   if( max >= 0 )
		     return  max;
		  }
		}
		
		 // This can be very heavy weight
    int ret = JavaUtils.canConvertClass( vclass , c );
    if( ret >= 0 )
      return ret ;		
		
		// Is this a Xdm and want to convert to something else
		// SNH : in JavaUtils
		if( value instanceof XdmValue ){
		  
			value = getJavaNative();
			if( value == null )
				return -1 ;
			vclass = value.getClass();
			ret = JavaUtils.canConvertClass( vclass , c );
		}

		return ret -1;

	}


	public boolean isEmptySequence()
  {
	  return 
	  isInstanceOf(XValueSequence.class) &&
      asType(XValueSequence.class).isEmpty() ;
	  

  }

  public Object convert( Class<?> c) throws InvalidArgumentException {

		try {
			Object value = mValue ;
			if( value == null )
				return null;
			
      if( c.isAssignableFrom(value.getClass()) )
        return value ;

			if( c.isInstance(value))
				return c.cast(value);

			if( c.isAssignableFrom( XdmValue.class ) ) {
			 if( isSequence() ) {
			   if( isEmptySequence() ) 
			     return XMLUtils.emptySequence() ;
			   else
			       return (XdmValue) XMLUtils.toXdmValue( this ) ;
			}
			}
			
      if( JavaUtils.canConvertClass(value.getClass(), c) >= 0 ) {
        Object obj =  JavaUtils.convert(value, c);
        if( obj != null )
          return obj;
      }
			
			if( value instanceof XdmValue )
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

	
	// somewhat bogus
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

		return 
		this.typeFamilyInstance().isAtomic(mValue);

	}

	public boolean isEmpty() throws InvalidArgumentException {

		if( this.isNull() )
			return true ;
		 return getTypeMethods().isEmpty( mValue );
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

	public boolean isString() {
		if( mValue == null )
			return false ;
		if( mValue instanceof String )
			return true ;

		if( ! (mValue instanceof XdmItem ))
			return false ;


		ValueRepresentation<? extends Item> value = asXdmItem().getUnderlyingValue();
		boolean isString = ( value instanceof net.sf.saxon.value.StringValue ) || ( value instanceof NodeInfo && ((NodeInfo)value).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isString ;


	}

	public boolean isXdmNode() {
		return 
				mValue != null && 
				typeFamily() == TypeFamily.XDM &&
				mValue instanceof XdmNode ;
	}
	
	// Never ask for an Xdm Value 
	public boolean isXdmItem() {
	 
		return 
				mValue != null && 
				typeFamily() == TypeFamily.XDM &&
				mValue instanceof XdmItem  ;
	}



	/*
	 * Returns true if the class is an Integer like class
	 */

	public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException  
	{
		if( mValue == null )
			return ;
		ITypeFamily tf = typeFamilyInstance();
		if( tf != null )
			tf.serialize( mValue , out , opts );
		else 
			out.write( toByteArray(opts) );
		out.flush();

	}

	public XValue shift(int n) {
		if( mValue == null )
			return this ;
		XdmValue value = toXdmValue();
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
		/*
		 * Check for Java boolean and integer values
		 */
		if( mValue == null )
			return false ;
		if( isSequence() )
		  return ! isEmptySequence() ;

		try {
			if( isJson() )
				return asJson().asBoolean();
		}
		catch( Exception e ){
			Util.wrapException(e,InvalidArgumentException.class);	
			return false ; // SNH 
		}


		if( ! (mValue instanceof XdmValue ) ){
		  
		  if( mValue instanceof String )
		    return Util.parseBoolean((String)mValue);

			if( JavaUtils.canConvertClass( mValue.getClass() , Boolean.class ) >= 0 )
				return ((Boolean)convert(Boolean.class)).booleanValue() ;

			if( JavaUtils.canConvertClass( mValue.getClass() , Long.class ) >= 0 )
				return ((Long)convert(Long.class)).longValue() != 0L ;

			return false;
		}

		XdmValue value = (XdmValue) mValue;
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
					return XMLUtils.toByteArray( toXdmValue(), opts);

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
			return getTypeMethods().asString( mValue );
		} catch (Exception e) {
			mLogger.debug("Exception in XValue.toString()",e);
		}
		return "";
	}
	public ITypeFamily typeFamilyInstance() {
		return  XTypeUtils.getFamilyInstance(typeFamily());
	}

	public TypeFamily  typeFamily()
	{
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
	public static List<XValue> emptyList()
    {
	   return Collections.emptyList();
    }
	public List<XValue> asXList()
    {
		// TODO: inspect contents
		return Collections.singletonList(this);
    }
	
  public IMethods getTypeMethods()
  {
    return typeFamilyInstance();
  }
  @Override
  public Iterator<XValue> iterator()
  {
    if( isNull() || isEmptySequence() )
      return Collections.emptyIterator();
    if( isSequence()  )
      return ((XValueSequence)mValue).iterator() ;
     return Util.singletonIterator( this );
    
    
  }
  
  public XdmItem toXdmItem()
  {
    if( isXdmItem() )
      return asXdmItem() ;
    
    return XMLUtils.toXdmItem(mValue);
    
  }
    
  
  
  /*
   * Prefered way create an XValue from an object - does NOT unwrap an existing XValue
   */
  public static XValue newInstance(Object obj)
  {
    if( obj  == null )
      return  nullValue();
    
    return new XValue( obj );
  
  }
  
  /*
   * Prefered way create an XValue from an object
   * Will cast directly to XValue if its a known type
   * otherwise will create a wrapper
   * 
   */
  public static XValue asXValue(Object obj)
  {
    if( obj  == null )
      return  nullValue();
    if( obj instanceof XValue )
      return (XValue) obj ;

    return new XValue( obj );
  
  }
  public boolean isSequence()
  {
    return isInstanceOf(XValueSequence.class);
  }
  
  public EnumSet<XVarFlag> typeFlags() {
    EnumSet<XVarFlag> flags  = EnumSet.noneOf(XVarFlag.class);
    
    if( isSequence() ) 
      flags = XVariable.XVAR_SEQUENCE ;
    
    if( isInstanceOf( XValueProperty.class )|| isInstanceOf( XValueMap.class ) 
        || isInstanceOf( XValuePropertyList.class ) )
      flags = Util.withEnumsAdded( flags , XVarFlag.NAMED_INDEX );
    if( isInstanceOf( XValuePropertyList.class ) ||
        isInstanceOf( XValueList.class ) ||
        isInstanceOf( XValueArray.class ) )
      flags = Util.withEnumsAdded( flags , XVarFlag.POSITIONAL_INDEX , XVarFlag.LIST );
      return flags ;
    
    
  }

  public String javaTypeName()
  {
    if( mValue == null)
      return "null";
    return mValue.getClass().getName();
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
