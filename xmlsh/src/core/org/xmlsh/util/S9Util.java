/**
 * $Id: $
 * $Date: $
 *
 */

/** 
 * Note: This class is in the net.sf.saxon.s9api package so that it can have access 
 * to package private method XdmNode.wrap() which is otherwise unavailable
 * 
 * @TODO: When s9api is changed to export XdmNode.wrap this class can go away
 * 
 */

package org.xmlsh.util;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

public class S9Util extends XdmNode {
	
	protected S9Util(NodeInfo node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public static XdmValue	wrapNode( NodeInfo node)
	{
		return XdmNode.wrap(node);
	}
	
	public static XdmItem	wrapItem( Item item)
	{
		return XdmNode.wrapItem(item);
	}
	

}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
