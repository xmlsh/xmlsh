/**
 * $Id$
 * $Date$
 * 
 */

package org.xmlsh.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
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
import net.sf.saxon.value.DecimalValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.FloatValue;
import net.sf.saxon.value.IntegerValue;

import org.apache.logging.log4j.Logger;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.IMethods;
import org.xmlsh.types.ITypeFamily;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.types.xtypes.IXValueList;
import org.xmlsh.types.xtypes.IXValueMap;
import org.xmlsh.types.xtypes.IXValueSequence;
import org.xmlsh.types.xtypes.XValueProperty;
import org.xmlsh.types.xtypes.XValuePropertyList;
import org.xmlsh.types.xtypes.XValueSequence;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * A XValue is a single value or a sequence of 0 or more values
 */
public class XValue implements Iterable<XValue>
{

  private static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger(XValue.class);

  private TypeFamily mTypeFamily = null; // default null defers to a runtime evaluatiion
  private Object mValue;		// String , XdmItem , Object , List<XValue> ...

  private XValue(BigDecimal n)
  {
    this(TypeFamily.XDM, new XdmAtomicValue(n));
  }
  
  private XValue(boolean n)
  {
    this(TypeFamily.XDM, new XdmAtomicValue(n));
  }

  private XValue(int n)
  {
    this(TypeFamily.XDM, new XdmAtomicValue(n));
  }

  private XValue(Item item)
  {
    this(S9Util.wrapItem(item));
  }

  private XValue(IXValueList list)
  {
    this(TypeFamily.XTYPE, list);
  }

  private XValue(IXValueMap map)
  {
    this(TypeFamily.XTYPE, map);
  }

  /*
   * Create an atomic string (xs:string)
   */

  private XValue(IXValueSequence<?> seq)
  {
    this(TypeFamily.XTYPE, seq);

  }

  /*
   * Create an XValue by combining a list of XValue objects into a single XValue as a sequence
   * null or empty list becomes an empty sequence
   */
  private XValue(List<XValue> args)
  {

      
    if(args == null || args.isEmpty()) {
      _initSequence(null);

    }
    else if(args.size() == 1) {
      mTypeFamily = args.get(0).mTypeFamily;
      mValue = args.get(0).mValue;
    }
    else {

      _initSequence(new XValueSequence(args));
    }
    _init();

  }

  private XValue(long n)
  {
    this(TypeFamily.XDM, new XdmAtomicValue(n));
  }


  private XValue(String s)
  {
    this(TypeFamily.JAVA, s);
  }

  private XValue(String value, ItemType type) throws SaxonApiException
  {
    this(TypeFamily.XDM, new XdmAtomicValue(value, type));
  }

  // Create XValue from an array of strings
  private XValue(String[] astring)
  {

    this(new XValueSequence(Util.toXValueList(astring)));
  }

  private XValue(TypeFamily family, Object obj)
  {
    mTypeFamily = family;
    mValue = obj;
    _init();

  }

  /*
   * Create an XValue from an XdmValue
   */
  protected XValue(XdmValue v)
  {
    this(TypeFamily.XDM, v == null ? null : XMLUtils.simplify(v));
  }

  private XValue(XValueProperty prop)
  {
    this(TypeFamily.XTYPE, prop);
  }

  private XValue(XValuePropertyList plist)
  {
    this(TypeFamily.XTYPE, plist);
  }

  public XValue()
  {
    mTypeFamily = TypeFamily.XTYPE;
    mValue = null;
    _init();

  }

  public XValue(TypeFamily family)
  {
    mTypeFamily = family;
    mValue = null;
    _init();

  }

  public static List<XValue> emptyList()
  {
    return Collections.emptyList();
  }

  public static XValue empytSequence()
  {

    return new XValue(TypeFamily.XTYPE, XValueSequence.emptySequence());

  }


  public static XValue newXValue(BigDecimal n)
  {
    return new XValue(n);
  }

  /*
   * 
   * public XValue append(XValue v) {
   * return append( v.asXdmValue() );
   * 
   * }
   */

  public static XValue newXValue(boolean n)
  {
    return new XValue(n);
  }

  public static XValue newXValue(int n)
  {
    return new XValue(n);
  }

  public static XValue newXValue(Item item)
  {
    return new XValue(item);
  }

  public static XValue newXValue(ITypeFamily type, Object value, boolean convert) throws InvalidArgumentException
  {
    if(type == null)
      return newXValue(value);
    if(convert && !type.isInstanceOfFamily(value))
      return newXValue(type.typeFamily(), value);
    return new XValue(type.typeFamily(), value);

  }

  public static XValue newXValue(IXValueList list)
  {
    return new XValue(list);
  }

  public static XValue newXValue(IXValueMap map)
  {
    return new XValue(map);
  }

  public static XValue newXValue(IXValueSequence<?> seq)
  {
    return new XValue(seq);
  }

  public static XValue newXValue(List<XValue> values)
  {
    return new XValue(values);
  }

  public static XValue newXValue(long n)
  {
    return new XValue(n);
  }

  public static XValue newXValue(Object obj) throws InvalidArgumentException
  {
    if(obj == null)
      return nullValue();
    if(obj instanceof XValue)
      return (XValue) obj;

    TypeFamily tf = XTypeUtils.inferFamily(obj);
    return XTypeUtils.getInstance(tf).getXValue(obj);

  }

  public static XValue newXValue(String s)
  {
    return new XValue(s);
  }

  public static XValue newXValue(String value, ItemType type) throws SaxonApiException
  {
    return new XValue(value, type);
  }

  public static XValue newXValue(String[] astring)
  {
    return new XValue(astring);
  }

  public static XValue newXValue(TypeFamily type, Object value) throws InvalidArgumentException
  {
    if(type == null)
      return newXValue(value);

    return XTypeUtils.getInstance(type).getXValue(value);

  }

  public static XValue newXValue(XdmValue v)
  {
    return new XValue(v);
  }

  public static XValue newXValue(XValueProperty prop)
  {
    return new XValue(prop);
  }

  /*
   * Return a new XValue which is an appending of "this" value
   * and another XdmValue as a sequence
   * If This is null or the empty sequence then return the value
   */

  public static XValue newXValue(XValuePropertyList plist)
  {
    return new XValue(plist);
  }

  public static XValue nullValue()
  {
    return new XValue(TypeFamily.XTYPE, null);
  }

  public static XValue nullValue(TypeFamily xtype)
  {
    return XTypeUtils.getInstance(xtype).nullXValue();
  }

  private void _init()
  {
    if(mTypeFamily == null)
      mTypeFamily = XTypeUtils.inferFamily(mValue);

    if(mValue != null)
      assert (!mValue.getClass().isArray());
    assert (XTypeUtils.getInstance(mTypeFamily).isInstanceOfFamily(mValue));

  }

  private void _initSequence(IXValueSequence<?> seq)
  {
    mTypeFamily = TypeFamily.XTYPE;
    mValue = seq == null ? XValueSequence.emptySequence() : seq;
    _init();

  }

  public XValue append(XdmValue value) throws InvalidArgumentException
  {

    if(mValue == null)
      return XValue.newXValue(value);

    if(value == null)
      return this;

    /*
     * List<XdmItem> items = new ArrayList<XdmItem>();
     * for (XdmItem item : asXdmValue())
     * items.add(item);
     * 
     * for( XdmItem item : xvalue )
     * items.add(item);
     * 
     * return XValue.asXValue(new XdmValue(items));
     */
    return append(XValue.newXValue(value));
  }

  public XValue append(XValue v) throws InvalidArgumentException
  {
    if(mValue == null)
      return v;

    return getTypeMethods().append(mValue, v);

  }

  public JsonNode asJson() throws InvalidArgumentException
  {
    if(mValue == null || mValue instanceof JsonNode)
      return (JsonNode) mValue;

    return JSONUtils.toJsonNode(toString());
  }

  public NodeInfo asNodeInfo() throws InvalidArgumentException
  {

    return asXdmNode().getUnderlyingNode();
  }

  @JsonValue
  public Object asObject()
  {
    return mValue;
  }

  /*
   * Returns true if the class is an Integer like class
   */

  public QName asQName(Shell shell)
  {
    if(mValue == null)
      return null;

    if(mValue instanceof QName)
      return (QName) mValue;
    String qn = null;
    if(mValue instanceof XdmAtomicValue) {
      Object v = ((XdmAtomicValue) mValue).getValue();
      if(v instanceof QName)
        return (QName) v;
      qn = v.toString();
    }
    if(qn == null && !isAtomic())
      return null;
    if(qn == null)
      qn = mValue.toString();
    if(qn.startsWith("{") || qn.indexOf(':') <= 0)
      return Util.qnameFromClarkName(mValue.toString());

    StringPair pair = new StringPair(qn, ':');

    String uri = shell.getEnv().getNamespaces().get(pair.getLeft());
    return new QName(pair.getLeft(), uri, pair.getRight());

  }

  public Source asSource() throws InvalidArgumentException
  {
    return asXdmNode().asSource();

  }

  public List<String> asStringList()
  {
    if(isNull())
      return null;
    List<String> list = new ArrayList<String>();
    for (XValue v : this)
      list.add(v.toString());
    return list;
  }

  public <T> T asType(Class<T> cls)
  {
    return cls.cast(mValue);
  }

  public XdmItem asXdmItem()
  {
    if(mValue instanceof XdmItem)
      return (XdmItem) mValue;
    return null;
    // return XMLUtils.asXdmItem( asXdmValue() );

  }

  public XdmNode asXdmNode() throws InvalidArgumentException
  {
    XdmItem item = asXdmItem();
    if(item instanceof XdmNode)
      return (XdmNode) item;
    else throw new InvalidArgumentException("Value is not a Node");
  }

  public XdmSequenceIterator asXdmSequenceIterator() throws InvalidArgumentException
  {
    XdmValue value = toXdmValue();
    if(value == null)
      return null;

    return value.iterator();
  }

  public List<XValue> asXList()
  {
    // TODO: inspect contents
    return Collections.singletonList(this);
  }

  public int canConvert(Class<?> c) throws InvalidArgumentException, UnexpectedException
  {
    Object value = mValue;
    if(value == null)
      return -1;

    Class<? extends Object> vclass = value.getClass();

    if(isSequence()) {
      if(c.isAssignableFrom(XdmValue.class)) {
        if(isEmptySequence())
          return 1;

        int max = -1;
        for (XValue v : this) {
          int ic = v.canConvert(c);
          if(ic < 0)
            break;
          if(ic >= max)
            max = ic;
        }
        if(max >= 0)
          return max;
      }
    }

    // This can be very heavy weight
    int ret = JavaUtils.canConvertClass(vclass, c);
    if(ret >= 0)
      return ret;

    // Is this a Xdm and want to convert to something else
    // SNH : in JavaUtils
    if(value instanceof XdmValue) {

      value = getJavaNative();
      if(value == null)
        return -1;
      vclass = value.getClass();
      ret = JavaUtils.canConvertClass(vclass, c);
    }

    return ret - 1;

  }

  public Object convert(Class<?> c) throws InvalidArgumentException
  {

    try {
      Object value = mValue;
      if(value == null)
        return null;

      if(c.isAssignableFrom(value.getClass()))
        return value;

      if(c.isInstance(value))
        return c.cast(value);

      if(c.isAssignableFrom(XdmValue.class)) {
        if(isSequence()) {
          if(isEmptySequence())
            return XMLUtils.emptySequence();
          else return XMLUtils.toXdmValue(this);
        }
      }

      if(JavaUtils.canConvertClass(value.getClass(), c) >= 0) {
        Object obj = JavaUtils.convert(value, c);
        if(obj != null)
          return obj;
      }

      if(value instanceof XdmValue)
        value = getJavaNative();

      return JavaUtils.convert(value, c);
    } catch (Exception e) {
      Util.wrapException(e, InvalidArgumentException.class);

      return null; // SNH
    }
  }

  @Override
  public boolean equals(Object that)
  {
    if(this == that)
      return true;

    return super.equals(that);
  }

  /*
   * Type Family and 2.0.x extensions
   */

  public boolean equals(String s)
  {
    return isAtomic() && toString().equals(s);
  }

  public boolean equals(XValue that)
  {
    if(this == that)
      return true;

    if(isAtomic() && that.isAtomic())
      return toString().equals(that.toString());

    if(mValue != null && that.mValue != null)
      return mValue.equals(that.mValue);
    return false;
  }

  // somewhat bogus
  public Object getJavaNative() throws InvalidArgumentException, UnexpectedException
  {
    try {
      if(mValue == null)
        return null;

      if(isJson())
        return JSONUtils.asJavaNative(asJson());

      // Already a java type
      if(!(mValue instanceof XdmValue))
        return mValue;

      XdmValue xv = (XdmValue) mValue;

      Sequence value = xv.getUnderlyingValue();
      // Special case for text nodes treat as String
      if(value instanceof NodeInfo && ((NodeInfo) value).getNodeKind() == net.sf.saxon.type.Type.TEXT)
        return ((NodeInfo)value).getStringValue();

      if(!(value instanceof AtomicValue))
        return value;

      AtomicValue av = (AtomicValue) value;
      Object java = SequenceTool.convertToJava(av);

      return java;
    } catch (Exception e) {
      Util.wrapException("Exception getting java native value", e, UnexpectedException.class);

    }
    // SNH

    return null;
  }

  public IMethods getTypeMethods()
  {
    return typeFamilyInstance();
  }

  @Override
  public int hashCode()
  {
    if(mValue == null)
      return 0;
    return mValue.hashCode();
  }

  public boolean isAtomic()
  {
    if(mValue == null)
      return false;

    return typeFamilyInstance().isAtomic(mValue);

  }

  public boolean isEmpty() 
  {

    if(isNull())
      return true;
    return getTypeMethods().isEmpty(mValue);
  }

  public boolean isEmptySequence()
  {
    return mValue instanceof IXValueSequence && ((IXValueSequence<?>) mValue).isEmpty();

  }

  public boolean isInstanceOf(Class<?> cls)
  {
	ClassLoader cl1 = getClass().getClassLoader();
	ClassLoader cl2 = cls.getClassLoader();
	ClassLoader cl3 = mValue.getClass().getClassLoader();
	boolean b1 = mValue != null;
	boolean b2 = b1 
			&& cls.isAssignableFrom(mValue.getClass());
    return b1 && b2 ;
  }
   
  public <T> T asInstanceOf( Class<T> cls ){
    return  cls.cast( mValue ); 
  }
  
  
  public boolean isJson()
  {
    if(mTypeFamily == TypeFamily.JSON)
      return true;
    return mValue != null && XTypeUtils.getInstance(TypeFamily.JSON).isInstanceOfFamily(mValue);

  }

  public boolean isNull()
  {
    return mValue == null ;
  }

  /*
   * Prefered way create an XValue from an object
   * Will cast directly to XValue if its a known type
   * otherwise will create a wrapper
   */

  public boolean isSequence()
  {
    return mValue instanceof IXValueSequence;
  }
  public boolean isContainer() 
  {
      return getTypeMethods().isContainer(mValue);
  }
  
  

  public boolean isString()
  {
    if(mValue == null)
      return false;
    if(mValue instanceof String)
      return true;

    if(!(mValue instanceof XdmItem))
      return false;

    Sequence value = asXdmItem().getUnderlyingValue();
    boolean isString = (value instanceof net.sf.saxon.value.StringValue) ||
        (value instanceof NodeInfo && ((NodeInfo) value).getNodeKind() == net.sf.saxon.type.Type.TEXT);
    return isString;

  }

  public boolean isTypeFamily(TypeFamily family)
  {
    return typeFamily() == family;
  }

  // Never ask for an Xdm Value
  public boolean isXdmItem()
  {

    return mValue != null && typeFamily() == TypeFamily.XDM && mValue instanceof XdmItem;
  }

  public boolean isXdmNode()
  {
    return mValue != null && typeFamily() == TypeFamily.XDM && mValue instanceof XdmNode;
  }

  public boolean isXType()
  {
    return isTypeFamily(TypeFamily.XTYPE);
  }

  @Override
  public Iterator<XValue> iterator()
  {
    if(isNull() || isEmptySequence())
      return Collections.emptyIterator();
    if(isSequence())
      return ((IXValueSequence<?>) mValue).iterator();
    return Util.singletonIterator(this);

  }

  public String javaTypeName()
  {
    if(mValue == null)
      return "null";
    return mValue.getClass().getName();
  }

  public void serialize(OutputStream out, SerializeOpts opts) throws IOException, InvalidArgumentException
  {
    if(mValue == null)
      return;
    ITypeFamily tf = typeFamilyInstance();
    if(tf != null)
      tf.serialize(mValue, out, opts);
    else out.write(toByteArray(opts));
    out.flush();

  }

  public XValue shift(int n) throws InvalidArgumentException
  {
    if(mValue == null)
      return this;

    if(!(mValue instanceof IXValueSequence))
      return this;
    IXValueSequence<?> seq = (IXValueSequence<?>) mValue;

    if(seq.isEmpty())
      return this;
    if(seq.size() <= n)
      return empytSequence();

    return XValue.newXValue(seq.subSequence(n));
  }

  public BigDecimal toBigDecimal() throws XPathException
  {
    if(mValue == null)
      return null;

    if(!isAtomic())
      return null;

    if(mValue instanceof AtomicValue) {
      AtomicValue av = (AtomicValue) mValue;
      if(av instanceof DecimalValue)
        return ((DecimalValue) av).getDecimalValue();
      if(av instanceof DoubleValue)
        return BigDecimal.valueOf(((DoubleValue) av).getDoubleValue());
      if(av instanceof FloatValue)
        return BigDecimal.valueOf(((FloatValue) av).getDoubleValue());
      if(av instanceof IntegerValue)
        return BigDecimal.valueOf(((IntegerValue) av).longValue());

    }
    return BigDecimal.valueOf(Double.valueOf(mValue.toString()));
  }

  public boolean toBoolean() throws InvalidArgumentException, UnexpectedException 
  {
    /*
     * Check for Java boolean and integer values
     */
    if(mValue == null)
      return false;
    if(isSequence())
      return !isEmptySequence();

    try {
      if(isJson())
        return asJson().asBoolean();
    } catch (Exception e) {
      Util.wrapException(e, InvalidArgumentException.class);
      return false; // SNH
    }

    if(!(mValue instanceof XdmValue)) {

      if(mValue instanceof String)
        return Util.parseBoolean((String) mValue);

      if(JavaUtils.canConvertClass(mValue.getClass(), Boolean.class) >= 0)
        return ((Boolean) convert(Boolean.class)).booleanValue();

      if(JavaUtils.canConvertClass(mValue.getClass(), Long.class) >= 0)
        return ((Long) convert(Long.class)).longValue() != 0L;

      return false;
    }

    XdmValue value = (XdmValue) mValue;
    if(value == null)
      return false;

    // Sequence of > 1 length
    if(value.size() > 1)
      return true;
    // Sequence of 0 length
    if(value.size() == 0)
      return false;

    Processor processor = Shell.getProcessor();

    XPathCompiler compiler = processor.newXPathCompiler();

    try {
      XPathExecutable exec = compiler.compile(".");

      XPathSelector eval = exec.load();
      eval.setContextItem(value.itemAt(0));
      return eval.effectiveBooleanValue();

    } catch (Exception e) {
      throw new UnexpectedException("Exception evaluating boolean xpath");
    }
  }

  public byte[] toByteArray(SerializeOpts opts) throws IOException
  {
    try {
      if(mValue != null) {
        switch (typeFamily()) {
        case JAVA:
        case XTYPE:
          return JavaUtils.toByteArray(mValue, opts);
        case JSON:
          return JSONUtils.toByteArray(asJson(), opts);
        case XDM:
          return XMLUtils.toByteArray(toXdmValue(), opts);
        }

      }
    } catch (JsonGenerationException | JsonMappingException | UnsupportedEncodingException | InvalidArgumentException
      | SaxonApiException e) {
      Util.wrapIOException(e);
    }

    return null;
  }

  public double toDouble()
  {
    if(mValue == null)
      return 0.;

    if(!isAtomic())
      return 0.;

    if(mValue instanceof DoubleValue)
      return ((DoubleValue) mValue).getDoubleValue();

    return Double.parseDouble(toString());

  }

  public int toInt() throws XPathException
  {
    return (int) toLong();
  }

  public long toLong() throws XPathException
  {
    if(mValue == null)
      return 0;

    if(!isAtomic())
      return -1;

    if(mValue instanceof IntegerValue)
      return ((IntegerValue) mValue).longValue();

    return Long.parseLong(toString());
  }

  @Override
  public String toString()
  {
    try {
      return getTypeMethods().asString(mValue);
    } catch (Exception e) {
      mLogger.debug("Exception in XValue.toString()", e);
    }
    return "";
  }

  public XdmItem toXdmItem() throws InvalidArgumentException
  {
    if(isXdmItem())
      return asXdmItem();

    return XMLUtils.toXdmItem(mValue);

  }

  public XdmValue toXdmValue() throws InvalidArgumentException
  {
    if(isSequence())
      return XMLUtils.toXdmValue(((XValueSequence) mValue));
    else
    if( isContainer() ){
        return XMLUtils.toXdmValue( getXValues() );
        
    }

    return XMLUtils.toXdmValue(mValue);

  }

  public TypeFamily typeFamily()
  {
    return mTypeFamily;
  }

  public ITypeFamily typeFamilyInstance()
  {
    return XTypeUtils.getInstance(typeFamily());
  }

  public XValue xpath(Shell shell, String expr) throws UnexpectedException
  {
    if(mValue == null || !(mValue instanceof XdmValue))
      return null;

    Processor processor = Shell.getProcessor();

    XPathCompiler compiler = processor.newXPathCompiler();

    Namespaces ns = shell.getEnv().getNamespaces();
    if(ns != null) {
      for (String prefix : ns.keySet()) {
        String uri = ns.get(prefix);
        compiler.declareNamespace(prefix, uri);

      }

    }

    try {
      XPathExecutable exec = compiler.compile(expr);

      XPathSelector eval = exec.load();
      eval.setContextItem(((XdmValue) mValue).itemAt(0));
      return XValue.newXValue(TypeFamily.XDM, eval.evaluate());

    } catch (Exception e) {
      throw new UnexpectedException("Exception evaluating xpath: " + expr, e);
    }
  }

  /*
   * StrReplace based replacement of values
   * 
   */
  public XValue replace( XStringSubstituter subst )
  {
      if( isAtomic() ){
          StringBuilder sb = new StringBuilder( toString() );
          if( subst.replaceIn(sb) )
             return XValue.newXValue(sb.toString());
      }
      return this ;
  }

    
    public XValue getNamedValue( String key ) throws CoreException {
    	
    	return typeFamilyInstance().getXValue( mValue, key );
    
    }
    
    public List<XValue> getXValues() throws InvalidArgumentException {
    	if( mValue == null )
    		return emptyList();
    	return getTypeMethods().getXValues( mValue  ); 
    }
    
    /*
     * A reasonable version of toString that wont be a bazillion bytes
     */
    public String describe()
    {
       if( isNull() )
         return "null";
    
       return typeFamilyInstance().simpleTypeName(mValue);
    
    }

}
//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
