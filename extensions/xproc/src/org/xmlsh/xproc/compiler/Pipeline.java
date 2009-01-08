/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.XdmNode;

/*
 <p:pipeline
  name? = NCName
  type? = QName
  psvi-required? = boolean
  xpath-version? = string
  exclude-inline-prefixes? = prefix list>
    (p:input |
     p:output |
     p:option |
     p:log |
     p:serialization)*,
    (p:declare-step |
     p:import)*,
    subpipeline
</p:pipeline>

 */



class Pipeline extends DeclareStep {
	

	void parse(XdmNode node)
	{
		super.parse(node);
		
		/*
		 * Add implicit input and output ports
		 
  <p:input port='source' primary='true'/>
  <p:input port='parameters' kind='parameters' primary='true'/>
  <p:output port='result' primary='true'>

		 */
		
		inputs.add( new Input("source" , null , true ));
		inputs.add( new Input("parameters" , "parameters" , true ));
		outputs.add( new Output("result" , true ));
		
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
