/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

public class SerializeOpts {
	private 	boolean		indent	= true ;
	private		boolean		omit_xml_declaration = true ;
	private		String		encoding = Shell.getXMLEncoding();
	
	public SerializeOpts() {}
	
	public SerializeOpts( SerializeOpts that ) {
		
		indent = that.isIndent();
		omit_xml_declaration = that.isOmit_xml_declaration();
		encoding = that.getEncoding();

	}
	
	public void set( String name , boolean value )
	{
		if( name.equals("omit-xml-declaration" ) )
			omit_xml_declaration = value;
		if( name.equals("indent"))
			indent = value ;
		
	}
	public void set( String name , String value )
	{
		if( name.equals("encoding"))
			encoding = value;
			
	}


	public boolean isIndent() {
		return indent;
	}

	
	public boolean isOmit_xml_declaration() {
		return omit_xml_declaration;
	}


	public String getEncoding() {
		return encoding;
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
