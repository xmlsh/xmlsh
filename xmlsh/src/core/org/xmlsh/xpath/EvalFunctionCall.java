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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.VariableInputPort;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.core.ICommandExpr;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.XMLUtils;

@SuppressWarnings("serial")
public class EvalFunctionCall extends ExtensionFunctionCall
{
	private static Logger	mLogger	= LogManager.getLogger();

	EvalFunctionCall()
	{
		
		mLogger.entry();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SequenceIterator<? extends Item> call(SequenceIterator<? extends Item>[] arguments, XPathContext context) throws XPathException
	{

		
		mLogger.entry(arguments, context);
		// Arg0 is the command to run

		String command = arguments[0].next().getStringValue();
		SequenceIterator<? extends Item> args = arguments.length > 1 ? arguments[1] : null;

		Shell sh = ThreadLocalShell.get();

		try ( Shell shell = newShell(sh) ) { // work around compiler warning


			ICommandExpr cmd = shell.parseEval(command);

			List<XValue> shell_args = new ArrayList<XValue>();
			if(args != null) {
				@SuppressWarnings("unused")
				Item item;
				while ((item = args.next()) != null) {
					shell_args.add(XValue.newXValue(item));
				}
			}

			// Capture stdout
			XValue oValue = XValue.empytSequence();
			XVariable oVar = XVariable.anonymousInstance(oValue);
			Item contextItem = null;
			if(arguments.length > 2)
				contextItem = arguments[2].next();
			else
				contextItem = context.getContextItem();
			
			try (	VariableOutputPort oPort = new VariableOutputPort(oVar) ;
					VariableInputPort iPort = newInputPort(context, contextItem); ) { // work around compiler warning
					
					
				shell.getEnv().setStdout(oPort);

				// set stdin
				if(iPort != null) 
					shell.getEnv().setStdin(iPort);

				shell.setArgs(shell_args);
				try {
					shell.exec(cmd);
					oPort.flush();
					oValue = oVar.getValue();
					if(oValue == null)
						return  mLogger.exit(null);;
					
				} catch (ThrowException e) {
					
					mLogger.trace("caught throwException");
					return mLogger.exit(null);
				} 
				
			} 
			
			return mLogger.exit(XMLUtils.asSequenceIterator( oValue.toXdmValue() ));
		} catch (Exception e) {
			throw new XPathException(e);
		}
		

	}

	
	// helper functions for compiler warnings in try-resource
	private VariableInputPort newInputPort(XPathContext context,
			@SuppressWarnings("rawtypes") Item contextItem) {
		if( context == null)
			return null ;
		

		return new VariableInputPort(
				  XVariable.anonymousInstance( XValue.newXValue(contextItem)));
	}

	private Shell newShell(Shell sh) throws Exception {
		if( sh == null )
			return new Shell();
		else
			return sh.clone();
	}

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
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
