/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.datamapping;

import java.util.Collections;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;

@Function(name="bean",names={"new-bean"} )
public class bean extends AbstractBuiltinFunction {
        @Override
    	public XValue run(Shell shell, List<XValue> args) throws Exception {
    	    ClassLoader classLoader = getClass().getClassLoader();

    	    String name = args.remove(0).toString();
    	    XValue xpclass = args.remove(0);
    
    	    
    	    Class<?> pclass= DataMappingModule.resolveClass( null , xpclass , classLoader );
    	    
    	    DynamicType.Builder<?> b = new ByteBuddy().subclass( (Class<?>) (pclass!=null ? pclass :Object.class) ).name(name);

    	    for( XValue a : args ){
    	        b = newProperty(b, a.toString(), String.class );
    	        
            }
    
    	    Object bean = b.make()
    	                .load(classLoader, ClassLoadingStrategy.Default.INJECTION)
    	                .getLoaded().newInstance();
    
    	   		return XValue.newXValue((Object) bean  );
    
    
    	}

        protected DynamicType.Builder<?> newProperty(DynamicType.Builder<?> b,
                String name, Class<?> cls) {
            b=b.defineField( name, String.class, Visibility.PUBLIC ).
              defineMethod( JavaUtils.toSetterName(name)  ,Void.TYPE, ( List<Class<?>> )(List)  Collections.singletonList(cls) , Visibility.PUBLIC).
              intercept( FieldAccessor.ofField(name) ).
             defineMethod(JavaUtils.toGetterName(name),cls , Collections.EMPTY_LIST, Visibility.PUBLIC).
                     intercept( FieldAccessor.ofField(name) );
            return b;
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
