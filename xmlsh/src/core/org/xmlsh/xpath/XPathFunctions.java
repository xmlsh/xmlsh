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
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.VariableInputPort;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;

public class XPathFunctions {
	private static Logger mLogger = LogManager.getLogger(XPathFunctions.class);


	public static ValueRepresentation eval(XPathContext c, String command  ) throws IOException, CoreException, XPathException
	{
		return eval(c.getContextItem()  ,  command , null );
	}
	
	public static ValueRepresentation eval(XPathContext c, String command , SequenceIterator args ) throws IOException, CoreException, XPathException
	{
		return eval( c.getContextItem() , command , args  );
	}
	public static ValueRepresentation eval(String command , SequenceIterator args , Item context ) throws IOException, CoreException, XPathException
	{
		return eval( context , command , args  );
	}
	private static ValueRepresentation eval(Item context , String command , SequenceIterator args ) throws IOException, CoreException, XPathException
	{
		Shell shell = ShellContext.get();

		if( shell == null )
			shell = new Shell();
		else
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
			
			
			// Capture stdout
			 XValue oValue = new XValue();
			 XVariable oVar = new XVariable("_out" , oValue);
			 VariableOutputPort oPort = new VariableOutputPort(oVar);
			 shell.getEnv().setStdout( oPort );
			 
			// set stdin
			 if( context != null ){
				 VariableInputPort iPort = new VariableInputPort( new XVariable("_in", new XValue(context)));
				 shell.getEnv().setStdin(iPort);
			 }
			 
			 shell.setArgs(shell_args);
			 try {
				shell.exec(cmd);
			} catch (ThrowException e) {
				mLogger.info("Caught ThrowException within eval" , e  );
				return null;
			}
			 
			 oValue = oVar.getValue();
			 if( oValue == null )
				 return null;
			 XdmValue oXdm =oValue.asXdmValue();
			 if( oXdm == null )
				 return null ;
			return oXdm.getUnderlyingValue();
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
