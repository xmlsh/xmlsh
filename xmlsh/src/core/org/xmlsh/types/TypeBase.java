package org.xmlsh.types;

import org.xmlsh.core.XValue;

abstract class TypeBase implements IType
{
    protected  Class<?>  mClass;
    protected TypeBase(Class<?> cls ) {
        mClass = cls ;
    }


    @Override
    public String simpleName() {
       return  typeName().replaceFirst("^.*\\.", "");
        
    }

    @Override
    public String typeName() {
        return mClass.getName();
    }
    

    @Override
    public XValue getIndexedValue(Object obj , String ind ) { return null ; }


}
