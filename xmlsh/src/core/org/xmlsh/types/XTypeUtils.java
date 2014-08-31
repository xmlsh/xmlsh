package org.xmlsh.types;

import org.xmlsh.core.IXValueMap;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueMap;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.XNamedValue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class XTypeUtils
{
	public static final TypeFamily defaultFamily = TypeFamily.XDM;
	private static final TypeFamily[] typeFamilyPrecidence = {
		TypeFamily.XDM , 
		TypeFamily.XTYPE,
		TypeFamily.JSON ,
		TypeFamily.JAVA 
	};

	public static ITypeFamily getFamilyInstance( TypeFamily family ) {
		switch( family ) {
		case JAVA:
			return JavaTypeFamily._instance;
		case JSON :
			return JSONTypeFamily._instance;

		case XDM : 
			return XDMTypeFamily._instance;

		case XTYPE :
			return XTypeFamily._instance;

		}
		return XDMTypeFamily._instance;
	}


	// Make the best guess as to the type family given only an object
	public static TypeFamily inferFamily( Object obj ) {

		// Hack for now - null values are XDM
		if( obj == null )
			return TypeFamily.XDM;

		for( TypeFamily f : typeFamilyPrecidence ) {
			ITypeFamily itf = getFamilyInstance(f);
			if( itf != null  &&  itf.isInstanceOfFamily(obj))
				return f;
		}
		return defaultFamily;
	}





	public static IXValueMap newMapFromList(List<XValue> value) throws InvalidArgumentException {

		if( value.isEmpty() )
			return new XValueMap(); ;

			if( value.size() == 1 )
				return newMapFromValue( value.get(0) );

			XValueMap map = null ;
			for( XValue xv : value ) {
				XValueMap m = newMapFromValue( xv );
				if( map == null )
					map = m;
				else
					map.addAll(m);
			}

			return map ;  
	}

	public static XValueMap newMapFromProperties(Properties props)
	{
		XValueMap map = new XValueMap();
		for (Iterator<Entry<Object, Object>> iterator = props.entrySet().iterator(); iterator.hasNext();) {
			Entry<Object, Object> e = iterator.next();
			map.put( e.getKey().toString() , new XValue( (String) e.getValue() ) );

		}
		return map;
	}



	public static XValueMap newMapFromValue(XValue value) throws InvalidArgumentException
	{

		if( value.isInstanceOf( XValueMap.class ) )
			return value.asType(  XValueMap.class  );

		if( value.isInstanceOf( Properties.class ) )
			return newMapFromProperties( value.asType( Properties.class) );


		XValueMap map = new XValueMap();

		if( value.isInstanceOf(Map.class) ) {
			for( Entry<?, ?> e : ((Map<?,?>) value.asType(Map.class)).entrySet() )
				map.put( e.getKey().toString() ,e.getValue()  );
		}
		else
		{
			map.put( newNamedValue(value) );
		}

		return map;

	}





	public static org.xmlsh.util.XNamedValue newNamedValue(XValue arg) throws InvalidArgumentException
	{
	    if( arg.isInstanceOf( XNamedValue.class ))
	        return ((XNamedValue) arg.asObject());
	    
	    
		if( arg.isAtomic() ) {
			StringPair pair = new StringPair( arg.toString() ,'=' );
			return new org.xmlsh.util.XNamedValue(pair.getLeft(), new XValue(pair.getRight()) );
		} else {
			throw new InvalidArgumentException( "Cannot convert to NamedValue" );

		}

	}

	
	
	
	
}
