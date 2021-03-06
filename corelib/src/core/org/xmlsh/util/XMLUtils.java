package org.xmlsh.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.EmptyAtomicSequence;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.ObjectValue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
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
		Sequence item = value.getUnderlyingValue();
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
        Sequence v = value.getUnderlyingValue();
        
        
      return SequenceTool.getCardinality(v);
    }
    
    public static String simpleTypeName( XdmItem v ) 
    {

        Sequence s = v.getUnderlyingValue();
      if(s instanceof EmptySequence || s instanceof EmptyAtomicSequence || SequenceTool.getCardinality(s) == StaticProperty.ALLOWS_ZERO ) 
        return "empty-sequence()";
      
      try {
          
          Item item = SequenceTool.asItem(s);
       /*
          return Type.displayTypeName( item );
        if (item instanceof NodeInfo) {
            return ((NodeInfo)item).getDisplayName() + cs ;
        } else if (item instanceof AtomicValue) {
            return ((AtomicValue)item).getPrimitiveType().getName() + cs ;
            
       
        } else if (item instanceof FunctionItem) {
            return ((FunctionItem<?>)item).getFunctionName().toString()  + cs ;
        }
        */
      
          if (item instanceof NodeInfo) {
              NodeInfo node = (NodeInfo) item;
              switch (node.getNodeKind()) {
                  case Type.DOCUMENT:
                      return "document-node()";
                  case Type.ELEMENT:
                      return "element()";
                  case Type.ATTRIBUTE:
                      return "attribute()";
                  case Type.TEXT:
                      return "text()";
                  case Type.COMMENT:
                      return "comment()";
                  case Type.PROCESSING_INSTRUCTION:
                      return "processing-instruction()";
                  case Type.NAMESPACE:
                      return "namespace()";
                  default:
                      return "";
              }
          } else if (item instanceof ObjectValue) {
              return ((ObjectValue<?>) item).getObject().getClass().toString();
          } else if (item instanceof AtomicValue) {
              return ((AtomicValue) item).getItemType().toString();
          } else if (item instanceof Function) {
              return "function(*)";
          } else {
              return item.getClass().toString();
          }
      
      
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
    public static Sequence asSequence(XdmValue value )
    {
        if( value == null )
          return null ;

        Sequence s = value.getUnderlyingValue();
        return s ;
      }
    @SuppressWarnings("rawtypes")
	public static SequenceIterator asSequenceIterator(XdmValue value ) throws XPathException 
    {
        if( value == null )
          return null ;

        Sequence s = value.getUnderlyingValue();
        return s.iterate();
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
    implements ITypeConverter<XValue, Item >
    {
      @SuppressWarnings("rawtypes")
	@Override
      public Item convert(XValue xvalue) throws XPathException, InvalidArgumentException
      {
        Sequence s = xvalue.toXdmItem().getUnderlyingValue();
        if( SequenceTool.getLength(s) == 1 )
          return s.head();
        return null;
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
	public static  XMLEventReader createEventReader( String s ) throws UnsupportedEncodingException, FactoryConfigurationError, XMLStreamException
	{
		return createEventReader(s.getBytes(ShellConstants.kENCODING_UTF_8) );
	}


	public static  XMLEventReader createEventReader(byte[] bytes)
			throws FactoryConfigurationError, XMLStreamException {
		ByteArrayInputStream iss = new ByteArrayInputStream( bytes );
		XMLInputFactory factory = XMLInputFactory.newInstance();
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.valueOf(false)); // Dont try to reference http://java.sun.com/dtd/properties.dtd !!!
		XMLEventReader reader = factory.createXMLEventReader( null , iss);
		return reader;
	}


  public static XPathSelector evalXPath(XdmValue value, String expr, Shell shell) throws IOException, SaxonApiException {
    
      Processor processor = Shell.getProcessor();
      XPathCompiler compiler = processor.newXPathCompiler();
    XPathExecutable exec = compiler.compile(expr);

    if( shell != null ){
      Namespaces ns = shell.getEnv().getNamespaces();
      if(ns != null) {
        for (String prefix : ns.keySet()) {
          String uri = ns.get(prefix);
          compiler.declareNamespace(prefix, uri);
        }
      }
    }
      
    XPathSelector eval = exec.load();
    eval.setContextItem(value.itemAt(0));
    return eval;
  }


  
    
};
