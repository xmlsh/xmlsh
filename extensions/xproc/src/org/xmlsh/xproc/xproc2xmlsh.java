/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc;

import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.xproc.compiler.OutputContext;
import org.xmlsh.xproc.compiler.XProcCompiler;

public class xproc2xmlsh extends XCommand {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("b=base:",args);
		opts.parse();
		args = opts.getRemainingArgs();
		
		Source input = getStdin().asSource(getSerializeOpts());
		String base = opts.getOptString("base", input.getSystemId());
		
		input.setSystemId(base);
		
		Processor  processor  = Shell.getProcessor();
		DocumentBuilder builder = processor.newDocumentBuilder();
		XdmNode doc = builder.build(  input );
		
		
		
		XProcCompiler xp = new XProcCompiler( );
		
		PrintWriter w = new PrintWriter(getStdout().asOutputStream());
		xp.parse(doc);

		OutputContext c = new OutputContext();
		
		xp.serialize(c);
		
		c.serialize( w );
		
		
		w.flush();
		w.close();
		
		
		
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
