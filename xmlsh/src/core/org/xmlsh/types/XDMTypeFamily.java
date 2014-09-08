package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueSequence;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.S9Util;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

public final class XDMTypeFamily extends AbstractTypeFamily implements ITypeFamily
{
  private static final XDMTypeFamily _instance = new XDMTypeFamily();
  private static final Object _nullValue = null;



  private static Logger mLogger = LogManager.getLogger();

  @Override
  public boolean isClassOfFamily(Class<?> cls)
  {
    assert( cls != null );
    if( cls == null ) 
      return false;
    return XdmItem.class.isAssignableFrom(cls) || ValueRepresentation.class.isAssignableFrom(cls) ||
        QName.class.isAssignableFrom(cls) ;
  }

  @Override
  public boolean isInstanceOfFamily(Object obj)
  {
    if( obj == null )
      return true ;

    if(  obj instanceof XdmItem || obj instanceof ValueRepresentation || obj instanceof QName  || obj instanceof URI )
      return true ;
    return false ;

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
      assert (obj instanceof XdmItem );
      
      if(xvalue == null)
        return XValue.newXValue( TypeFamily.XDM , obj);
      
      return XValue.newXValue(new XValueSequence( getXValue(obj), xvalue ));

    }

    @Override
    public String asString(Object obj)
    {  
      if(obj == null)
        return "";
      assert( obj != null );
      assert (obj instanceof XdmItem );
      
      if(obj instanceof XdmItem) {
        try {
          return new String(XMLUtils.toByteArray((XdmItem) obj, SerializeOpts.defaultOpts),
            SerializeOpts.defaultOpts.getOutput_xml_encoding());
        } catch (SaxonApiException | IOException e) {
          mLogger.warn("Exception serializing XDM value", e);
          return "";
        }
      }
      else 
        return  obj.toString();


    }

    @Override
    public int getSize(Object obj)
    {
      assert( obj != null );
      assert (obj instanceof XdmItem);
      return ((XdmItem)obj).size();
    }

    // Named index XValue
    @Override
    public XValue getXValue(Object obj, String ind) throws InvalidArgumentException
    {
      if( obj == null )
        return nullXValue();
      
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
      if(value == null)
        return true;
      if(!(value instanceof XdmItem))
        return false;

      return XMLUtils.isAtomic((XdmItem) value);
    }
    

    @Override
    public XValue getXValue(Object obj)
    {
    
       if( obj == null )
           return XTypeFamily.getInstance().nullXValue();
       
       if( obj instanceof XdmValue && ! (obj instanceof XdmItem) ) {
           XdmValue v = (XdmValue) obj ;
           v = XMLUtils.simplify(v);
           
           switch( v.size() ){
           case 0:
              return XValue.empytSequence();
           case 1:
             obj = v.itemAt(0);
           default:
             return XValue.newXValue(XMLUtils.toXValueList( v ));
           }
         }
       
       if( obj instanceof XdmItem ) 
         assert( ( (XdmItem) obj ).size() <= 1 );
       else       
       if( obj instanceof QName )
         obj = new XdmAtomicValue( (QName) obj );
       else
       if( obj instanceof String )
         obj = new XdmAtomicValue( (String) obj );
       else
       if( obj instanceof URI )
         obj = new XdmAtomicValue( (URI) obj );
       else
       if( obj instanceof Item<?> )
         obj = S9Util.wrapItem((Item<?>)obj);
       else
         return XValue.newXValue(obj);

      return XValue.newXValue(this,obj,false);
    }

    

    @Override
    public XValue getXValue(Object obj, int index) throws CoreException
    {
      if(obj == null)
        return nullXValue();
      assert (obj instanceof XdmItem);
      XdmItem v = (XdmItem) obj;

      if(index < 0 || index >= v.size())
        throw new InvalidArgumentException("Invalid index for sequence");
      return XValue.newXValue(this,v.itemAt(index),false);
    }

    @Override
    public XValue setXValue(XValue obj, int index, XValue value) throws  UnimplementedException
    {
      throw new UnimplementedException("Invalid operation: setXValue by index");

    }

  @Override
  public Object nullValue()
  {
    return _nullValue;
  }

  @Override
  public XValue nullXValue()
  {
   
   return XValue.newXValue(TypeFamily.XDM, _nullValue);
  }

  public static XDMTypeFamily getInstance()
  {
    return _instance;
  }

@Override
public boolean hasKey(Object obj, String key) {
	if( XMLUtils.isXdmElement(obj)){

		XdmNode element = XMLUtils.asXdmNode(obj);

		if( element.getAttributeValue( new QName(key) ) != null )
			return true ;
	}
	return false ;
		
}


}
