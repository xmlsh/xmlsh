package org.xmlsh.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.EnumSet;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLUtils;
import org.xmlsh.xpath.ShellContext;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public final class XDMTypeFamily implements ITypeFamily
{
    static final XDMTypeFamily instance = new XDMTypeFamily();
	private static  XValue _theNullValue = new XValue( TypeFamily.XDM );

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
    public String asString(Object value) throws SaxonApiException, IOException {
        if( value == null )
    	   return "";
    	if( value instanceof XdmValue )
           return new String( XMLUtils.toBytes( (XdmValue) value , SerializeOpts.defaultOpts  ) , SerializeOpts.defaultOpts.getOutput_xml_encoding() );
    
        else
            return value.toString();
    
    }

	@Override
    public int getSize(Object obj)
    {
		assert( obj instanceof XdmValue) ;
		return((XdmValue)obj).size();
		
    }

	@Override
    public XValue getValue(XValue xvalue, String ind)
    {
		if( xvalue == null || xvalue.isNull() ) 
			return _theNullValue;
       if( Util.isEmpty(ind))
            return xvalue;
       XdmValue v = xvalue.asXdmValue();
       if( v == null )
    	   return _theNullValue ;
		if( Util.isBlank(ind) || ind.equals("*") )
			return new XValue(v);
		else {
			int index = Util.parseInt(ind, 0) - 1;
			if( index < 0 || index >= v.size() )
				return null ;
			return new XValue( v.itemAt( index ));
		}
		
    }

	@Override
    public void serialize(Object value, OutputStream out, SerializeOpts opts) throws IOException
    {
		
		if( value == null )
			return ;
		
		if( value instanceof XdmValue) {
			XdmValue xv = (XdmValue)value;
			if( ! XMLUtils.isAtomic( xv ) ) {
				  try {
	                XMLUtils.serialize( xv, out , opts );
                } catch (SaxonApiException e) {
	              Util.wrapIOException(e);
                }
				return ;
			}
		}
		
		out.write( value.toString().getBytes(opts.getOutput_text_encoding()));
	    
    }
    


}
