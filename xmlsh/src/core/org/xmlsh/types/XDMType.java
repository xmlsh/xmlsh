package org.xmlsh.types;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class XDMType extends AbstractType implements IType
{
	private class Methods extends AbstractMethods
	{

		protected Methods()
		{
			super(XDMType.this);
		}

		@Override
		public XValue append(Object obj , XValue xvalue)
		{
			assert (obj instanceof XdmValue);

			if( xvalue == null )
				return  newXValue(obj) ;
			XdmValue xv = (XdmValue) obj; 

			if( ! xvalue.isXdmValue() )
				return newXValue( xv.toString() + xvalue.toString() );


			List<XdmItem> items = new ArrayList<XdmItem>();
			for (XdmItem item : ((XdmValue)obj) )
				items.add(item);


			for( XdmItem item : xvalue.asXdmValue() )
				items.add(item);

			return newXValue( new XdmValue(items) );

		}

		@Override
		public String asString(Object value)
		{
			if(value == null)
				return "";
			if(value instanceof XdmValue)
				try {
					return new String(XMLUtils.toByteArray((XdmValue) value, SerializeOpts.defaultOpts),
							SerializeOpts.defaultOpts.getOutput_xml_encoding());
				} catch (SaxonApiException | IOException e) {
					mLogger.warn("Exception serializing XDM value", e);
				}
			else
				return value.toString();
			return "";

		}

		@Override
		public int getSize(Object obj)
		{
			assert (obj instanceof XdmValue);
			return ((XdmValue) obj).size();

		}

		@Override
		public XValue getXValue(Object obj, String ind)
		{
			if(obj == null)
				return _nullValue;
			assert (obj instanceof XdmValue);

			if(Util.isEmpty(ind))
				return newXValue(obj);
			XdmValue v = (XdmValue) obj;

			if(Util.isBlank(ind) || ind.equals("*"))
				return new XValue(v);
			else {
				int index = Util.parseInt(ind, 0) - 1;
				if(index < 0 || index >= v.size())
					return null;
				return newXValue(v.itemAt(index));
			}

		}

		@Override
		public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
		{

			if(value == null)
				return;

			if(value instanceof XdmValue) {
				XdmValue xv = (XdmValue) value;
				if(!XMLUtils.isAtomic(xv)) {
					try {
						XMLUtils.serialize(xv, out, opts);
					} catch (SaxonApiException e) {
						Util.wrapIOException(e);
					}
					return;
				}
			}

			out.write(value.toString().getBytes(opts.getOutput_text_encoding()));

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
				return "null" ;

			assert (obj instanceof XdmValue);
			if(XMLUtils.isAtomic((XdmValue) obj))
				return "string";
			else
				return "xml";
		}


	}
	private static Logger	        mLogger	= LogManager.getLogger(XDMType.class);

	private static volatile Methods _methods = null ;;

	protected static IType newInstance(XTypeKind kind)
	{
		return new XDMType(kind);
	}

	private XDMType(XTypeKind kind)
	{
		super(TypeFamily.XDM, kind);
	}

	@Override
	protected IMethods getMethodsInstance()
	{
		if( _methods == null )
			_methods = new Methods() ;
		return _methods ;
	}


}
