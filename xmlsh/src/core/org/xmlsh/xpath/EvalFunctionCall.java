/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xpath;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.VariableInputPort;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.shell.Shell;

public class EvalFunctionCall extends ExtensionFunctionCall {
	private static Logger mLogger = LogManager.getLogger(EvalFunctionCall.class);
	
	
	EvalFunctionCall() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public SequenceIterator call(SequenceIterator[] arguments, XPathContext context) throws XPathException
	{
		
	
	// Arg0 is the command to run
	
	String command = arguments[0].next().getStringValue();
	SequenceIterator args = arguments.length > 1 ? arguments[1] : null ;
	
	
	Shell shell = ShellContext.get();


	
	try {
		
		if( shell == null )
			shell = new Shell();
		else
			shell = shell.clone();
		
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
		 
		 Item contextItem = null ;
		 if( arguments.length > 2 )
			 contextItem = arguments[2].next();
		 else
			 contextItem = context.getContextItem();
		 
		// set stdin
		 if( context != null ){
			 VariableInputPort iPort = new VariableInputPort( new XVariable("_in", new XValue(contextItem)));
			 shell.getEnv().setStdin(iPort);
		 }
		 
		 shell.setArgs(shell_args);
		 try {
			shell.exec(cmd);
		} catch (ThrowException e) {
			mLogger.info("Caught ThrowException within eval" , e  );
			return null;
		}
		 oPort.release();
		 oValue = oVar.getValue();
		 if( oValue == null )
			 return null;
		
		return oValue.asSequenceIterator(); 
	} 
	catch( Exception  e)
	{
		throw new XPathException(e);
	}
	
	finally {
		
		shell.close();
	}

	
	}

}



//
//
//Copyright (C) 2008,2009,2010 David A. Lee.
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
