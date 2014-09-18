/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunction;
import org.xmlsh.core.IHandleable;
import org.xmlsh.core.IReferenceCounted;
import org.xmlsh.core.IReferenceCountedHandleable;
import org.xmlsh.core.IReferencedCountedHandle;
import org.xmlsh.core.XValue;

/*
 * A runtime instance of a module that may have state and may be shared or duplicated
 * in different contexts.
 */
public interface IModule extends		IStaticModule {

	/*
	 * Calls into the module
	 */

	public void onInit(Shell shell, List<XValue> args) throws Exception;

	// close() is called for an unload

	/*
	 * Calls TO the module
	 */
	public ICommand getCommand(String name) throws IOException;

	public IFunction getFunction(String name);

	public URL getHelpURL();

	@Override
	public String getName();

	StaticContext getStaticContext();

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All
 * Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */