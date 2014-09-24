/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

public enum CharAttr {
	ATTR_NONE( 0 ),
	ATTR_SOFT_QUOTE(1),
	ATTR_HARD_QUOTE(2),
	ATTR_PRESERVE(4),   // Do not touch, unquote or expand
	ATTR_ESCAPED(8);
	private int attr;
	CharAttr(){
		attr = 0;
	}
	CharAttr(int attr ){
		this.attr = attr ;
	}

	public boolean isQuote() { 
		return isSet( (ATTR_SOFT_QUOTE.attr|ATTR_HARD_QUOTE.attr ) ); 
	}
	public byte attr() { return (byte) attr ; }


	public static CharAttr valueOf(char c)
	{
		if( c == '\'')
			return ATTR_HARD_QUOTE ;
		else
			if( c == '\"')
				return ATTR_SOFT_QUOTE;
			else
				return ATTR_NONE ;
	}

	public boolean isSet( CharAttr ca ) {
		return ( attr & ca.attr) != 0;
	}
	public boolean isSet( int ca ) {
		return ( attr & ca ) != 0;
	}
	public void clear( CharAttr ca ) {
		attr &= ~ ca.attr;
	}
	public void set( CharAttr ca ) {
		attr |= ca.attr;
	}
	public static boolean isQuote( char c ) {
		return c == '\'' || c == '"';
	}
	public boolean isHardQuote()
	{
		return  isSet(ATTR_HARD_QUOTE);
	}
	public boolean isSoftQuote()
	{
		return  isSet(ATTR_SOFT_QUOTE);
	}
	public boolean isEscaped()
	{
		return  isSet(ATTR_ESCAPED);
	}
	public boolean isPreserve()
	{
		return isSet( ATTR_PRESERVE );
	}
}


/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */