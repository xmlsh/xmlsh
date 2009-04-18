/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.util;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.xmlsh.sh.shell.Version;

public class XPathFunctions {

	public static String systemProperty(XPathContext c, String var)
	{
		
	/*
		p:episode
		Returns a string which should be unique for each invocation of the pipeline processor. In other words, if a processor is run several times in succession, or if several processors are running simultaneously, each invocation of each processor should get a distinct value from p:episode.

		The unique identifier must be a valid XML name.

		p:language
		Returns a string which identifies the current language, for example, for message localization purposes. The exact format of the language string is implementation-defined but should be consistent with the xml:lang attribute.

		p:product-name
		Returns a string containing the name of the implementation, as defined by the implementer. This should normally remain constant from one release of the product to the next. It should also be constant across platforms in cases where the same source code is used to produce compatible products for multiple execution platforms.

		p:product-version
		Returns a string identifying the version of the implementation, as defined by the implementer. This should normally vary from one release of the product to the next, and at the discretion of the implementer it may also vary across different execution platforms.

		p:vendor
		Returns a string which identifies the vendor of the processor.

		p:vendor-uri
		Returns a URI which identifies the vendor of the processor. Often, this is the URI of the vendor's web site.

		p:version
		Returns the version of XProc implemented by the processor; for processors implementing the version of XProc specified by this document, the value is “1.0”. The value of the version attribute is a token (i.e., an xs:token per [W3C XML Schema: Part 2]).

		p:xpath-version
		Returns the version(s) of XPath implemented by the processor for evaluating XPath expressions on XProc elements. The result is a list of versions supported. For example, a processor that only supports XPath 1.0 would return “1.0”; a processor that supports XPath 2.0 and XPath 1.0 backwards compatibility mode could return “1.0 2.0”; a processor that supports only XPath 2.0 would return “2.0”.

		p:psvi-supported
		Returns true if the implementation supports passing PSVI annotations between steps, false otherwise.
	*/
		
	int colon = var.indexOf(':');
	if( colon < 1 )
		return "";
	String prefix = var.substring(0,colon);
	var = var.substring(colon+1);
	
	if( var.equals("episode"))
		return "1";
	if( var.equals("language") )
		return "en-us";
	if( var.equals("product-name"))
		return "xmlsh";
	if( var.equals("product-version"))
		return Version.getRelease();
	if( var.equals("vendor"))
		return "DEI Services Inc.";
	if( var.equals("vendor-uri"))
		return "http://www.xmlsh.org";
	if( var.equals("version"))
		return "1";
	if( var.equals("xpath-version"))
		return "2.0";
	if( var.equals("psvi-supported"))
		return "false";
	return "";

	
	}
	
	
	public static boolean stepAvailable(XPathContext c, String step)
	{
		return true ;

	}
	
	public static int iterationPosition(XPathContext c)
	{
		return 1;
	}
	
	public static int iterationSize(XPathContext c)
	{
		return 1;
	}
	
	public static String baseUri(XPathContext c)
	{
		Item item = c.getContextItem();
		if( item instanceof NodeInfo )
			return baseUri( c , (NodeInfo)item );
		else
			return "";
		
	}
	public static String baseUri(XPathContext c,NodeInfo node)
	{
		return node.getBaseURI();
	}
	
	public static String baseUri(XPathContext c,SequenceIterator node)
	{
		Item item;
		try {
			item = node.next();
		} catch (XPathException e) {
			return "";
		}
		if( item != null && item instanceof NodeInfo )
			return baseUri( c , (NodeInfo)item );
		else
			return "";
	}
	public static String resolveUri(XPathContext c,String relative)
	{
		return "";
	}

	public static String resolveUri(XPathContext c,String relative,String base)
	{
		return "";
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
