package org.xmlsh.types;

import java.util.EnumSet;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.util.JSONUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class JSONTypeFamily implements ITypeFamily
{
    static final JSONTypeFamily instance = new JSONTypeFamily();
    private static Logger  mLogger = LogManager.getLogger( JSONTypeFamily.class);

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
}
