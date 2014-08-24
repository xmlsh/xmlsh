package org.xmlsh.types;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueList;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public final class XDMTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  static final XDMTypeFamily _instance = new XDMTypeFamily();

  @Override
  public boolean isClassOfFamily(Class<?> cls)
  {
    return XdmValue.class.isAssignableFrom(cls) || ValueRepresentation.class.isAssignableFrom(cls) ||
        QName.class.isAssignableFrom(cls);
  }

  @Override
  public boolean isInstanceOfFamily(Object obj)
  {
    return obj instanceof XdmValue || obj instanceof ValueRepresentation || obj instanceof QName;

  }

  @Override
  public TypeFamily typeFamily()
  {
    return TypeFamily.XDM;
  }

    @Override
    public XValue append(Object obj, XValue xvalue)
    {
      assert( obj != null );
      assert (obj instanceof XdmItem);
      
      if(xvalue == null)
        return newXValue(obj);
      
      return new XValue( new XValueSequence( newXValue(obj) , xvalue ) );

    }

    @Override
    public String asString(Object obj)
    {  
      if(obj == null)
        return "";
      assert( obj != null );
      assert (obj instanceof XdmItem);
      if(obj instanceof XdmItem)
        try {
          return new String(XMLUtils.toByteArray((XdmItem) obj, SerializeOpts.defaultOpts),
            SerializeOpts.defaultOpts.getOutput_xml_encoding());
        } catch (SaxonApiException | IOException e) {
          mLogger.warn("Exception serializing XDM value", e);
        }
      else 
        return obj.toString();
      return "";

    }

    @Override
    public int getSize(Object obj)
    {
      assert( obj != null );
      assert (obj instanceof XdmItem);
      // return ((XdmItem) XdmItem).size();
      return 1;
    }

    // Named index XValue
    @Override
    public XValue getXValue(Object obj, String ind) throws InvalidArgumentException
    {
      if( obj == null )
        return _nullValue;
      assert( obj != null );
      assert( obj instanceof XdmItem );

    throw new InvalidArgumentException("Invalid named index for XDM Value:" + ind);

    }

    @Override
    public void serialize(Object obj, OutputStream out, SerializeOpts opts) throws IOException
    {

      assert( obj != null );
      assert( obj instanceof XdmItem );

      if(obj instanceof XdmItem) {
        XdmItem xv = (XdmItem) obj;
        if(!XMLUtils.isAtomic(xv)) {
          try {
            XMLUtils.serialize(xv, out, opts);
          } catch (SaxonApiException e) {
            Util.wrapIOException(e);
          }
          return;
        }
        out.write(xv.toString().getBytes(opts.getOutput_text_encoding()));

        return ;
      }

     assert(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmlsh.types.AbstractMethods#simpleTypeName(java.lang.Object)
     */
    @Override
    public String simpleTypeName(Object obj)
    {
      if( obj == null )
        return "null";
      
      assert( obj instanceof XdmItem );
      
      if( obj instanceof XdmItem )
        return XMLUtils.simpleTypeName((XdmItem) obj );
      else
        return JavaUtils.simpleTypeName(obj);
    
    }

    @Override
    public boolean isEmpty(Object obj)
    {
      assert( obj != null );
      assert( obj instanceof XdmItem );
      // Non-XdmValues not considered atomic.
      if(!(obj instanceof XdmItem))
        return false;
      return ((XdmItem) obj).size() == 0;
    }

    @Override
    public XValue setXValue(XValue xobj, String ind, XValue value) throws CoreException
    {
      throw new UnimplementedException("Set indexed value not supported on XDM types");
    }

    @Override
    public List<XValue> getXValues(Object obj)
    {
      assert( obj != null );
      assert( obj instanceof XdmItem );
      if(obj == null)
        return XValue.emptyList();
      XdmItem item = (XdmItem) obj;
      
      return XMLUtils.toXValueList( item );
    }

    @Override
    public boolean isAtomic(Object value)
    {
      assert( value != null );
      assert( value instanceof XdmItem );
      if(value == null)
        return true;
      if(!(value instanceof XdmValue))
        return false;

      return XMLUtils.isAtomic((XdmValue) value);
    }

    @Override
    public XValue getXValue(Object obj)
    {
      assert( obj != null );
      assert( obj instanceof XdmItem );
      if(obj == null)
        return _nullValue;
      assert( obj instanceof XdmItem );
      XdmItem v = (XdmItem) obj;
      return newXValue(v);

    }

    @Override
    public XValue getXValue(Object obj, int index) throws CoreException
    {
      if(obj == null)
        return _nullValue;
      assert (obj instanceof XdmValue);
      XdmValue v = (XdmValue) obj;

      if(index < 0 || index >= v.size())
        throw new InvalidArgumentException("Invalid index for sequence");
      return newXValue(v.itemAt(index));
    }

    @Override
    public XValue setXValue(XValue obj, int index, XValue value) throws  UnimplementedException
    {
      throw new UnimplementedException("Invalid operation: setXValue by index");

    }

  private static Logger mLogger = LogManager.getLogger(XDMTypeFamily.class);

  @Override
  protected XValue newXValue(Object obj)
  {
      return new XValue(TypeFamily.XDM, obj);
  }


}
