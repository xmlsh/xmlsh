package org.xmlsh.types;

import org.xmlsh.util.JavaUtils;

import com.fasterxml.jackson.databind.JsonNode;

public class JSONType extends TypeBase implements IType
{

    private JSONType(Class<?> cls) {
        super(cls);
    }

    @Override
    public TypeFamily family() {
        return TypeFamily.JSON;
    }

    @Override
    public XTypeKind kind() {
        // TODO Auto-generated method stub
        return null;
    }

    public static IType getType(Object obj) {
        return new JSONType( obj == null ? null : obj.getClass() );
    }

}