/**
 * $Id: set.java 388 2010-03-08 12:27:19Z daldei $
 * $Date: 2010-03-08 07:27:19 -0500 (Mon, 08 Mar 2010) $
 *
 */

package org.xmlsh.commands.builtin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;

public class jset extends BuiltinCommand {

	static final String sDocRoot = "env";

	public int run(List<XValue> args) throws Exception {

		Options opts = new Options("v=variable:,c=classname:,m=method:,o=object:");
		opts.parse(args);

		String varname = opts.getOptStringRequired("v");
		String classname = opts.getOptString("c",null);
		String method = opts.getOptString("m", null);
		XValue instance = opts.getOptValue("o");

		args = opts.getRemainingArgs();

		ClassLoader classloader = getClassLoader(null);

		Object obj = null;
		if (method == null)
			obj = newObject(classname, args, classloader);
		else if (instance == null)
			obj = callStatic(classname, method, args, classloader);
		else
			obj = callMethod(instance, method, args, classloader);

		mShell.getEnv().setVar(varname, new XValue(obj), false);

		return 0;
	}

	private Object newObject(String classname, List<XValue> args, ClassLoader classloader)
			throws Exception {
		Class<?> cls = Class.forName(classname, true, classloader);

		Constructor<?>[] constructors = cls.getConstructors();
		Constructor<?> c = getBestMatch(args, constructors);
		if (c == null) {
			mShell.printErr("No construtor match found for: " + classname);
			return null;
		}

		Object obj = c.newInstance(getArgs(c.getParameterTypes(), args));
		return obj;
	}

	private Object callStatic(String classname, String methodName, List<XValue> args,
			ClassLoader classloader) throws Exception {
		Class<?> cls = Class.forName(classname, true, classloader);
		Method[] methods = cls.getMethods();
		Method m = getBestMatch(methodName, args, methods,true);

		if (m == null) {
			mShell.printErr("No method match found for: " + classname + "." + methodName);
			return null;
		}

		Object obj = m.invoke(null, getArgs(m.getParameterTypes(), args));
		return obj;
	}

	private Object callMethod(XValue instance, String methodName, List<XValue> args,
			ClassLoader classloader) throws Exception {
		Class<?> cls = instance.asObject().getClass();
		Method[] methods = cls.getMethods();
		Method m = getBestMatch(methodName, args, methods,false);

		if (m == null) {
			mShell.printErr("No method match found for: " + cls.getName() + "." + methodName);
			return null;
		}

		Object obj = m.invoke(instance.asObject(), getArgs(m.getParameterTypes(), args));
		return obj ;
	}

	private Method getBestMatch(String methodName, List<XValue> args, Method[] methods , boolean bStatic ) {

		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				boolean isStatic = (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC   ;
				if( bStatic && ! isStatic )
					continue ;
				
				Class<?>[] params = m.getParameterTypes();
				if (params.length == args.size()) {
					int i = 0;
					for (XValue arg : args) {
						if (!arg.canConvert(params[i]))
							break;
						i++;
					}
					if (i == params.length)
						return m;

				}

			}

		}
		return null;

	}

	private Object[] getArgs(Class<?>[] params, List<XValue> args) {

		Object[] ret = new Object[params.length];
		int i = 0;
		for (XValue arg : args) {
			ret[i] = arg.convert(params[i]);
			i++;
		}

		return ret;

	}

	private Constructor<?> getBestMatch(List<XValue> args, Constructor<?>[] constructors) {
		for (Constructor<?> c : constructors) {
			Class<?>[] params = c.getParameterTypes();
			if (params.length == args.size()) {
				int i = 0;
				for (XValue arg : args) {
					if (!arg.canConvert(params[i]))
						break;
					i++;
				}
				if (i == params.length)
					return c;

			}

		}
		return null;

	}
}
//
//
// Copyright (C) 2008,2009,2010 , David A. Lee.
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
