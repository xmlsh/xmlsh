package org.xmlsh.types;

import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XdmItem;
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
    	if( mClass == null ) 
    		return XTypeKind.NULL;
    	
		if( XdmValue.class.isAssignableFrom(mClass) ) {
			
			if( XdmAtomicValue.class.isAssignableFrom(mClass ) ) 
				return XTypeKind.ATOMIC ;
			if( XdmNode.class.isAssignableFrom(mClass ) ) 
				return XTypeKind.CONTAINER;
		
		}

    	return XTypeKind.UNKNOWN;
    }
    
    public static IType getType(Object obj) {
        return new XDMType( obj == null ? null : obj.getClass() );
    }

	/* (non-Javadoc)
	 * @see org.xmlsh.types.TypeBase#simpleName()
	 */
    @Override
    public String simpleName()
    {

    	return kind() == XTypeKind.ATOMIC ? "string" : "xml" ;
    	
    }

	/* (non-Javadoc)
	 * @see org.xmlsh.types.TypeBase#typeName()
	 */
    @Override
    public String typeName()
    {
    	return simpleName();
    }

 
}
