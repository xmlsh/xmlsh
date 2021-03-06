/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;

public class JavaUtils {

	private static Set< String > mReserved;
  private static Logger mLogger = LogManager.getLogger() ;

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
		mReserved.add("package");
		mReserved.add("new");


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
		return XValue.newXValue(null,obj);
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
				return XValue.newXValue((String) null);
			else
				return XValue.newXValue(null,(Object)null);
		}

		return XValue.newXValue(null,obj);
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
				return XValue.newXValue((String) null);
			else
				return XValue.newXValue( null , (Object) null);
		}


		return XValue.newXValue(null , obj) ;
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
  public static Object[] convertObjects(Class<?>[] params, List<Object> args) throws CoreException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {

    Object[] ret = new Object[params.length];
    int i = 0;
    for (Object arg : args) {
      ret[i] = convert(arg , params[i]);
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

  public static int  hasBeanConstructor(Class<?> targetClass, Class<?> sourceClass) throws InvalidArgumentException  
  { 
   
    Constructor<?> c = getBeanConstructor( targetClass , sourceClass );
    if( c == null )
      return -1;
    
    if( c.getParameterTypes()[0].isAssignableFrom( sourceClass) )
      return 3;
    else
      return 4;
  }

  // Does the target hava a single method constructor from a convertable source
  public static Constructor<?> getBeanConstructor(Class<?> targetClass, Class<?> sourceClass) throws InvalidArgumentException  
   {     
       try {
         Constructor<?> c =  targetClass.getConstructor(sourceClass);
         if( c !=null )
           return  c ;
         
      } catch (NoSuchMethodException | SecurityException e) {
        mLogger.info("Exception getting constructors for: " + targetClass.getName(), e );
        return null ;
      }

       Constructor<?>[] cs = targetClass.getConstructors();
       
       for (Constructor<?> c : cs) 
       {
         Class<?>[] params = c.getParameterTypes();
         if (params.length == 1) {
             int convert = canConvertClass( sourceClass , params[0] );
             if( convert >= 0 )
               return c ;
           }
       }
       return null ;
  }
	static private final Class<?> mNumberWrappers[] = new Class<?>[] {
    Integer.class,
    Long.class,
    Byte.class,
    Short.class,
    Number.class,
    AtomicInteger.class, 
    AtomicLong.class, 
    BigDecimal.class, 
    BigInteger.class, 
    Double.class, 
    Float.class
	};
	
  public  static boolean isWrappedOrPrimativeNumber(  Class<?> c )
	{
	  if( c == null )
	    return false ;
	  if( c.isPrimitive() && ! ( c == Void.TYPE || c == Boolean.TYPE ) )
	    return true ;
    for( Class<?> w : mNumberWrappers )
	    if( c.isAssignableFrom(w) )
	      return true ;

	  return false ;
	}
  
  public static Class<?> fromPrimativeName( String name )
  {
	  
	  switch( name ) {
	  case "boolean" :  return java.lang.Boolean.TYPE ;
	  case "char" :  return java.lang.Character.TYPE ;
	  case "byte"  :  return java.lang.Byte.TYPE;
	  case "short" :  return java.lang.Short.TYPE;
	  case "int"   : return  java.lang.Integer.TYPE;
	  case "long"  : return  java.lang.Long.TYPE;
	  case "float" : return  java.lang.Float.TYPE;
	  case "double" :return   java.lang.Double.TYPE;
	  case "void"  : return java.lang.Void.TYPE;
	  default :
		  return null;
	  }
	  
	  
  }
   
  


	public static <T> T convert(Object value, Class<T> targetClass) throws InvalidArgumentException {

	  assert( targetClass != null );
	  
	  assert( value != null );
		if( targetClass.isInstance(value))
			return targetClass.cast(value);

		Class<?> sourceClass = value.getClass();
		
    // Asking for an XdmValue class
    if(XdmValue.class.isAssignableFrom(targetClass)) {
      if( value instanceof XdmNode )
        return (T) (XdmValue) value ;
      
      if( targetClass.isAssignableFrom(XdmAtomicValue.class) ) {
      
        // Try some smart conversions of all types XdmAtomicValue knows
        if(value instanceof Boolean)
          return (T) new XdmAtomicValue(((Boolean) value).booleanValue());
        else if(value instanceof Double)
          return (T) new XdmAtomicValue(((Double) value).doubleValue());
        else if(value instanceof Float)
          return (T) new XdmAtomicValue(((Float) value).floatValue());
  
        else if(value instanceof BigDecimal)
          return (T) new XdmAtomicValue((BigDecimal) value);
        else if(value instanceof BigDecimal)
          return (T) new XdmAtomicValue((BigDecimal) value);
  
        else if(value instanceof URI)
          return (T) new XdmAtomicValue((URI) value);
        else if(value instanceof Long)
          return (T) new XdmAtomicValue((Long) value);
        else if(value instanceof QName)
          return (T) new XdmAtomicValue((QName) value);
      
      // Still wanting an xdm value
      }
      
      if( isAtomic( value ) ) 
         return (T) new XdmAtomicValue(value.toString());

    }

    boolean bAtomic = isAtomic( value );
		Class<?> vclass = value.getClass();

		if( targetClass.isPrimitive() && bAtomic ){

			 /* Try to match non-primative types
			 */
			if( targetClass == Integer.TYPE ){
				if( vclass == Long.class )
					value = Integer.valueOf(((Long)value).intValue());
				else
					if( vclass == Short.class )
						value = Integer.valueOf( ((Short)value).intValue() );
					else
						if( vclass== Byte.class )
							value = Integer.valueOf( ((Byte)value).intValue() );
						else 
							value = Integer.valueOf( value.toString() );
						
			}
			else
				if( targetClass == Long.TYPE ){
					if(vclass == Integer.class )
						value = Long.valueOf(((Integer)value).intValue());
					else
						if( vclass == Short.class )
							value = Long.valueOf( ((Short)value).intValue() );
						else
							if( vclass == Byte.class )
								value = Long.valueOf( ((Byte)value).intValue() );
					value = Long.valueOf( value.toString() );

				}

				else
					if( targetClass == Short.TYPE ){
						if( vclass == Integer.class )
							value = Short.valueOf((short)((Integer)value).intValue());
						else
							if( vclass == Long.class )
								value = Short.valueOf((short) ((Long)value).intValue() );
							else
								if( vclass== Byte.class )
									value = Short.valueOf((short) ((Byte)value).intValue() );
						value = Short.valueOf( value.toString() );

					}

					else
						if( targetClass == Byte.TYPE ){
							if( vclass == Integer.class )
								value = Byte.valueOf((byte)((Integer)value).intValue());
							else
								if(vclass == Long.class )
									value = Byte.valueOf((byte) ((Long)value).intValue() );
								else
									if( vclass == Short.class )
										value = Byte.valueOf((byte) ((Short)value).intValue() );
							value = Byte.valueOf( value.toString() );
						}
						else 
							; // skip

			return (T) value ;
		}
		
		// Bean constructor
		
		Constructor<?> cnst = getBeanConstructor( targetClass , sourceClass ) ;
		if( cnst != null )
      try {
        return (T) cnst.newInstance( convert( value , cnst.getParameterTypes()[0] ) );
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

        mLogger.debug("Exception converting argument for constructor",e);
      }
		
		
		// Cast through string

		String svalue = value.toString();

		if( targetClass == Integer.class )
			value = Integer.valueOf( svalue );
		else
			if( targetClass == Long.class )
				value =  Long.valueOf( svalue );
			else
				if( targetClass == Short.class )
					value = Short.valueOf(svalue);
				else
					if( targetClass == Float.class )
						value = Float.valueOf(svalue );
					else
						if( targetClass == Double.class )
							value = Double.valueOf(svalue);
						else 
							value = null ;

		return (T) value ;


	}

	public static Object getFieldValue(String classname, XValue instance, String fieldName, ClassLoader classloader) throws InvalidArgumentException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		Class<?> cls = findClass(classname, classloader);
		return getFieldValue( cls , instance == null ? null : instance.asObject() , fieldName , classloader );
	}

	public static Object getFieldValue(Class<?> cls , Object instance, String fieldName, ClassLoader classloader) throws InvalidArgumentException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
		assert( cls != null );
		assert( fieldName != null );

		Field f = cls.getField(fieldName);

		if (f == null) 
			throw new InvalidArgumentException("No field match found for: " + cls.getName() + "." + fieldName );

		Object obj = f.get(instance == null ? null : instance);	
		return obj ;
	}



	public static  int canConvertClass( Class<?> sourceClass ,  Class<?> targetClass) throws InvalidArgumentException {

	  assert( targetClass != null );
	  if( targetClass == null )
	    return -1;
	  
	  // Null case
	  if( sourceClass == null  ) {
	    
	     if(  XdmValue.class.isAssignableFrom(targetClass) )
	       return 3;
	     return -1;  
 	  }
		// Equal class
		if( sourceClass.equals(targetClass))
			return 0 ;

		// Directly assignable
		if( targetClass.isAssignableFrom(sourceClass))
			return 1 ;

		boolean sourceNum =  isWrappedOrPrimativeNumber(sourceClass) ;
		boolean targetNum = isWrappedOrPrimativeNumber(targetClass) ;


		// Boxable 
		// int <-> Integer
		if(sourceNum && targetNum  )
			return 2 ;

		boolean soruceString = isStringClass(sourceClass);


		
		// TypeFamily conversion TO XdmValue 
		if( XdmValue.class.isAssignableFrom(targetClass)) {
		   if( XdmNode.class.isAssignableFrom(sourceClass ) )
		     return 1;
		   if( XdmAtomicValue.class.isAssignableFrom(sourceClass) )
		    return 2;

		   if( sourceNum || soruceString || isBooleanClass( sourceClass ) ) 
		     return 3 ;
		   if( isAtomic( sourceClass ) ) 
		     return 3 ;
		}   
		
    boolean targetString = isStringClass(targetClass);
    if( targetClass.isPrimitive() && soruceString )
      // convert string to primative, yes
      return 4 ;
    
		if( targetString )
		  return 5 ;
		
		int c = hasBeanConstructor( targetClass , sourceClass );
		if( c >= 0 )
		  return 5 + c ;

		return -1;

	}


  public static  int canConvertObject( Object sourceObject ,  Class<?> targetClass) throws InvalidArgumentException {
		return canConvertClass( sourceObject.getClass() , targetClass );
	}

	public static Class<?> convertToClass(XValue arg, Shell shell) throws CoreException 
	{
		return convertToClass(arg, 	shell.getClassLoader(null));

	}

	public static Class<?> convertToClass(XValue arg,  ClassLoader classLoader) 
	{
		if( arg.isAtomic() && arg.isString() )
			try {
				return findClass(arg.toString(),classLoader);
			} catch (ClassNotFoundException e) {
				mLogger.catching( e );
				return null;
			}
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
	  if( obj == null )

		if( obj instanceof Collection )
			return ((Collection<?>)obj).size();
		if( obj instanceof Array )
			Array.getLength(obj);

		return 1;

	}

	public static boolean isNullClass(Class<?> cls)
	{
		return cls == null ;
	}

	public static boolean isContainer( Object obj)
	{
	   return isContainerClass( obj.getClass());
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
	
  public  static boolean isBooleanClass(  Class<?> c )
    {
        return c == Boolean.TYPE ||
                Boolean.class.isAssignableFrom( c ) ;

    }
	

//DAL: TODO: See 1.3
	public static boolean isStringClass( Class<?> cls) {

		return String.class.isAssignableFrom(cls ) ||
				CharSequence.class.isAssignableFrom(cls );

	}
	public static boolean isAtomicClass(Class<?> cls)
	{
		return cls.isEnum() || 
				isWrappedOrPrimativeNumber( cls ) ||
				isStringClass( cls ) ;

	}


  public static boolean isClassClass(Class<?> cls)
	{
		return Class.class.isAssignableFrom(cls);
	}

	public static Object stringConcat(Object o, Object that)
	{

		return o.toString() + that.toString() ;
	}

	    public static boolean isNumber(String v) {
        return toNumber(v,null) != null ;
    }
	// Is a or attomic collection type that is empty 
    public static boolean isEmpty(Object value) {
       
        if( isAtomic( value ) ) 
          return isAtomicEmpty(value);
        else
          return isObjectEmpty( value );
      
        }

    public static boolean isObjectEmpty(Object value)
    {

      if( value instanceof Collection )
          return ((Collection<?>)value).isEmpty() ;
      if( value instanceof Map)
          return ((Map<?, ?>)value).isEmpty() ;
      if( value.getClass().isArray() )
          return Array.getLength(value) == 0 ;
      return false ;
    }

    private static boolean isAtomicEmpty(Object value)
    {
      return value == null || value.toString().isEmpty();
      
    }

    private static boolean isAtomic(Object value)
    {
      return isAtomicClass( value.getClass() );
    }

    public static void setNameIndexedValue(  Object obj, String ind, Object object) throws NoSuchFieldException, SecurityException, InvalidArgumentException, IllegalArgumentException, IllegalAccessException {
          Field f = obj.getClass().getField(ind);
          if (f == null) 
             throw new InvalidArgumentException("No field match found for: " + getClassName(obj) + "." + ind );
          f.set(obj, object);
    }

    public static String getClassName(Object obj)
    {
      assert( obj != null );
      if( obj == null )
       return "";
      return obj.getClass().getName();
    }

    @SuppressWarnings("rawtypes")
    public static void setNamePositionalValue(  Object obj, int index, Object object) throws InvalidArgumentException 
    {
      if(obj.getClass().isArray() ) { 
          Array.set( obj ,index, object);
      } else
      if( obj instanceof List) {
          ((List)obj).set( index, object);
      }
      else
      throw new InvalidArgumentException("No positional index for class: " + getClassName(obj)  );
  }

    public static  boolean isArrayOf(Object value, Class<?> cls) {
      return  value.getClass().isArray() &&  
    		  cls.isAssignableFrom(value.getClass().getComponentType());
    }

    public static Object getIndexValue(Object obj, int index )
    {
      Object res = null ;
      if(obj.getClass().isArray() ) { 
        res = Array.get( obj , index);
    } else
    if( obj instanceof List) {
       res =  ((List<?>)obj).get(index);
    }
      return res;
    }
  
    public static <T> List<T> getValues( Object obj  ){
    	
    	 if(obj.getClass().isArray() ) { 
    		Object array = obj ;
			int len = ArrayUtils.getLength(array);
    		ArrayList<T> list = new ArrayList<>(len);
			for( int i = 0 ;i < len ; i++ )
				list.add( (T) Array.get(array, i));

    	   return list ;
    	 }
    	 if( obj instanceof Collection ){
    		@SuppressWarnings({ "rawtypes", "unchecked" })
			Collection<T> c = ((Collection)obj) ;
     		ArrayList<T> list = new ArrayList<>( c.size() );
     		list.addAll( c );
     		return list;

    	 }
    	 return (List<T>) Collections.singletonList(obj);
    	
    }
    

    public static boolean isCollection(Object obj) {
		return obj instanceof Collection;
	}

	public static String simpleTypeName(Object value)
    {
      if( value == null )
        return "null";
      Class<?> cls = value.getClass();
       if( isStringClass( cls ) )
         return "string" ;
       if( isArrayClass( cls ) )
         return "array" ;
       
       if( isWrappedOrPrimativeNumber( cls ) )
         return "number" ;
       return "object" ;
           
    }

	public static boolean hasKey(Object obj, String key) {
		if( obj instanceof Map )
			return ((Map)obj).containsKey(key);
		return false ;
		
		
	}


	public static Object getNameIndexValue(Object obj, String ind) throws CoreException {
		if( obj instanceof Map )
			return ((Map)obj).get(ind);

    	  try {
          return getFieldValue(obj.getClass(), obj, ind, null);
      } catch (SecurityException | NoSuchFieldException | IllegalArgumentException
        | IllegalAccessException | ClassNotFoundException e) {
        Util.wrapCoreException("Exception getting value from java class: " + obj.getClass().getName(), e);
      }
		return null;
		
		
	}

	public static boolean isList(Object obj) {
		return isArrayClass(obj.getClass()) ||
			 obj instanceof List ;
	}
	

	public static Package convertToPackage(XValue arg) {
		if( arg.isAtomic() && arg.isString() )
			return Package.getPackage(arg.toString());
		Object obj = arg.asObject();
		if( obj instanceof Package)
			return (Package) arg.asObject();
		return null ;


	}

	public static  String convertToCamelCase(String name) {
		if (name.indexOf('-') < 0)
			return name;
	
		String parts[] = name.split("-");
		if (parts.length == 1)
			return name;
	
		StringBuilder result = new StringBuilder(name.length());
	
		for (String p : parts) {
			if (p.length() == 0)
				continue;
	
			if (result.length() == 0)
				result.append(p);
			else {
				result.append(Character.toUpperCase(p.charAt(0)));
				result.append(p.substring(1));
			}
	
		}
	
		return result.toString();
	
	}

	public static String convertFromCamelCase(String name) {
		StringBuilder result = new StringBuilder(name.length());
		for( char c : name.toCharArray() ){
			if( Character.isUpperCase(c) ){
			 result.append('-');
			 result.append( Character.toLowerCase(c));
			} else
				result.append(c);
		}
		return result.toString();
	}

	public static  <T> List<T> uniqueList(List<T> list) {
		if( list == null )
			return null ;
		Set<T> set = new HashSet<T>(list);
		if( set.size() == list.size() )
			return list ;

		set.clear();
		List<T> alist = new ArrayList<>( set.size());
		for( T item : list ){
			if( set.contains(item))
				continue;
			set.add( item );
			alist.add( item );
		}
		return alist ;
		
	}

	public static Class<?> getContainedType(Object value) {
		
		Class<?> c = getClass( value );
		
		return c.getComponentType();

	}

	public static Class<?> getClass(Object value) {
		assert( value != null );
		if( value instanceof Class )
			return (Class<?>) value ;
		return value.getClass();
	}

	public static boolean isContainerOf(Object obj, Class<?> c ) {
		assert( obj != null );
		return isArrayOf( obj , c ) ||
				isCollectionOf( obj , c );
	}


    public static Number toNumber(String v, Number def) {
        try {
            if( Util.isInt(v, true))
                return Long.parseLong(v);
            return Double.parseDouble(v);
        } catch (NumberFormatException e) {
            // silent
            return def;
        }
    }

	public static boolean isCollectionOf(Object obj, Class<?> c) {
			if( obj instanceof Collection ){
				Collection col = (Collection<?>) obj;
			    if( col.isEmpty())
			    	return true ; // sure WTF
			    return ClassUtils.isAssignable(col.iterator().next().getClass(), c );
				
			}

			return false ;

	}

    public static String toGetterName(String f) {

        return "get" + toFirstUpperCase(f);
    
    }
    public static String toSetterName(String f) {

        return "set" + toFirstUpperCase(f);
    
    }
    protected static String toFirstUpperCase(String f) {
        return Character.toUpperCase(f.charAt(0)) + f.substring(1);
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
