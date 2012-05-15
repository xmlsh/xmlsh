/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;

public class JavaUtils {


	public static XValue newObject(String classname, List<XValue> args, ClassLoader classloader) throws Exception {
		Class<?> cls = Class.forName(classname, true, classloader);

		Constructor<?>[] constructors = cls.getConstructors();
		Constructor<?> c = getBestMatch(args, constructors);
		if (c == null) 
			throw new InvalidArgumentException("No construtor match found for: " + classname  + "(" + getArgClassesString(args) + ")");

		Object obj = c.newInstance(getArgs(c.getParameterTypes(), args));
		return new XValue(obj);
	}

	public static XValue callStatic(String classname, String methodName, List<XValue> args,
			ClassLoader classloader) throws Exception {
		Class<?> cls = Class.forName(classname, true, classloader);
		Method[] methods = cls.getMethods();
		Method m = getBestMatch(methodName, args, methods,true);

		if (m == null) 
			throw new InvalidArgumentException("No method match found for: " + classname + "." + methodName + "(" + getArgClassesString(args) + ")");

		Object obj = m.invoke(null, getArgs(m.getParameterTypes(), args));
		
		// Special case for null - use formal return type to cast to right XValue type
		if( obj == null ){
			if( String.class.isAssignableFrom(m.getReturnType()) )
				return new XValue( (String) null );
			else
				return new XValue( (Object) null);
			
			
		}
			
		
		
		return new XValue(obj);
	}

	public static String getArgClassesString(List<XValue> args) {
		StringBuffer sb = new StringBuffer();
		for( XValue arg : args ){
			if( sb.length() > 0 )
				sb.append(",");
			sb.append( arg.asObject().getClass().getName() );
			
		}
		return sb.toString();
	}

	public static XValue callMethod(XValue instance, String methodName, List<XValue> args,
			ClassLoader classloader) throws Exception {
		Class<?> cls = instance.asObject().getClass();
		Method[] methods = cls.getMethods();
		Method m = getBestMatch(methodName, args, methods,false);

		if (m == null) 
			throw new InvalidArgumentException("No method match found for: " + cls.getName() + "." + methodName);

		Object obj = m.invoke(instance.asObject(), getArgs(m.getParameterTypes(), args));
		
		// Special case for null - use formal return type to cast to right XValue type
		if( obj == null ){
			if( String.class.isAssignableFrom(m.getReturnType()) )
				return new XValue( (String) null );
			else
				return new XValue( (Object) null);
			
			
		}
		
		
		return new XValue(obj) ;
	}

	public static Method getBestMatch(String methodName, List<XValue> args, Method[] methods , boolean bStatic ) throws XPathException {

		
		Method best = null;
		int bestConversions = 0;
		
		for (Method m : methods) {
			int conversions = 0;
			if (m.getName().equals(methodName)) {
				boolean isStatic = (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC   ;
				if( bStatic && ! isStatic )
					continue ;
				
				Class<?>[] params = m.getParameterTypes();
				if (params.length == args.size()) {
					int i = 0;
					for (XValue arg : args) {
						int conversion = arg.canConvert(params[i]);
						if( conversion < 0 )
							break;
						i++;
						conversions += conversion ;
					}
					if (i == params.length){
						
						if( best == null || conversions < bestConversions ){
							best = m;
							bestConversions = conversions ;
						}
					}

				}

			}

		}
		return best;

	}

	public static Object[] getArgs(Class<?>[] params, List<XValue> args) throws XPathException {

		Object[] ret = new Object[params.length];
		int i = 0;
		for (XValue arg : args) {
			ret[i] = arg.convert(params[i]);
			i++;
		}

		return ret;

	}

	public static Constructor<?> getBestMatch(List<XValue> args, Constructor<?>[] constructors) throws XPathException {
		
		Constructor<?> best = null;
		int bestConversions = 0;
		
		// TODO how to choose best match
		
		for (Constructor<?> c : constructors) 
		{
			Class<?>[] params = c.getParameterTypes();
			if (params.length == args.size()) {
				int conversions = 0;
				int i = 0;
				for (XValue arg : args) {
					int convert = arg.canConvert(params[i]);
					if( convert < 0 )
						break;
					conversions += convert;
					i++;
				}
				if (i == params.length){
					// Find best match
					if( best == null || conversions < bestConversions ){
						best = c ;
						bestConversions = conversions;
						
					}
				

				}

			}

		}
		return best;

	}

	public static boolean isIntClass(Class<?> c) {
		if( c == Integer.class ||
			c == Long.class ||
			c == Byte.class ||
			c == Short.class ||
			
			c == Integer.TYPE ||
			c == Long.TYPE ||
			c == Byte.TYPE || 
			c == Short.TYPE )
				return true ;
		return false;
	
	
	}

	public static Object convert(Object value, Class<?> c) throws XPathException {
		
		if( c.isInstance(value))
			return c.cast(value);
		
		
		
		// Convert to XdmValue
		if( c.equals(XdmValue.class) ){
			// Try some smart conversions of all types XdmAtomicValue knows
			if( value instanceof Boolean )
				return new XdmAtomicValue( ((Boolean)value).booleanValue() );
			else
			if( value instanceof Double)
				return new XdmAtomicValue( ((Double)value).doubleValue() );
			else
			if( value instanceof Float)
				return new XdmAtomicValue( ((Float)value).floatValue() );
			
			else
			if( value instanceof BigDecimal)
				return new XdmAtomicValue( (BigDecimal) value );
			else
			if( value instanceof BigDecimal)
				return new XdmAtomicValue( (BigDecimal) value );
			
			else
			if( value instanceof URI )
				return new XdmAtomicValue( (URI) value );
			else
			if( value instanceof Long)
				return new XdmAtomicValue( (Long) value );
			else
			if( value instanceof QName)
				return new XdmAtomicValue( (QName) value );
			else
				value = new XdmAtomicValue( value.toString() );
		}
		if( c.isInstance(value))
			return c.cast(value);
		
		if( c.isPrimitive() ){
			
			Class<?> vclass = value.getClass();
			
			
			/*
			 * Try to match non-primative types
			 */
			if( c == Integer.TYPE ){
				if( vclass == Long.class )
					value = Integer.valueOf(((Long)value).intValue());
				else
				if( vclass == Short.class )
					value = Integer.valueOf( ((Short)value).intValue() );
				else
				if( vclass== Byte.class )
					value = Integer.valueOf( ((Byte)value).intValue() );
			}
			else
			if( c == Long.TYPE ){
				if(vclass == Integer.class )
					value = Long.valueOf(((Integer)value).intValue());
				else
				if( vclass == Short.class )
					value = Long.valueOf( ((Short)value).intValue() );
				else
				if( vclass == Byte.class )
					value = Long.valueOf( ((Byte)value).intValue() );
			}
			
			else
			if( c == Short.TYPE ){
				if( vclass == Integer.class )
					value = Short.valueOf((short)((Integer)value).intValue());
				else
				if( vclass == Long.class )
					value = Short.valueOf((short) ((Long)value).intValue() );
				else
				if( vclass== Byte.class )
					value = Short.valueOf((short) ((Byte)value).intValue() );
			}
				
			else
			if( c == Byte.TYPE ){
				if( vclass == Integer.class )
					value = Byte.valueOf((byte)((Integer)value).intValue());
				else
				if(vclass == Long.class )
					value = Byte.valueOf((byte) ((Long)value).intValue() );
				else
				if( vclass == Short.class )
					value = Byte.valueOf((byte) ((Short)value).intValue() );
			}
				
			
		}
		return value ;
	}

	public static XValue getField(String classname, XValue instance, String fieldName, ClassLoader classloader) throws InvalidArgumentException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		Class<?> cls = Class.forName(classname, true, classloader);
	    Field f = cls.getField(fieldName);

 
	    

		if (f == null) 
			throw new InvalidArgumentException("No field match found for: " + classname + "." + fieldName );

		Object obj = f.get(instance == null ? null : instance.asObject());	
		// Special case for null - use formal return type to cast to right XValue type
		if( obj == null ){
			if( String.class.isAssignableFrom(f.getType()) )
				return new XValue( (String) null );
			else
				return new XValue( (Object) null);
			
			
		}
			
		
		
		return new XValue(obj);
	}
	
	
}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
