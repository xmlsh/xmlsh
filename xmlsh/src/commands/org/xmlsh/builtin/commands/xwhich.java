/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.builtin.commands;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CommandFactory;
import org.xmlsh.core.FunctionCommand;
import org.xmlsh.core.ICommand;
import org.xmlsh.core.IFunctionDecl;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.ScriptCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.IModule;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.io.File;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

public class xwhich extends BuiltinCommand {


	private static final String typenames[] = new String[] {
		"builtin" , "internal" , "user" , "external" , "script" , "function"

	};

	@Override
	public int run(   List<XValue> args ) throws Exception {

		Options opts = new Options( "n" , SerializeOpts.getOptionDefs());
		opts.parse(args);

		boolean bNoWrite = opts.hasOpt("n");



		List<XValue> xvargs = Util.expandSequences(opts.getRemainingArgs());

		if( xvargs.size() < 1  ){
			mShell.printErr("usage: " + getName() + " command ...");
			return 1;
		}

		final  String sDocRoot = getName();

		XMLStreamWriter 	out = null ;
		OutputPort stdout = mShell.getEnv().getStdout();	

		SerializeOpts serializeOpts = getSerializeOpts(opts);
		if( !bNoWrite ){

			out = stdout.asXMLStreamWriter(serializeOpts);
			out.writeStartDocument();
			out.writeStartElement(sDocRoot);

		}

		int bad = 0;

		final  String sCmd = "command";
		final	String sName = "name";
		final 	String sType = "type";
		final  String sPath = "path";
		final String sModule = "module";


		for( XValue xname : xvargs ){

			String name = xname.toString();
			ICommand command = CommandFactory.getInstance().getCommand(mShell , name , getLocation() );

			// Try builtin functions 
			if( command == null ) {
				IFunctionDecl func = CommandFactory.getInstance().getBuiltinFunction(mShell, name,  getLocation() );
				if( func != null ) {
					if( ! bNoWrite ){

						out.writeStartElement(sCmd);
						out.writeAttribute(sName, name );
						String type = "builtin-function";
						out.writeAttribute(sType, type );
						out.writeEndElement();
					}
					continue;
				}

			}

			if( command != null ){
				if( ! bNoWrite ){




					out.writeStartElement(sCmd);
					out.writeAttribute(sName, name);

					String type = typenames[command.getType().ordinal()];
					out.writeAttribute(sType, type);

					File file = command.getFile();
					if( file != null )
						out.writeAttribute(sPath, file.getCanonicalPath() );
					else 
						if( command instanceof ScriptCommand ){
							ScriptCommand sc  = (ScriptCommand) command;
							out.writeAttribute(sPath, sc.getScriptName());
						}
						else
							if( command instanceof FunctionCommand ) {
								FunctionCommand fc = (FunctionCommand) command;
								out.writeAttribute(sName, type);
							}
					IModule module = command.getModule();
					if( module != null )
						out.writeAttribute(sModule, module.getName());
					out.writeEndElement();

				}
			} else
				bad++;

		}
		if( ! bNoWrite ){
			out.writeEndElement();
			out.writeEndDocument();
			out.flush();
			out.close();
			stdout.writeSequenceTerminator(serializeOpts);
		}


		return bad;



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
