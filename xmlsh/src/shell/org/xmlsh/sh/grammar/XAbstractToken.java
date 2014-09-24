/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.grammar;

import java.util.Arrays;

public abstract class XAbstractToken
{
	protected int getKind() {
		assert(false);
		return 0;}
	private static int depth[] = new int[ ShellParserConstants.tokenImage.length ];
	public void enter(int kind) { 
		 assert( kind < depth.length ) ;
		 depth[kind]++ ; 
	}
	public boolean exit(int kind) { 
		 assert( kind < depth.length ) ;
		 assert( depth[kind] >= 0 );
         if( --depth[kind] < 0 ) {
        	 depth[kind] = 0; 
        	 return false ;
         }
         return true ;
	}
	static int get(int kind) {
		assert( kind < depth.length ) ;
		return depth[kind];
	}
	static void  clearAll()
	{
		Arrays.fill( depth , 0 );
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