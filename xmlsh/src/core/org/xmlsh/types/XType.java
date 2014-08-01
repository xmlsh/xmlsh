package org.xmlsh.types;

import org.xmlsh.core.IXValueContainer;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;

public class XType extends AbstractType implements IType
{
	private class Methods extends  AbstractMethods 
	{

		protected Methods(	)
		{
			super( XType.this );
		}

		@Override
		public XValue append(Object value, XValue v)
		{

			if( ( value instanceof IXValueContainer ) ) {
				((IXValueContainer<?>)value).add( v );
				return v ;
			}
			return new XValue( value.toString() + v.toString());

		}
		@Override
		public String asString(Object value) {
			return value.toString();
		}
		@Override
		public int getSize(Object obj)
		{
			if( ( obj instanceof IXValueContainer ) )
				return ((IXValueContainer<?>)obj).size();
			return 0;

		}

		@Override
		public XValue getXValue(Object obj, String ind)
		{
			if( obj == null)
				return _nullValue ;

			if( Util.isBlank(ind) )
				return newXValue(obj);


			if( ( obj instanceof IXValueContainer ) )
				return ((IXValueContainer<?>)obj).get(ind);
			else
				if( ( obj instanceof XValue ) ) 
					return ((XValue) obj );

				else
					return new XValue(null, obj ) ; // may conatain any type - SNH

		}

		@Override
		public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
		{
			if( value == null )
				return ;

			if( ( value instanceof IXValueContainer ) )
				((IXValueContainer<?>)value).serialize( out , opts );
			else
				if( ( value instanceof XValue ) )
					try {
						((XValue)value).serialize(out, opts);
					} catch (InvalidArgumentException e) {
						Util.wrapIOException(e);
					}
				else
					out.write(JavaUtils.toByteArray(value, opts));

		}

		@Override
		public String typeName(Object obj)
		{
			assert( obj != null );
			return obj.getClass().getName();


		}

	}

	private static volatile Methods _methods = null ;;

	protected static XType newInstance(XTypeKind kind )
	{
		return new XType( kind );
	}

	private XType( XTypeKind kind ) {
		super(TypeFamily.XTYPE , kind );
	}



	@Override
	protected IMethods getMethodsInstance()
	{
		if( _methods == null )
			_methods = new Methods() ;
		return _methods ;
	}





}
