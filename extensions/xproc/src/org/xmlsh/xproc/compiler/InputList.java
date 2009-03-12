/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.util.ArrayList;

@SuppressWarnings("serial")
class InputList extends ArrayList<Input>
{

	Input getPrimary() {
		// Find an explicitly marked primary
		for( Input in : this )
			if( in.primary != null && in.primary.booleanValue() )
				return in ;
		
		// If only 1 and its not marked use it
		if( this.size() == 1 ){
			Input in = this.get(0);
			if( in.primary == null )
				return in;
		}
		return null;
	}

	public void serialize(OutputContext c) {
		for( Input in : this )
			in.serialize(c);
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
