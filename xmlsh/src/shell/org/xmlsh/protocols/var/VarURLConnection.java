/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.protocols.var;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.VariableInputPort;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.xpath.ThreadLocalShell;

public class VarURLConnection extends URLConnection {

	protected VarURLConnection(URL url) {
		super(url);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void connect() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.net.URLConnection#getInputStream()
	 */
	@Override
	public InputStream getInputStream() throws IOException {

		String name = getURL().getPath();
		Shell shell = ThreadLocalShell.get();
		if (shell != null)
			try {
				XVariable var = shell.getEnv().getVar(name);
				if( var != null )
					return new VariableInputPort(var).asInputStream(shell.getSerializeOpts());
			} catch (CoreException e) {
				return null;
			}
		return null;

	}

	/* (non-Javadoc)
	 * @see java.net.URLConnection#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {

		String name = getURL().getPath();
		Shell shell = ThreadLocalShell.get();
		if (shell != null) {
			XVariable var = shell.getEnv().getVar(name);
			if( var != null )
				return new VariableOutputPort(var).asOutputStream(shell.getSerializeOpts());
		}
		return null;
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
