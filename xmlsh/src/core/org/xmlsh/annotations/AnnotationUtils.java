package org.xmlsh.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xmlsh.core.ICommand;
import org.xmlsh.core.IXFunction;
import org.xmlsh.sh.shell.IFunctionDefiniton;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

public class AnnotationUtils {

	/*
	 * Get function names in order of  
	 *    name/default + aliases
	 */
	
	public static List<String> getFunctionNames(
			Class<?> cls) 
	{

		Function a = cls.getAnnotation(Function.class);
		if( a != null ){
			List<String> names = new ArrayList<>();
			// Use name= over default
			if( ! Util.isBlank(a.name()) )
				names.add(a.name());
			if( ! Util.isBlank( a.value()))
				names.add(a.value());
			if( a.names() != null && a.names().length > 0 )
				for( String name : a.names())
					names.add( name );
			return names;
		}
		if( isFunctionClass( cls )){
			String name = cls.getSimpleName();
			if( name.startsWith("_"))
				name = name.substring(0);
	
			else
	     	    name = JavaUtils.convertFromCamelCase( name );
			
			return Collections.singletonList(name);
		}
		return null;
			

	}

	/* 
	 * Static check if this is a possible function class
	 */
	public static boolean isFunctionClass(Class<?> cls) {
	
		if( cls == null )
			return false ;
		if(  IFunctionDefiniton.class.isAssignableFrom( cls ) ) 
			return true ;
		if( IXFunction.class.isAssignableFrom(cls ))
			return true ;
		if( cls.getAnnotation(org.xmlsh.annotations.Function.class ) != null )
			return true ;
		return false ;
	
	
	}

    
    public static List<String> getCommandNames(
            Class<?> cls) 
    {

        Command a = cls.getAnnotation(Command.class);
        if( a != null ){
            List<String> names = new ArrayList<>();
            // Use name= over default
            if( ! Util.isBlank(a.name()) )
                names.add(a.name());
            if( ! Util.isBlank( a.value()))
                names.add(a.value());
            if( a.names() != null && a.names().length > 0 )
                for( String name : a.names())
                    names.add( name );
            return names;
        }
        if( isCommandClass( cls )){
            String name = cls.getSimpleName();
            if( name.startsWith("_"))
                name = name.substring(0);
            else
                name = JavaUtils.convertFromCamelCase( name );
            return Collections.singletonList(name);
        }
        return null;
            

    }

	/* 
	 * Static check if this is a possible command class
	 */
	public static boolean isCommandClass(Class<?> cls) {
	
		if( cls == null )
			return false ;
		if(  ICommand.class.isAssignableFrom( cls ) ) 
			return true ;
		if( cls.getAnnotation(org.xmlsh.annotations.Command.class ) != null )
			return true ;
		return false ;
	
	
	}

}
