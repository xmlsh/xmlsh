/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.internal.commands;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;
import org.xmlsh.util.XFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class xls extends XCommand {


	private boolean opt_a = false ;
	private boolean opt_R = false ;
	private boolean opt_r = false ;
	private boolean opt_l = false ;
	@Override
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("a=all,l=long,R=recurse,r=relative", SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();




		OutputPort stdout = getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
		writer.writeStartDocument();

		String sDocRoot = "dir";
		writer.writeStartElement(sDocRoot);


		if( args == null )
			args = new ArrayList<XValue>();
		if( args.size() == 0 )
			args.add(XValue.newXValue(""));

		opt_l = opts.hasOpt("l");
		opt_a = opts.hasOpt("a");
		opt_R = opts.hasOpt("R");
		opt_r = opts.hasOpt("r");
		int ret = 0;
		for( XValue arg : args ){

			// Must go to Shell API to get raw files
			String sArg = arg.toString();
			File dir = getEnv().getShell().getFile(sArg);
			if( dir == null ||  ! dir.exists() ){
				this.printErr("ls: cannot access " + sArg + " : No such file or directory" );
				ret++;
				continue;
			}
			//			try {
			list(writer, dir , true);
			//			} catch (Exception e) {
			//				System.out.println(e.getMessage());
			//				e.printStackTrace();
			//			}
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
		stdout.writeSequenceTerminator(serializeOpts);


		return ret;
	}

	private void list(XMLStreamWriter writer, File dir, boolean top ) throws XMLStreamException {
		if( !dir.isDirectory() ){

			new XFile(dir).serialize(writer, opt_l, true, opt_r ? getEnv().getShell().getCurdir() : null);
		} else {

			if( ! top )
				new XFile(dir).serialize(writer, opt_l, false, opt_r ? getEnv().getShell().getCurdir() : null);


			if( top || opt_R ){
				File [] files =  dir.listFiles();

				//smcs 30/9/2012 -- check that files is not null before working with it
				if (files != null) {
					Util.sortFiles(files);

					for( File f : files ){

						if( ! opt_a && f.getName().startsWith("."))
							continue;

						list( writer  , f , false );
					}
				}
			}
			if( ! top )
				writer.writeEndElement();
		}
	}



}

//
//
//Copyright (C) 2008-2014    David A. Lee.
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
