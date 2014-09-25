package org.xmlsh.annotations;

import java.lang.annotation.Annotation;

import org.xmlsh.util.JavaUtils;

public class AnnotationUtils {

	public static String getFunctionName(
			Class<?> cls) 
	{

		String name ;
		Function a = cls.getAnnotation(Function.class);
		if( a != null )
			return a.name() ;
		
		name = cls.getSimpleName();
		if( name.startsWith("_"))
			name = name.substring(0);

		else
		
	    name = JavaUtils.convertFromCamelCase( name );
			return name ;
			

	}

}
