/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XIOEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;

public class XPathFunctions {

	
	/*
	 * Thread local instance of a Shell
	 */
	
	private static ThreadLocal<Shell>		sInstance = new ThreadLocal<Shell>()
	{
         protected synchronized Shell initialValue() {
             return null;
         }
	}
    ;
    public static Shell setShell( Shell shell )
    {
    	Shell old = sInstance.get();
    	sInstance.set(shell);
    	return old;

    }
	public static ValueRepresentation eval(XPathContext c, String command  ) throws IOException, CoreException, XPathException
	{
		return eval( c , command , null );
	}
	public static ValueRepresentation eval(XPathContext c, String command , SequenceIterator args ) throws IOException, CoreException, XPathException
	{
		Shell shell = sInstance.get();

		if( shell == null ){
			return null;
		}
		shell = shell.clone();
		
		try {
			Command cmd = shell.parseEval(command);
	
			List<XValue> shell_args = new ArrayList<XValue>();
			Item item = null;
			if( args != null ){
				while( ( item = args.next()) != null ){
					shell_args.add( new XValue( item));
				}
			}
			
			 XValue value = new XValue();
			 XVariable var = new XVariable("_out" , value);
			 VariableOutputPort out = new VariableOutputPort(var);
			 shell.getEnv().setStdout( out );
			 shell.setArgs(shell_args);
			 shell.exec(cmd);
			 
			 value = var.getValue();
			return value.asXdmValue().getUnderlyingValue();
		} finally {
			shell.close();
		}

		
		
	}
	
	
}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
