package org.xmlsh.types;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;

public class JavaType extends AbstractType implements IType
{
	private class Methods extends  AbstractMethods 
	{
		protected Methods(	)
		{
			super( JavaType.this );
		}

		@Override
		public String asString(Object value) {
			return value.toString();
		}

		@Override
		public int getSize(Object obj)
		{
			if( obj == null)
				return 0 ;
			return JavaUtils.getSize( obj );

		}

		@Override
		public XValue getXValue(Object obj, String ind) throws CoreException 
		{
			if( obj == null )
				return _nullValue;
			assert( !Util.isBlank(ind) );

			try {
				return JavaUtils.getField( obj.getClass() , obj , ind , null );
			} catch (InvalidArgumentException | SecurityException | NoSuchFieldException | IllegalArgumentException
					| IllegalAccessException | ClassNotFoundException e) {
				Util.wrapCoreException("Exception getting value from java class: " + obj.getClass().getName() , e );
			}
			return newXValue(obj);
		}

		@Override
		public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
		{
			if( value == null )
				return ;
			out.write( JavaUtils.toByteArray(value, opts) );

		}


	}

	private static volatile Methods _methods = null ;;



	protected static IType newInstance(XTypeKind kind)
	{
		return new JavaType(kind );
	}


	private JavaType(XTypeKind _kind) {
		super(TypeFamily.XDM, _kind  );
	}


	@Override
	protected IMethods getMethodsInstance()
	{
		if( _methods == null )
			_methods = new Methods() ;
		return _methods ;
	}



}
