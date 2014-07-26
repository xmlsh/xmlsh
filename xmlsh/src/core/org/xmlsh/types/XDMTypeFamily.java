package org.xmlsh.types;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.XMLUtils;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;

public final class XDMTypeFamily implements ITypeFamily
{
    static final XDMTypeFamily instance = new XDMTypeFamily();

    private XDMTypeFamily() {}
    
    @Override
    public TypeFamily typeFamily() {
        return TypeFamily.XDM;

    }

    @Override
    public EnumSet<TypeFamily> subTypeFamilies() {
        return EnumSet.noneOf(TypeFamily.class);
    }

    @Override
    public EnumSet<TypeFamily> superTypeFamilies() {
        return EnumSet.of(TypeFamily.JAVA);
    }

    @Override
    public IType getType(Object obj) {
       return XDMType.getType(obj);
    }

    @Override
    public boolean isInstanceOfFamily(Object obj) {
        return 
                obj instanceof XdmValue;
        
    }

    @Override
    public boolean isClassOfFamily(Class<?> cls) {
        return XdmValue.class.isAssignableFrom( cls ) ||
                ValueRepresentation.class.isAssignableFrom(cls );
    }

    @Override
    public String asString(Object value) throws UnsupportedEncodingException, SaxonApiException {
        if( value instanceof XdmValue )
           return new String( XMLUtils.toBytes( (XdmValue) value , SerializeOpts.defaultOpts  ) , SerializeOpts.defaultOpts.getOutput_xml_encoding() );
    
        else
            return value.toString();
    
    }
    


}
