package org.xmlsh.types;

import java.util.EnumSet;

import com.fasterxml.jackson.databind.JsonNode;

public class JavaTypeFamily implements ITypeFamily
{
    static final JavaTypeFamily instance = new JavaTypeFamily();

    private JavaTypeFamily() {}

    @Override
    public TypeFamily typeFamily() {
      return TypeFamily.JAVA;
    }

    @Override
    public EnumSet<TypeFamily> subTypeFamilies() {
        return EnumSet.allOf(TypeFamily.class);
    }

    @Override
    public EnumSet<TypeFamily> superTypeFamilies() {
        return EnumSet.noneOf(TypeFamily.class);
    }

    @Override
    public IType getType(Object obj) {
        return JavaType.getType(obj);
    }

  
    @Override
    public boolean isInstanceOfFamily(Object obj) {
        return true ;
    }

    @Override
    public boolean isClassOfFamily(Class<?> cls) {
        return true ;
    }

    @Override
    public String asString(Object value) {
       return value.toString();
    }

}
