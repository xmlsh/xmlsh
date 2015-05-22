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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;


public class xannotation extends XCommand {
	
	@Target({ElementType.ANNOTATION_TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyAnnotation { }
	


	@Override
	public int run(  List<XValue> args ) throws Exception {

		Options opts = new Options("p=package,c=class",SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		OutputPort stdout = getEnv().getStdout();
		setSerializeOpts(getSerializeOpts(opts));

		try( PrintWriter pw = stdout.asPrintWriter(getSerializeOpts(),true)){
			pw.flush();
			if( args.isEmpty()){
				if( opts.hasOpt("p") ){
					for( Package p : Package.getPackages())
					   pw.println(p.getName());
					
				}
			} else 
			for( XValue arg : args){
				if( opts.hasOpt("p"))
					printAllPackageAnnotations(pw,arg);
				else
					printAllClassAnnotations(pw,arg);
			}			
		}
		
		return 0;

	}

	private void printAllClassAnnotations(PrintWriter pw, XValue arg) {
		
		try {
			Class<?> cls = org.xmlsh.util.JavaUtils.convertToClass(arg , getShell() );
			if( cls == null )
				pw.println("Cound not locate class: " + arg.toString() );
			else {

		    	printClassAnnotations(pw , cls , "");
			  printPackageAnnotations( pw , cls.getPackage() , "");
			}
		
		} catch (CoreException e) {
			pw.println("Cound not locate class: " +  e.getMessage());
		}
	}
    private void printAllPackageAnnotations(PrintWriter pw, XValue arg) {
		
			Package pkg = org.xmlsh.util.JavaUtils.convertToPackage(arg );
			if( pkg != null)
  			  printPackageAnnotations( pw ,pkg , "");
			else 
			pw.println("Cound not locate package: " + arg.toString() );

	}

	private void printPackageAnnotations(PrintWriter pw, Package pkg ,
			String indent) {

		if( pkg == null )
			return ;
		pw.println(indent + "Package: " + pkg.getName() );
		
		Annotation[] annos = pkg.getDeclaredAnnotations();
		printAnnotations( pw , annos , indent );
		
		
	}

	private void printClass(PrintWriter pw, Class<?> cls, String indent ) {
		pw.println(indent + ( classType(cls) )  + cls.getName() );
		indent = indent + " ";
		printClassAnnotations(pw, cls, indent);
		for( Class<?> inter : cls.getInterfaces()){
		   printClass( pw ,  inter ,  indent + " ");
		}
		
		Method[] methods =cls.getMethods();
		indent = indent + " " ;
		for( Method m : methods ){
			printMethod(pw, indent, m);
			
		}

		Class<?>[] childc = cls.getDeclaredClasses();
		for( Class<?> c : childc ){
			pw.println(indent+ "Child Classes");
			printClass(  pw , c , indent + " ");
			
		}
		Class<?> s = cls.getSuperclass();
		if( s != null ){
			pw.println(indent+ "Superclass");
			printClass( pw , s , indent + " ");
		}
		
	}

	protected void printMethod(PrintWriter pw, String indent, Method m) {
		Annotation[] ma =  m.getAnnotations();
		Annotation[][] pa = m.getParameterAnnotations();
		Type[] pt = m.getParameterTypes();
		boolean mp = false ;
		if( ma.length > 0 || pa.length > 0 ){
			if( ma.length > 0 ){
				mp = true ;
				pw.println(indent + "Method: "  + m.getName() );
				printAnnotations( pw , ma , indent + " ");
			}


			if( pa.length > 0 ){
				
				int i = 0;
				for( Annotation[] as : pa ){
					if( as.length > 0 ){
						if( ! mp ){
						    pw.println(indent + "Method: "  + m.getName() );
						    pw.println(indent + "Parameter Annotations");
						    mp = true ;
						}

						pw.println(indent + " Param: " + pt[i].toString() ); 
					    printAnnotations( pw ,  as , indent + " ");
					}
				    i++;

				}
		  }
		}
	}

	private void printAnnotations(PrintWriter pw, Annotation[] annotations , String indent ) {
		for( Annotation anno : annotations  ){
			printAnnotation( pw , anno , indent );
		}
		
	}

	private String classType(Class<?> cls) {
		if( cls.isInterface())
			return "Interface: " ;
		if( cls.isAnnotation())
			return "Annotation: ";
		if( cls.isAnonymousClass() )
			return "Anonymous: " ;

		if( cls.isEnum() ) 
			return "Enum: ";
		if( cls.isPrimitive()) 
			return "Primitive: " ;
		if( cls.isArray()) 
			return "Array: ";
		return "Class: ";
	}

	protected void printClassAnnotations(PrintWriter pw, Class<?> cls, String indent) {
		Annotation [] annos = cls.getDeclaredAnnotations();
		for( Annotation anno : annos  ){
			printAnnotation( pw , anno , indent );
		}
		
	}

	
	private void printAnnotation(PrintWriter pw, Annotation anno, String indent) {
		Class<? extends Annotation> type = anno.annotationType();
		String value = anno.toString();
		pw.println(indent+"Annotation:");
		pw.println(indent+ "Type: " + type.getName()  );
		pw.println(indent+ "Value: " + value );
		
		if( !type.getPackage().equals(  Package.getPackage("java.lang.annotation" ))){
		
			Annotation[] aparents = type.getDeclaredAnnotations();
			if( aparents.length > 0  ){
				pw.println(indent +"Annotation Annotations");
				printAnnotations( pw,aparents, indent + " ");
			}
			pw.println(indent+"Annotation Class Annotations");
			printClass( pw , type , indent + " ");
		}
				
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
