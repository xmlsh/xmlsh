/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands;

import java.io.PrintWriter;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.types.XFile;


/** 
 * XFile implements the equivilent of basename and dirname in one.
 * 
 * Input is a filename either 
 * 1) FILE XML element
 * 2) filename 
 * 3) dir + filename
 * 4) dir + filename + ext
 * 
 * Methods are 0 or one of
 * 1) construct a filename from pieces
 * 2) replace a component of a filename
 * 
 * 
 * Output is either
 *  a component of a filename or full filename
 *  as either a string or a new XFile element 
 * 
 * 
 */
public class xfile extends XCommand
{
	
	
	
	
	
	public static void main( String args[] ) throws Exception
	{
		xfile cmd = new xfile();

		cmd.run( args );
		
		
		
	}

	
	
	
	public int run(  List<XValue> args , XEnvironment env )	throws Exception
	{
		Options opts = new Options("n,b,d,a,c,e",args);
		opts.parse();
		args = opts.getRemainingArgs();
		
		XFile xf = null ;
		switch( args.size() ){
		case	0:
			xf = new XFile( env.getCurdir() ); break;
		case	1:
			xf = new XFile( args.get(0) ); break;
		case	2:
			xf = new XFile( args.get(0).toString(), args.get(1).toString() ); break;
			
		}
		
		
		PrintWriter out = new PrintWriter( env.getStdout() );
		
		if( opts.hasOpt("b") )
			out.println( xf.getBaseName());
		else
		if( opts.hasOpt("n") )
				out.println( xf.getName());
		else
		if( opts.hasOpt("d"))
			out.println(xf.getDirName());
		else
		if( opts.hasOpt("a"))
			out.println(xf.getFile().getAbsolutePath());
		else
		if( opts.hasOpt("c"))
			out.println(xf.getFile().getCanonicalPath());
		else
		if( opts.hasOpt("e"))
			out.println( xf.getExt());
		else
			out.println( xf.getPath());
		out.flush();
			
				
		return 0;
		
		
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
