/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.datamapping;

import java.util.List;

import net.bytebuddy.modifier.Visibility;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.Util;
/*
 * Looks up predefined types , or fully specified types 
 * Returns the class object
 */
// [full-name [class [ classloader ] ]]
@Function(name="type",names={"simple-type","new-type","get-type"})
public class type extends AbstractBuiltinFunction {

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception {

		mLogger.entry( shell , args);
		requires( args.size() <= 2 , "usage: type( [name [class] )");

		ClassLoader classLoader = getClass().getClassLoader() ;// TBD
		Class<?> cls = null ;
		String name = null ;
		
		XValue  xname = null ;
		XValue  xcls  = null ; 
		
		
		switch( args.size()){
		case 0:      // Assume Object.class
			return mLogger.exit(XValue.newXValue( (Object) Object.class));
			
		case 1:
			xcls  = args.get(0);
			break ;
		case 2:
			xname = args.get(0);
			xcls = args.get(1);
		    break ;
		}
		
		// base class
	 
	   
	   name = ( xname == null || ! xname.isString() ) ? null : xname.toString();
	   if( Util.isBlank(name)) name = null;
	   
	   
	   cls = DataMappingModule.resolveClass(name,  xcls, classLoader);


	    return  mLogger.exit(XValue.newXValue( TypeFamily.JAVA  , cls ));


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
