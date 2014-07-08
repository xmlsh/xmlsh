/**
 * $Id: colon.java 21 2008-07-04 08:33:47Z daldei $
 * $Date: 2008-07-04 04:33:47 -0400 (Fri, 04 Jul 2008) $
 *
 */

package org.xmlsh.commands.builtin;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.Version;

import java.util.List;

import javax.xml.stream.XMLStreamWriter;

public class xversion extends BuiltinCommand {

	
	public int run(  List<XValue> args ) throws Exception {
	
	      
		OutputPort stdout = mShell.getEnv().getStdout();
		Options opts = new Options(SerializeOpts.getOptionDefs());
		opts.parse(args);
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		
		
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
	
		writer.writeStartDocument();

			
		final String sBuild = "build";
		final String sRelease = "release";
		final String sVersion = "version";
		final String sDocRoot = sVersion;
		final String sSaxon = "saxon_version";
		final String sSaxonEdition = "saxon_edition";
		
	
		writer.writeStartElement(sDocRoot);
		writer.writeAttribute(sBuild, Version.getBuildDate());
		writer.writeAttribute(sRelease, Version.getRelease() );
		writer.writeAttribute(sVersion, Version.getVersion() );
		writer.writeAttribute(sSaxon, Shell.getProcessor().getSaxonProductVersion() );
		writer.writeAttribute(sSaxonEdition, Shell.getProcessor().getUnderlyingConfiguration().getEditionCode() );
		

		
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		stdout.writeSequenceTerminator(serializeOpts);
		
		return 0;
		
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
