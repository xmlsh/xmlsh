/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.List;

public class JavaModuleFunctionCommand extends Command
{
	private Module mModule ;

	private String mFunc ;
	private Class<?> mClass;

	private ClassLoader mClassLoader;

	public JavaModuleFunctionCommand(Module mod, String func, Class<?> cls , ClassLoader cl )
	{
		super(func);
		mModule = mod ;
		mFunc = func ;
		mClass = cls ;
		mClassLoader = cl;
	}

	@Override
	public void print(PrintWriter out, boolean bExec)
	{
		out.println( mModule.getPackage() + "." + mModule.getName() + "." + mFunc + "()");

	}

	@Override
	public int exec(Shell shell) throws Exception
	{
		List<XValue> args = shell.getArgs();
		XValue retVal = null ;

		if( Util.isEqual("new", mFunc)) {  // Constructor
			retVal  = JavaUtils.newXValue(mClass, args);
		}
		else
			// return class as an object
			if( Util.isEqual("class", mFunc)){

				retVal = new XValue( TypeFamily.JAVA, mClass );

			}
			else {

				Object thisObj = null;
				// Static first 
				Method m = JavaUtils.getBestMatch(mClass , mFunc, args, true );
				if( m == null && args.size() > 0  ) {

					thisObj = args.remove(0).asObject();
					if( mClass.isInstance(thisObj) ) 
						m = JavaUtils.getBestMatch(mClass , mFunc, args, false );

				}		
				if( m == null )
					throw new InvalidArgumentException( "Cannot find matching method: " + mFunc );


				retVal = thisObj != null ? JavaUtils.callMethod( m, thisObj , args ) : 
					JavaUtils.callStaticMethod( m , args );


			}
		shell.exec_return(retVal);
		return 0;


	}

	@Override
	public boolean isSimple()
	{
		// TODO Auto-generated method stub
		return true;
	}

}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */