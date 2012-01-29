/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.internal;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.XFile;
import org.xmlsh.util.Util;


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
	
	
	
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("n=name,b=base,d=dir,a=all,c=conanical,e=extension,B=basename,N,s=sys,u=uri",SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		XFile xf = null ;
		switch( args.size() ){
		case	0:
			xf = new XFile( getCurdir() ); break;
		case	1:
			xf = new XFile( mShell, args.get(0) ); break;
		case	2:
			xf = new XFile( args.get(0).toString(), args.get(1).toString() ); break;
		default : 
			
			usage("Unexpected argument");
			return 1;
		}
		
		boolean opt_sys = opts.hasOpt("s");
		
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		PrintWriter out = getStdout().asPrintWriter(serializeOpts);
		
		if( opts.hasOpt("b") )
			out.println( toSys(xf.getBaseName(),opt_sys));
		else
		if( opts.hasOpt("B"))
			out.println( toSys(xf.noExtention(),opt_sys));
		else
		if( opts.hasOpt("n") )
				out.println( toSys(xf.getName(),opt_sys));
		else
		if( opts.hasOpt("d"))
			out.println(toSys(xf.getDirName(),opt_sys));
		else
		if( opts.hasOpt("a"))
			out.println(toSys(xf.getFile().getAbsolutePath(),opt_sys));
		else
		if( opts.hasOpt("c"))
			out.println(toSys(xf.getFile().getCanonicalPath(),opt_sys));
		else
		if( opts.hasOpt("e"))
			out.println( xf.getExt());
		else
		if( opts.hasOpt("N"))
			out.println(toSys( xf.getPathName(),opt_sys));
		else
		if( opts.hasOpt("u"))
			out.println( xf.getFile().toURI().toString());
		else
			out.println( toSys(xf.getPath(),opt_sys));
		
		out.flush();
			
				
		return 0;
		
		
	}

	private String toSys(String name, boolean opt_sys) {
		return Util.convertPath(name, opt_sys);
			
	}
	
}

//
//
//Copyright (C) 2008,2009,2010,2011,2012 , David A. Lee.
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
