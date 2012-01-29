/**
 * $Id: ls.java 346 2009-12-03 13:14:51Z daldei $
 * $Date: 2009-12-03 08:14:51 -0500 (Thu, 03 Dec 2009) $
 *
 */

package org.xmlsh.commands.posix;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

/**
 * Posix command ls
 * Rewritten from xquery to native java so that it can stream large lists efficiently
 * @author David A. Lee
 */

public class ls extends XCommand {

	
	private boolean opt_a = false ;
	private boolean opt_R = false ;
	private boolean opt_l = false ;
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("a=all,l=long,R=recurse", SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		

	      
		OutputPort stdout = getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		PrintWriter writer  = stdout.asPrintWriter(serializeOpts);
		
		
		if( args == null )
			args = new ArrayList<XValue>();
		if( args.size() == 0 )
			args.add(new XValue(""));
		
		opt_l = opts.hasOpt("l");
		opt_a = opts.hasOpt("a");
		opt_R = opts.hasOpt("R");
		int ret = 0;
		for( XValue arg : args ){
			
			// Must go to Shell API to get raw files
			String sArg = arg.toString();
			File dir = getEnv().getShell().getFile(sArg);
			if( dir == null || ! dir.exists() ){
				this.printErr("ls: cannot access " + sArg + " : No such file or directory" );
				ret++;
				continue;
			}
			
			list(writer, dir , null);
		}
		// writer.write(serializeOpts.getSequence_term());
		writer.close();
		
		return ret;
	}

	private void list(PrintWriter writer, File dir, String parent ) throws XMLStreamException, InvalidArgumentException {
		if( !dir.isDirectory() ){

			serialize(dir, writer, opt_l,parent);
		} else {
			
			if( parent != null  )
				serialize(dir, writer, opt_l,parent);
				
			
			if( parent == null || opt_R ){
				String p = parent == null ? "" : (parent  + dir.getName() + "/");
				File [] files =  dir.listFiles();
				
				
				Util.sortFiles(files);
				
				for( File f : files ){
					
					if( ! opt_a && f.getName().startsWith("."))
						continue;
					
					list( writer  , f , p    );
	
				}
			}
			

		}
	}

	private void serialize(File dir, PrintWriter writer, boolean optL, String parent) throws InvalidArgumentException {
		String name = ( parent != null ? parent : "" ) + dir.getName();
		if(  optL )
			writeFlags( writer , dir );
		
		writer.write( name );
	
		writer.write(this.getSerializeOpts().getSequence_sep());

			
		
	}

	private void writeFlags(PrintWriter writer, File f) {
		StringBuffer flags = new StringBuffer();
		flags.append(f.isDirectory() ? "d" : "-");
		flags.append(f.canRead() ? "r" : "-");
		flags.append(f.canWrite() ? "w" : "-");
		flags.append(f.canExecute() ? "x" : "-");
		flags.append(" ");
		
		long len = f.length();
		String slen = String.valueOf(len);
		slen = String.format("%1$10s", slen);
		flags.append(slen);
		flags.append(" ");
		
		String sDate = String.format("%1$tF %1$tT", new Date(f.lastModified()));
		flags.append(sDate);
		flags.append(" ");
		writer.write(flags.toString());
		
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
