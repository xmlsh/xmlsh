/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.xmlsh.util.Util;

class XProcUtil {

	static String[] getAttrList( XdmNode node , String attr )
	{
		String value = XProcUtil.getAttrString( node , attr );
		if( value == null )
			return null;
		return value.split(" ");
		
	}

	static boolean getAttrBool( XdmNode node , String attr, boolean def)
	{
		String value = XProcUtil.getAttrString( node , attr );
		if( value == null )
			return def ;
		return parseBoolean( value );
		
	}
	
	// Parse a boolean as either 'true' or 'false
	static boolean parseBoolean(String value) {
		return Util.isEqual(value, "true");
	}

	static Boolean getAttrBoolean( XdmNode node , String attr)
	{
		String value = XProcUtil.getAttrString( node , attr );
		if( value == null )
			return null ;
		return new Boolean(parseBoolean( value ));
		
	}
	static QName getAttrQName(XdmNode node , String attr)
	{
		String value = XProcUtil.getAttrString( node , attr );
		if( value == null )
			return null;
		return new QName( value , node );
		
	}

	static String getAttrString( XdmNode node , String attr , String def )
	{
		String value = node.getAttributeValue( new QName(null, attr));
		if( value == null )
			value = def;
		return value;
		
	}

	static String getAttrString( XdmNode node , String attr )
	{
		return getAttrString( node , attr , null );
		
	}
	
	static XdmNode getFirstChild( XdmNode parent )
	{
		XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);
		while( children.hasNext() ){
			XdmItem item=children.next();
			if( item instanceof XdmNode ){
				if( ((XdmNode)item).getNodeKind() != XdmNodeKind.ELEMENT )
					continue ;
				
				return (XdmNode) item ;
			}
		}
		return null;
		
		
	}

	public static String quote(String string) {
		/* return "'" + 
			string.replace("'",  "'\"'\"'") +
			"'";
			*/
		return "<{{" + string + "}}>";
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
