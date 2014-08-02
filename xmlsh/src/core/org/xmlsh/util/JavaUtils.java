/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaUtils {

	private static Set< String > mReserved;

	// Words that could not make valid class names so cant otherwise be used as commands or functions

	static {
		mReserved = new HashSet<String>();
		mReserved.add( "class");
		mReserved.add( "boolean" );
		mReserved.add( "int"  );
		mReserved.add( "double");
		mReserved.add( "true"  );
		mReserved.add( "false" );
		mReserved.add( "long" );
		mReserved.add( "char" );
		mReserved.add( "null" );
		mReserved.add( "float" );
		mReserved.add( "byte" );
		mReserved.add( "short" );


	}

	public static boolean isReserved(String n )
	{
		return mReserved.contains(n);
	}

	public static XValue newXValue(Class<?> cls,  List<XValue> args) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, CoreException 
	{
		Constructor<?>[] constructors = cls.getConstructors();
		return newXValue( cls , constructors , args );

	}

	public static XValue newXValue(Class<?> cls, Constructor<?>[] constructors ,List<XValue> args) throws CoreException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
	{
		Constructor<?> c = getBestMatch(args, constructors);
		if (c == null) 
			throw new InvalidArgumentException("No construtor match found for: " + cls.getName()  + "(" + getArgClassesString(args) + ")");

		Object obj = c.newInstance(getArgs(c.getParameterTypes(), args));
		return new XValue(null,obj);
	}

	public static XValue newXValue(String classname, List<XValue> args, ClassLoader classloader) throws Exception {
		Class<?> cls = findClass(classname, classloader);
		return newXValue( cls , args );

	}


	public static Class<?> findClass(String classname, ClassLoader classloader) throws ClassNotFoundException
	{
		Class<?> cls = Class.forName(classname, true, classloader);
		return cls;
	}

	public static <T> T newObject( Class<T> cls , Object... args  ) throws InvalidArgumentException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		@SuppressWarnings("unchecked")
		Constructor<T>[] constructors = (Constructor<T>[]) cls.getConstructors();
		Constructor<T> c = getBestConstructor(constructors, args );
		if( c == null )
			throw new InvalidArgumentException("Cannot find constructor for: " + cls.getName());
		return c.newInstance(args);
	}


	public static XValue callStatic(String classname, String methodName, List<XValue> args,
			ClassLoader classloader) throws Exception {
		Class<?> cls = findClass(classname, classloader);
		Method m = getBestMatch(cls,methodName, args, true);

		if (m == null) 
			throw new InvalidArgumentException("No method match found for: " + classname + "." + methodName + "(" + getArgClassesString(args) + ")");

		return callStaticMethod(m, args);
	}

	public static XValue callStaticMethod(Method m, List<XValue> args) throws IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, CoreException
	{
		Object obj = m.invoke(null, getArgs(m.getParameterTypes(), args));
		// Special case for null - use formal return type to cast to right XValue type
		if( obj == null ){
			if( String.class.isAssignableFrom(m.getReturnType()) )
				return new XValue( (String) null );
			else
				return new XValue(null,(Object)null);
		}

		return new XValue(null,obj);
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
		Method m = getBestMatch(cls,methodName, args, false);

		if (m == null) 
			throw new InvalidArgumentException("No method match found for: " + cls.getName() + "." + methodName);

		return callMethod(m, instance.asObject(), args);
	}

	public static XValue callMethod(Method m, Object instance, List<XValue> args) throws IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, CoreException
	{
		Object obj = m.invoke(instance, getArgs(m.getParameterTypes(), args));

		// Special case for null - use formal return type to cast to right XValue type
		if( obj == null ){
			if( String.class.isAssignableFrom(m.getReturnType()) )
				return new XValue( (String) null );
			else
				return new XValue( null , (Object) null);
		}


		return new XValue(null , obj) ;
	}

	public static Method getBestMatch(Class<?> cls, String methodName, List<XValue> args, boolean bStatic ) throws CoreException {

		return getBestMatch(  cls.getMethods() , methodName , args , bStatic  );
	}

	public static Method getBestMatch(Method[] methods, String methodName, List<XValue> args, boolean bStatic ) throws CoreException {



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

	public static Object[] getArgs(Class<?>[] params, List<XValue> args) throws CoreException  {

		Object[] ret = new Object[params.length];
		int i = 0;
		for (XValue arg : args) {
			ret[i] = arg.convert(params[i]);
			i++;
		}

		return ret;

	}

	public static Constructor<?> getBestMatch(List<XValue> args, Constructor<?>[] constructors) throws CoreException {

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

	public static <T> Constructor<T> getBestConstructor(Constructor<T>[] constructors, Object... argValues ) throws InvalidArgumentException {

		Constructor<T> best = null;
		int bestConversions = 0;

		// TODO how to choose best match

		for (Constructor<T> c : constructors) 
		{
			Class<?>[] params = c.getParameterTypes();
			if (params.length == argValues.length) {
				int conversions = 0;
				int i = 0;
				for (Object obj : argValues) {
					int convert = canConvertObject( obj , params[i] );
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
		if( Integer.class.isAssignableFrom(c) ||
				Long.class.isAssignableFrom(c) ||
				Byte.class.isAssignableFrom(c) ||
				Short.class.isAssignableFrom(c) ||
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


		Class<?> vclass = value.getClass();

		if( c.isPrimitive() ){



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

			return value ;
		}
		// Cast through string

		String svalue = value.toString();

		if( c == Integer.class )
			value = Integer.valueOf( svalue );
		else
			if( c == Long.class )
				value =  Long.valueOf( svalue );
			else
				if( c == Short.class )
					value = Short.valueOf(svalue);
				else
					if( c == Float.class )
						value = Float.valueOf(svalue );
					else
						if( c == Double.class )
							value = Double.valueOf(svalue);

		return value ;


	}

	public static XValue getField(String classname, XValue instance, String fieldName, ClassLoader classloader) throws InvalidArgumentException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		Class<?> cls = findClass(classname, classloader);
		return getField( cls , instance == null ? null : instance.asObject() , fieldName , classloader );
	}

	public static XValue getField(Class<?> cls , Object instance, String fieldName, ClassLoader classloader) throws InvalidArgumentException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		assert( cls != null );
		assert( fieldName != null );

		Field f = cls.getField(fieldName);

		if (f == null) 
			throw new InvalidArgumentException("No field match found for: " + cls.getName() + "." + fieldName );

		Object obj = f.get(instance == null ? null : instance);	
		// Special case for null - use formal return type to cast to right XValue type
		if( obj == null ){
			if( String.class.isAssignableFrom(f.getType()) )
				return new XValue( (String) null );
			else
				return new XValue( null, (Object) null);

		}

		return new XValue(null,obj);
	}



	public static  int canConvertClass( Class<?> sourceClass ,  Class<?> targetClass) throws InvalidArgumentException {

		// Equal class
		if( sourceClass.equals(targetClass))
			return 0 ;

		// Directly assignable
		if( targetClass.isAssignableFrom(sourceClass))
			return 1 ;


		// Boxable 
		// int <-> Integer
		if( isIntClass(sourceClass) && isIntClass(targetClass))
			return 2 ;

		return -1;

	}
	public static  int canConvertObject( Object sourceObject ,  Class<?> targetClass) throws InvalidArgumentException {
		return canConvertClass( sourceObject.getClass() , targetClass );
	}

	public static Class<?> convertToClass(XValue arg, Shell shell) throws ClassNotFoundException, CoreException
	{
		return convertToClass(arg, 	shell.getClassLoader(null));

	}

	public static Class<?> convertToClass(XValue arg,  ClassLoader classLoader) throws ClassNotFoundException, CoreException
	{
		if( arg.isAtomic() && arg.isString() )
			return findClass(arg.toString(),classLoader);
		Object obj = arg.asObject();
		if( obj instanceof Class )
			return (Class<?>) obj ;
		return obj.getClass();


	}

	public static byte[] toByteArray(Object value, SerializeOpts opts) throws UnsupportedEncodingException {
		return value.toString().getBytes( opts.getOutput_text_encoding() );

	}

	public static int getSize(Object obj)
	{

		if( obj instanceof Collection )
			return ((Collection<?>)obj).size();
		if( obj instanceof Array )
			Array.getLength(obj);

		return 0;

	}

	public static boolean isNullClass(Class<?> cls)
	{
		return cls == null ;
	}

	public static boolean isContainerClass(Class<?> cls)
	{
		if( cls == null )
			return false;
		if( Collection.class.isAssignableFrom(cls) ||
				Array.class.isAssignableFrom(cls) )
			return true ;
		return false ;

	}
	
	public static boolean isArrayClass(Class<?> cls)
	{
		if( cls == null )
			return false;
		if(     List.class.isAssignableFrom(cls) ||
				Array.class.isAssignableFrom(cls) )
			return true ;
		return false ;

	}
	
	public static boolean isObjectClass(Class<?> cls)
	{
		if( cls == null )
			return false;
		return (  ! isAtomicClass( cls ) );

	}
	
	

	public static boolean isStringClass( Class<?> cls) {

		return String.class.isAssignableFrom(cls ) ||
				CharSequence.class.isAssignableFrom(cls );

	}
	public static boolean isAtomicClass(Class<?> cls)
	{
		return cls.isEnum() || cls.isPrimitive() ||
				isIntClass( cls ) ||
				isStringClass( cls ) ;

	}

	public static boolean isClassClass(Class<?> cls)
	{
		return Class.class.isAssignableFrom(cls);
	}

	public static Object concat(Object o, Object that)
	{

		return o.toString() + that.toString() ;
	}


}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
