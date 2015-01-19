/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.java;

import java.util.Collections;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassLoadingStrategy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.instrumentation.FieldAccessor;
import net.bytebuddy.modifier.Visibility;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.utility.ByteBuddyCommons.*;
import net.bytebuddy.asm.ClassVisitorWrapper;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.BridgeMethodResolver;
import net.bytebuddy.dynamic.scaffold.FieldRegistry;
import net.bytebuddy.dynamic.scaffold.MethodRegistry;
import net.bytebuddy.dynamic.scaffold.inline.InlineDynamicTypeBuilder;
import net.bytebuddy.dynamic.scaffold.inline.MethodRebaseResolver;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.dynamic.scaffold.subclass.SubclassDynamicTypeBuilder;
import net.bytebuddy.instrumentation.Instrumentation;
import net.bytebuddy.instrumentation.ModifierContributor;
import net.bytebuddy.instrumentation.attribute.FieldAttributeAppender;
import net.bytebuddy.instrumentation.attribute.MethodAttributeAppender;
import net.bytebuddy.instrumentation.attribute.TypeAttributeAppender;
import net.bytebuddy.instrumentation.method.MethodDescription;
import net.bytebuddy.instrumentation.method.MethodLookupEngine;
import net.bytebuddy.instrumentation.type.TypeDescription;
import net.bytebuddy.instrumentation.type.TypeList;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.modifier.TypeManifestation;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;

@Function(name="interface",names={"new-interface"} )
public class _interface extends AbstractBuiltinFunction {
    @SuppressWarnings({ "unchecked" })
    @Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {

	    String name = args.remove(0).toString();
	    
	    DynamicType.Builder<?> b = new ByteBuddy().
	            makeInterface().name(name);

	    for( XValue a : args ){
	        String f = a.toString();
            b=b.defineMethod( JavaUtils.toSetterName(f)  ,Void.TYPE, ( List<Class<?>> )(List)  Collections.
                    singletonList(String.class) , Visibility.PUBLIC).withoutCode().
                defineMethod( JavaUtils.toGetterName(f) , String.class ,( List<Class<?>> ) (List) Collections.emptyList() , Visibility.PUBLIC ).withoutCode();
	    }

	    Class cls = b.make()
	                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
	                .getLoaded();

	   		return XValue.newXValue( cls  );


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
