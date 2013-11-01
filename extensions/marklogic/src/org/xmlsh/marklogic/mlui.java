/**
 * $Id: colon.java 245 2009-05-29 11:44:01Z daldei $
 * $Date: 2009-05-29 07:44:01 -0400 (Fri, 29 May 2009) $
 *
 */

package org.xmlsh.marklogic;

import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.ui.ExplorerShell;
import org.xmlsh.marklogic.util.MLCommand;
import org.xmlsh.sh.shell.SerializeOpts;

public class mlui extends MLCommand {

	
	boolean mTopShell = false ;
	
	
	public mlui()
	{
		
	}
	
	
	/*
	 * Special constructor for a top level shell which doesnt clone
	 */
	public mlui( boolean bTopShell )
	{
		mTopShell = bTopShell ;
	}
	
	
	
	public int run( List<XValue> args ) throws Exception {
			
		Options opts = new Options(SerializeOpts.getOptionDefs());
		opts.parse(args);
		new ExplorerShell( this.getShell() , opts ).run();
		return 0;
		
		
				
	}



}
//
//
//Copyright (C) 2008-2013    David A. Lee.
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
