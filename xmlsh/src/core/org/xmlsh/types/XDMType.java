package org.xmlsh.types;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.XdmValue;

public class XDMType extends TypeBase implements IType
{

    private XDMType(Class<?> cls) {
        super(cls);
    }

    @Override
    public TypeFamily family() {
       return TypeFamily.XDM;
    }

    @Override
    public XTypeKind kind() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public static IType getType(Object obj) {
        return new XDMType( obj == null ? null : obj.getClass() );
    }

 
}
