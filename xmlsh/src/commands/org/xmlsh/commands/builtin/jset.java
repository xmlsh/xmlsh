/**
 * $Id: set.java 388 2010-03-08 12:27:19Z daldei $
 * $Date: 2010-03-08 07:27:19 -0500 (Mon, 08 Mar 2010) $
 *
 */

package org.xmlsh.commands.builtin;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.util.JavaUtils;

import java.util.List;

public class jset extends BuiltinCommand {

	static final String sDocRoot = "env";

	public int run(List<XValue> args) throws Exception {

		Options opts = new Options("v=variable:,c=classname:,m=method:,o=object:,f=field:");
		opts.parse(args);

		String varname = opts.getOptStringRequired("v");
		String classname = opts.getOptString("c",null);
		String method = opts.getOptString("m", null);
		String field = opts.getOptString("f", null);
	
		
		
			
		XValue instance = opts.getOptValue("o");

		args = opts.getRemainingArgs();

		ClassLoader classloader = getClassLoader(null);

		XValue obj = null;
		if (method == null && field == null )
			obj = JavaUtils.newXValue(classname, args, classloader); 
		else if (instance == null && field == null)
			obj = JavaUtils.callStatic(classname, method, args, classloader);
		else
	    if( field != null )
	    	obj = JavaUtils.getField( classname , instance,  field , classloader );
	    else
			obj = JavaUtils.callMethod(instance, method, args, classloader);

		mShell.getEnv().setVar(varname, obj, false);

		return 0;
	}

}
//
//
// Copyright (C) 2008-2014    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
