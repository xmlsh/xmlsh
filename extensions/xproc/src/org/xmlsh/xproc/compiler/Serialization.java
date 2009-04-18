/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

/*
 <p:serialization
  port = NCName
  byte-order-mark? = boolean
  cdata-section-elements? = NMTOKENS
  doctype-public? = string
  doctype-system? = string
  encoding? = string
  escape-uri-attributes? = boolean
  include-content-type? = boolean
  indent? = boolean
  media-type? = string
  method? = QName
  normalization-form? = NFC|NFD|NFKC|NFKD|fully-normalized|none|xs:NMTOKEN
  omit-xml-declaration? = boolean
  standalone? = true|false|omit
  undeclare-prefixes? = boolean
  version? = string />
  
 */
class Serialization {
	String	port;
	boolean  byte_order_mark;
	String[]  cdata_section_elements;
	String   doctype_public ;
	String   doctype_system;
	String   encoding;
	boolean   escape_uri_attributes;
	boolean	  include_content_type;
	boolean	  indent;
	String    media_type;
	QName 	  method;
	String 	  normalization_form;
	boolean	  omit_xml_declaration;
	String	  standalone;
	boolean  undeclare_prefixes;
	String   version;
	
	
	static Serialization create(XdmNode node) {
		Serialization s = new Serialization();
		s.parse( node );
		return s;
	}


	void parse(XdmNode node) {
		port 				=	XProcUtil.getAttrString(node, "port");
		// Encoding first
		encoding 			=	XProcUtil.getAttrString(node, "encoding", "UTF-8");
		
		byte_order_mark		= 	XProcUtil.getAttrBool(node, "byte-order-mark", 
			encoding.equalsIgnoreCase("UTF-16") ? true : false );
		
		
		cdata_section_elements = XProcUtil.getAttrList(node, "cdata-section-elements");
		
		doctype_public 		= XProcUtil.getAttrString(node, "doctype-public");
		doctype_system 		= XProcUtil.getAttrString(node, "doctype-system");
		
		escape_uri_attributes = XProcUtil.getAttrBool( node , "escape-uri-attributes", false );
		media_type 			= XProcUtil.getAttrString(node, "media-type");
		method 				= XProcUtil.getAttrQName(node, "method");
		normalization_form = XProcUtil.getAttrString(node, "normalization-form");
		omit_xml_declaration = XProcUtil.getAttrBool( node , "omit-xml-declaration",false);
		standalone 			= XProcUtil.getAttrString(node, "standalone");

		undeclare_prefixes 	= XProcUtil.getAttrBool( node , "undeclare-prefixes",false);
		version	= XProcUtil.getAttrString(node, "version");
		
		
		
		
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
