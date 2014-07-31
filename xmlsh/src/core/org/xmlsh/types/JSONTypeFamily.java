package org.xmlsh.types;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumSet;

import net.sf.saxon.s9api.XdmValue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JSONUtils;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

public class JSONTypeFamily implements ITypeFamily
{
    static final JSONTypeFamily instance = new JSONTypeFamily();
    private static Logger  mLogger = LogManager.getLogger( JSONTypeFamily.class);
	private static XValue _nullValue = new XValue(TypeFamily.JSON);

    private JSONTypeFamily() {}
    @Override
    public TypeFamily typeFamily() {
        return TypeFamily.JSON;

    }

    @Override
    public EnumSet<TypeFamily> subTypeFamilies() {
        return EnumSet.noneOf(TypeFamily.class);
    }


    @Override
    public EnumSet<TypeFamily> superTypeFamilies() {
        return EnumSet.of( TypeFamily.JAVA);
    }
    @Override
    public IType getType(Object obj) {
     return JSONType.getType(obj);
    }

    @Override
    public boolean isInstanceOfFamily(Object obj) {
        return obj instanceof JsonNode ;
    }

    @Override
    public boolean isClassOfFamily(Class<?> cls) {
      return  JsonNode.class.isAssignableFrom(cls);
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
    public XValue getValue(XValue xvalue, String ind) throws CoreException
    {
		 if( xvalue == null || xvalue.isNull() )
		    	return _nullValue ;
	    if( Util.isBlank(ind) )
	    	return xvalue;
	    Object obj = xvalue.asObject();
	    
	    assert( obj instanceof JsonNode );
	    
	    JsonNode node = (JsonNode) obj ;
	    switch( node.getNodeType() ) {
	    case ARRAY : 
	    	return new XValue( node.get( Util.parseInt(ind, 0)));
	    case OBJECT :
	    case  POJO :
	       return new XValue( node.get( ind) );
        default :
        	return xvalue ;
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
}
