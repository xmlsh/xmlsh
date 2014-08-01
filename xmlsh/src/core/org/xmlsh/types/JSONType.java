package org.xmlsh.types;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class JSONType extends AbstractType implements IType
{    

	private class Methods extends  AbstractMethods 
	{

		protected Methods(	)
		{
			super( JSONType.this );
		}

		@Override
		public String asString(Object value) {
			try {
				return JSONUtils.jsonToString( (JsonNode) value );
			} catch (JsonProcessingException e1) {
				mLogger.warn("Exception serializing Json value",e1);
			}
			return "";
		}
		@Override
		public int getSize(Object obj)
		{
			assert( obj instanceof JsonNode) ;
			return ((JsonNode)obj).size();
		}
		@Override
		public XValue getXValue(Object obj, String ind) throws CoreException
		{
			assert( ! Util.isBlank(ind));

			if( obj == null )
				return _nullValue;
			assert( obj instanceof JsonNode );

			JsonNode node = (JsonNode) obj ;
			switch( node.getNodeType() ) {
			case ARRAY : 
				return newXValue( node.get( Util.parseInt(ind, 0)));
			case OBJECT :
				return newXValue(  node.get(ind));
			case  POJO :
				return new XValue( null, node.get( ind) );  // not JSON type 
			default :
				return newXValue(  obj ) ;
			}



		}
		@Override
		public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
		{
			if( value == null )
				return ;

			assert( value instanceof JsonNode );
			JsonNode node = (JsonNode) value ;

			try {
				JSONUtils.writeJsonNode(node, out , opts );
			} catch (JsonGenerationException | JsonMappingException e ) {
				Util.wrapIOException(e);
			}	
		}

		/* (non-Javadoc)
		 * @see org.xmlsh.types.AbstractMethods#simpleTypeName(java.lang.Object)
		 */
		@Override
		public String simpleTypeName(Object obj)
		{
			assert( obj != null );
			if( obj instanceof JsonNode ) {
				JsonNodeType nt = ((JsonNode)obj).getNodeType();
				return nt.toString().toLowerCase();
			}
			return JSONUtils.getJavaType(obj).getClass().getSimpleName();
		}



	}

	private static Logger  mLogger = LogManager.getLogger( JSONType.class);

	private static volatile Methods _methods = null ;;

	protected static IType newInstance(XTypeKind kind)
	{
		return new JSONType(kind);
	}


	private JSONType(XTypeKind kind) {
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
