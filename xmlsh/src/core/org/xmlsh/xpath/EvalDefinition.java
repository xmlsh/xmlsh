/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xpath;

import net.sf.saxon.functions.ExtensionFunctionCall;
import net.sf.saxon.functions.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.SequenceType;

public class EvalDefinition extends ExtensionFunctionDefinition {

	
	public static final String kXMLSH_EXT_NAMESPACE = "http://www.xmlsh.org/extfuncs";

	@Override
	public boolean dependsOnFocus() {
		return true ; // depends on 
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		SequenceType[] types = { SequenceType.SINGLE_STRING , SequenceType.ANY_SEQUENCE , SequenceType.OPTIONAL_NODE};
		return types ;
		
		
		
	}

	@Override
	public Object getCompilerForJava() {
		// TODO Auto-generated method stub
		return super.getCompilerForJava();
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName("xmlsh", kXMLSH_EXT_NAMESPACE , "eval" );
			
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.functions.ExtensionFunctionDefinition#getMaximumNumberOfArguments()
	 */
	@Override
	public int getMaximumNumberOfArguments() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.functions.ExtensionFunctionDefinition#getMinimumNumberOfArguments()
	 */
	@Override
	public int getMinimumNumberOfArguments() {
		return 1 ;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.functions.ExtensionFunctionDefinition#getResultType(net.sf.saxon.value.SequenceType[])
	 */
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.ANY_SEQUENCE ; 
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.functions.ExtensionFunctionDefinition#hasSideEffects()
	 */
	@Override
	public boolean hasSideEffects() {
		return true ;
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.functions.ExtensionFunctionDefinition#makeCallExpression()
	 */
	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new EvalFunctionCall();
	}

	/* (non-Javadoc)
	 * @see net.sf.saxon.functions.ExtensionFunctionDefinition#trustResultType()
	 */
	@Override
	public boolean trustResultType() {
		// TODO Auto-generated method stub
		return super.trustResultType();
	}

	
	
}



//
//
//Copyright (C) 2008,2009 David A. Lee.
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
