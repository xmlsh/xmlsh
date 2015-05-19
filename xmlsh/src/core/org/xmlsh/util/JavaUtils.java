/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;

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

    public static Method getBestMatch(String methodName, List<XValue> args, Method[] methods , boolean bStatic ) throws XPathException, InvalidArgumentException {


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

    public static Object[] getArgs(Class<?>[] params, List<XValue> args) throws XPathException, InvalidArgumentException {

        Object[] ret = new Object[params.length];
        int i = 0;
        for (XValue arg : args) {
            ret[i] = arg.convert(params[i]);
            i++;
        }

        return ret;

    }

    public static Constructor<?> getBestMatch(List<XValue> args, Constructor<?>[] constructors) throws XPathException, InvalidArgumentException {

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


        Class<?> vclass = value.getClass();
        boolean bAtomic = isAtomic( value );

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
               if( targetClass == Character.TYPE ){
                   if( vclass == Byte.class )
                       value = Character.valueOf((char) ((Byte)value).intValue());
               }
                            ; // skip

            return (T) value ;
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
        
        // QName
        if(  XdmValue.class.isAssignableFrom(targetClass) ){
            if( targetClass.isAssignableFrom(XdmAtomicValue.class) ) 
                if( QName.class.isAssignableFrom(sourceClass) ||
                   isAtomicClass( sourceClass ))
                    return 1;

        }


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
        if( targetString && soruceString )
            return 5 ;
        if( targetClass.isPrimitive() && soruceString ){
            // convert string to primative, 
            if( isBooleanClass(targetClass) || isIntClass(targetClass) || isStringClass(targetClass) || isNullClass(targetClass) )
                return 6 ;
        }
            
        return -1;

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

    private static boolean isAtomic(Object value)
    {
        return isAtomicClass( value.getClass() );
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
    public static  int canConvertObject( Object sourceObject ,  Class<?> targetClass) throws InvalidArgumentException {
        return canConvertClass( sourceObject.getClass() , targetClass );
    }


    public static boolean isStringClass( Class<?> cls) {

        return cls.isAssignableFrom(String.class)  ||
              cls.isAssignableFrom(CharSequence.class);

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


    public static boolean isNumber(String v) {
        return toNumber(v,null) != null ;
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
