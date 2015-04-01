/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.datamapping;

import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.ClassLoadingStrategy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.modifier.Visibility;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;

@Function(name="type",names={"declare-type"})
public class type extends AbstractBuiltinFunction {

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {


        String name = args.remove(0).toString();
        
        DynamicType.Builder<?> b = new ByteBuddy().subclass(Object.class).name(name);
        

        for( XValue a : args ){
            b=b.defineField( a.toString(), String.class, Visibility.PUBLIC );

        }
        Class<?> bean = b.make()
                .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
            return XValue.newXValue((Object)bean);


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
