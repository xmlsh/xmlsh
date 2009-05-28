/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.builtin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Module;
import org.xmlsh.sh.shell.Modules;
import org.xmlsh.util.StringPair;

public class ximport extends BuiltinCommand {

	
	public int run(  List<XValue> args ) throws Exception {
		if( args.size() < 1 )
			return 1;
		
		XValue what = args.remove(0);

		if( what.toString().equals("module"))
			return importModule( args );
		else
		if( what.toString().equals("package"))
			return importPackage(args);
		
		return 2;
				
	}
	/*
	 * Implements 
	 *    import module a.b.c
	 *    import module foo=a.b.c
	 *    import module foo=a.b.c at jar-file
	 *    
	 */

	private int importModule(List<XValue> args) throws CoreException {
		if( args.size() == 0 )
			return listModules();
		
		String mod = args.remove(0).toString();
		
		
		mShell.importModule(mod , args );

		return 0;
	}
	
	/*
	 * import package name foo.bar.spam
	 * import package foo.bar.spam
	 */

	private int importPackage(List<XValue> args) throws CoreException {
		
		String name = null; 
		String pkg = null;
		String prefix = null;
		
		if( args.size() == 1 )
		{
			pkg = args.get(0).toString();
			name = pkg ;
		} 
		else {
		
			if( args.size() != 2)
				return -1;
			name = args.remove(0).toString(); 
			pkg = args.remove(0).toString();
		}
		
		/* parse package for prefix=package */
		StringPair 	pair = new StringPair(pkg,'=');
		if( pair.hasLeft() ){
			prefix = pair.getLeft();
			pkg = pair.getRight();
		}
		
		
		
		mShell.importPackage(prefix , name, pkg);
		
		
		return 0;
	}

	
	
	private int listModules() {
		Modules modules = mShell.getModules();
		if( modules == null )
			return 0;
		
		for( Module m : modules ){
			String prefix = m.getPrefix();
			if( prefix == null )
				mShell.printOut( m.getName() );
			else
				mShell.printOut( prefix + "=" + m.getName() ); 

			
		}
		return 0;
		
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
