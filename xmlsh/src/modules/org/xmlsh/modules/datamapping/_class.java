/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.datamapping;

import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

@Function(name="class",names={"new-class"} )
public class _class extends AbstractBuiltinFunction {
    @SuppressWarnings({ "unchecked" })
    @Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {
	    ClassLoader classLoader = getClass().getClassLoader();

	    String name = args.remove(0).toString();
	    XValue xpclass = args.remove(0);
	    Class<?> pclass= DataMappingModule.resolveClass( null , xpclass , classLoader );
	    
	    DynamicType.Builder<?> b = new ByteBuddy().subclass( (Class<?>) (pclass!=null ? pclass :Object.class) ).name(name);
	    
		while(  ! args.isEmpty() ){
	    	XValue xv = args.remove(0);
	        String f = xv.toString();
	        Class<?> cls = String.class;
	        if( ! args.isEmpty() )
	        	cls = DataMappingModule.resolveClass( null , args.remove(0) , classLoader );
	        
	        b=b.defineField( f, cls, Visibility.PUBLIC );	        
        }

	    Class<?> cls = b.make()
	                .load(classLoader, ClassLoadingStrategy.Default.INJECTION )
	                .getLoaded();

	   		return XValue.newXValue((Object) cls  );


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
