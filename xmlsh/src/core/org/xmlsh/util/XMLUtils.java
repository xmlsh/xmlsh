package org.xmlsh.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.Value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.ITypeConverter;
import org.xmlsh.types.TypeFamily;

public class XMLUtils
{
	private static Logger mLogger = LogManager.getLogger();


  public static byte[]   toByteArray(XdmValue xdm , SerializeOpts opts ) throws SaxonApiException, IOException
	{
		if( xdm != null ){
			if( isAtomic(xdm) )
				return xdm.toString().getBytes(opts.getOutput_text_encoding());
			else
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				serialize( xdm ,  out , opts );
				return out.toByteArray();
			}
		}
		return null;
	}


	public static boolean isAtomic(XdmValue value) {
		if(  value == null )  // strange but null is atomic for this 
		return true ;

		@SuppressWarnings("rawtypes")
		ValueRepresentation<? extends Item> item = value.getUnderlyingValue();
		boolean isAtom = ( item instanceof AtomicValue ) || 
				( item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
		return isAtom;
	}


	public static XdmItem asXdmItem(XdmValue value)
	{
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
	public static XdmValue asXdmValue(Iterable<XdmItem> list) {
	
		return simplify( new XdmValue(list) );
		
	}
	public static XdmValue asXdmValue(Object obj) throws InvalidArgumentException
	{
		if( ! (obj instanceof XdmValue) )
			return null ;
		return (XdmValue) obj ;
	}
	

	public static XdmNode asXdmNode(Object obj) throws InvalidArgumentException
	{
		if( ! (obj instanceof XdmValue) )
			return null ;
		XdmItem item = asXdmItem((XdmValue)obj );
		if( item instanceof XdmNode )
			return (XdmNode) item ;
		else
			throw new InvalidArgumentException("Value is not a Node");
	}

	public static void serialize( XdmValue value , OutputStream out, SerializeOpts opt) throws SaxonApiException, IOException
	{
		Serializer ser = Util.getSerializer(opt);
		ser.setOutputStream( out );
		Util.writeXdmValue( value , ser );
		ser.close();;
		out.flush();
	}


    public static XdmValue emptySequence() {
       return  XdmEmptySequence.getInstance();
    }
    
    
    
    public static XdmValue toXdmValue( Object obj ) throws InvalidArgumentException {
      return JavaUtils.convert(obj, XdmValue.class);
    }
    
    public static XdmItem toXdmItem( Object obj ) throws InvalidArgumentException {
      return JavaUtils.convert(obj, XdmItem.class);
    }
    
    
    public static String cardinalityString( int card) {
      switch( card & StaticProperty.CARDINALITY_MASK ) {
      case StaticProperty.ALLOWS_ONE_OR_MORE : return "+" ;
      case StaticProperty.ALLOWS_ZERO_OR_MORE : return "*" ;
      case StaticProperty.ALLOWS_ZERO_OR_ONE : return "?" ;
      default :
           return "" ;
      }
    }

      
    public static int getCardinality( XdmValue value ) {
      return Value.asValue(value.getUnderlyingValue()).getCardinality();
    }
    
    public static String simpleTypeName( XdmItem v ) 
    {

      @SuppressWarnings("rawtypes")
	Value<? extends Item> value = Value.asValue(v.getUnderlyingValue());
      if(value instanceof EmptySequence) 
        return "empty-sequence()";
      
      try {
        @SuppressWarnings("rawtypes")
		Item item = value.asItem();
        return Type.displayTypeName( item );
        /*
        if (item instanceof NodeInfo) {
            return ((NodeInfo)item).getDisplayName() + cs ;
        } else if (item instanceof AtomicValue) {
            return ((AtomicValue)item).getPrimitiveType().getName() + cs ;
            
       
        } else if (item instanceof FunctionItem) {
            return ((FunctionItem<?>)item).getFunctionName().toString()  + cs ;
        }
        */
      } catch (net.sf.saxon.trans.XPathException e) {
         mLogger.info("Excpetion getting simple type",e);
         
      }
      return JavaUtils.simpleTypeName( v );

      }


    public static String simpleTypeName( XdmValue value ) {
      

      
      if (value == null)
        return null;
      if( value.size() == 0 )
        return "empty-sequence()" ;


      String cs = cardinalityString(getCardinality(value));
      
      if( value.size() == 1 )
        return simpleTypeName( value.itemAt(0) );
      else
      {
        String prev = null ;
        for( XdmItem item : value ) {
          String st = simpleTypeName( item );
          if( prev == null ) {
            prev = st ; 
          }
          else
          if( ! prev.equals(st ) )
            return "item()+" ;
        }
        
        if( prev == null )
          return "empty-sequence()";

        return prev + cs ;
      }
    }

    public static  Iterator<XdmItem>  asXdmItemIter(XdmValue value) {
      assert( value != null );
      if( value == null )
        return null ;
      return value.iterator() ;
    }
      

    
    public static XdmValue simplify( XdmValue value ) {
      assert( value != null );

    	if( value == null )
    		return null ;
    	
        int n = value.size();
        if (n == 0) {
            return  XdmEmptySequence.getInstance();
        } else if (n == 1) {
            return value.itemAt(0);
        } else {
            return value;
        }
    }


    @SuppressWarnings("rawtypes")
	public static SequenceIterator<? extends Item> asSequenceIterator(XdmValue value ) throws net.sf.saxon.trans.XPathException
    {
        if( value == null )
          return null ;

        ValueRepresentation<? extends Item> v = value.getUnderlyingValue();
        if (v instanceof Value) {
          return  ((Value<?>)v).iterate();
        } else {
          return SingletonIterator.makeIterator((NodeInfo)v);
        }
      }
    public static XdmValue toXdmValue( Iterator<XValue> iter )
    {
      return new XdmValue(
        Util.toConvertingIterable(iter, 
          new XValueToXdmItemConverter()));
    }

    public static XdmValue toXdmValue( Iterable<XValue> iter )
    {
      return toXdmValue(iter.iterator() );
    }
    public static XdmValue toXdmValue( List<XValue> list )
    { 
      if( list == null || list.size() == 0 )
        return emptySequence();
      return new XdmValue(
        Util.toConvertingIterable(list.iterator(), 
          new XValueToXdmItemConverter()));
    }


    public static List<XValue> toXValueList(XdmValue item )
    {
     return  Util.toList( 
        Util.toConvertingIterater(item.iterator(), 
          new XMLUtils.XdmToXValueConverter()));
      
      
    }
    

    public static List<XValue> toXValueList(XdmItem item )
    {
     return toXValueList((XdmValue)  item );
      
      
    }
    

    
    
/*

    public static SequenceIterator asSequenceIterator( Iterable<XValue> iter )
    {
       
      
      List<Item> items = new ArrayList<Item>();
      for( XValue v : iter ) {
        items.add( (Item) v.toXdmItem().getUnderlyingValue());
      }
      
      return new SequenceExtent<Item>(items).asIterator()

    }

    public static SequenceIterator asSequenceIterator(Iterable<Item> iitem)
    {
      
      List<Item> items = new ArrayList<Item>();
      for (Item item : iitem) {
        items.add(item);
      }
      return new SequenceExtent<Item>(items);
    }

*/
    final static class XValueToItemConverter 
    implements ITypeConverter<XValue, Item<?> >
    {
      @SuppressWarnings("rawtypes")
	@Override
      public Item<?> convert(XValue xvalue) throws InvalidArgumentException
      {
        ValueRepresentation<? extends Item> v = xvalue.toXdmItem().getUnderlyingValue();
        assert( v instanceof Item );
        if( !( v instanceof Item ))
          return null ;
        return (Item<?>) v ;
      }
    }


    final static class XValueToXdmItemConverter 
    implements ITypeConverter<XValue,XdmItem >
    {
      @Override
      public XdmItem convert(XValue xvalue) throws InvalidArgumentException
      {
        return xvalue.toXdmItem();
      }
    }


    public static final class XdmToXValueConverter implements ITypeConverter<XdmItem, XValue>
    {
      @Override
      public XValue convert(XdmItem value) throws InvalidArgumentException
      {
        return XValue.newXValue(TypeFamily.XDM , value);
      }
    }


	public static boolean isXdmValue(Object obj) {
		return obj instanceof XdmValue ;

	}


	public static boolean isXdmElement(Object obj) {
		if( obj instanceof XdmNode ){
			XdmNode node = ((XdmNode)obj);
			return node.getNodeKind() == XdmNodeKind.ELEMENT;
		}
		return false ;
	}


  
    
};
