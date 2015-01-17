/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.experimental.commands;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;


public class xdescribe extends XCommand {
	
	@Target({ElementType.ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyAnnotation { }
	


	@Override
	public int run(  List<XValue> args ) throws Exception {

		Options opts = new Options(SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		OutputPort stdout = getEnv().getStdout();
		setSerializeOpts(getSerializeOpts(opts));

		try( PrintWriter pw = stdout.asPrintWriter(getSerializeOpts(),true)){
			pw.flush();
            for( XValue a : args ){
			    Object o = a.asObject(); 
			    if( o instanceof Class) 
			        printClass(pw,(Class<?>)o,"");
			    else

			        printObject(pw,o,"");
			}
		}
		
		return 0;

	}

	private void printObject(PrintWriter pw, Object o, String indent) {
        pw.println(indent + "object " + o.toString() );
        printClass(pw,o.getClass(), indent + " ");
        
    }

	private void printClass(PrintWriter pw, Class<?> cls, String indent ) {

		pw.print(indent + ( classType(cls) )  + cls.getName() );
	    Class<?> s = cls.getSuperclass();
	    if( s != null )
	        pw.println(indent+" extends " + s.toString());
		String sep = "implements ";
	    for(Class<?>  i : cls.getInterfaces() ){
		    pw.println(indent+sep+i.toString() );
		    sep=", ";
	    }

	    pw.println(indent+"{");

		indent = indent + " ";
		
		/*
		printClassAnnotations(pw, cls, indent);
		for( Class<?> inter : cls.getInterfaces()){
		   printClass( pw ,  inter ,  indent + " ");
		}
		*/
		
		Method[] methods =cls.getDeclaredMethods();
		indent = indent + " " ;
		for( Method m : methods ){
			printMethod(pw, indent, m);
			
		}
		
	      Field[] fields =cls.getFields();
	        indent = indent + " " ;
	        for( Field f : fields ){
	            printField(pw, indent, f);
	        }

		Class<?>[] childc = cls.getDeclaredClasses();
		for( Class<?> c : childc )
			printClass(  pw , c , indent + " ");
		pw.println(indent + "}");
		
	}

	private void printField(PrintWriter pw, String indent, Field f) {
	    pw.println(  f.toString() );
    }

    protected void printMethod(PrintWriter pw, String indent, Method m) {
        pw.println(indent + m.toString() );
	}

    private String classType(Class<?> cls) {
		if( cls.isInterface())
			return "interface " ;
		if( cls.isAnnotation())
			return "annotation ";
		if( cls.isAnonymousClass() )
			return "<> " ;

		if( cls.isEnum() ) 
			return "enum ";
		if( cls.isPrimitive()) 
			return "primitive " ;
		if( cls.isArray()) 
			return "array ";
		return "class ";
	}

		
}
//
//
//Copyright (C) 2008-2014    David A. Lee.
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
