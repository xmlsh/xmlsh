/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.xproc.util.XProcException;

/*
subpipeline = p:variable*, (p:for-each|p:viewport|p:choose|p:group|p:try|p:standard-step|pfx:user-pipeline)+
*/


class SubPipeline {
	
	List<Variable>	variables = new ArrayList<Variable>();
	List<AbstractStep>			steps	  = new ArrayList<AbstractStep>();
	
	private void	parseVariable( XdmNode node )
	{
		variables.add( Variable.create(node));
		
	}
	
	private void	parseStep( XdmNode node )
	{
		steps.add( AbstractStep.create(node));
	}



	void parse(XdmNode child) {

		QName name = child.getNodeName();
		if( name.equals(Names.kVARIABLE))
			parseVariable(child);
		
		// Must be a step ...
		else
			parseStep(child);
			
		
	}

	public void serialize(OutputContext c) throws XProcException {
		
		boolean bFirst = true ;
		
		c.addBody(" ( ");
		c = c.push();

		
	
	
		
		
		for( AbstractStep step : steps ){
			if( ! bFirst )
				c.addBodyLine(" |");
			bFirst = false ;
			step.serialize(c);
			
		}
		c = c.pop();
		c.addBody(" )" );
		
			
		
	}

}



//
//
//Copyright (C) 2008, David A. Lee.
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
