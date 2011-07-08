/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.builtin;

import java.util.List;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellModuleURIResolver;
import org.xmlsh.util.Util;

public class xtype extends BuiltinCommand {
	public int run(  List<XValue> args ) throws Exception 
	{
		Options opts = new Options( SerializeOpts.getOptionDefs());
		opts.parse(args);
		
		Processor  processor  = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		compiler.setModuleURIResolver(new ShellModuleURIResolver(mShell));
		
		XQueryExecutable expr = null;

		expr = compiler.compile( 
		"import module namespace functx = 'http://www.functx.com' ;\n"+ 
		"declare variable $A external;\n" + 
		"functx:sequence-type($A)"
				
		);
		
		

		XQueryEvaluator eval = expr.load();	
		
		QName vqname = Util.resolveQName( "A", null );
		
		SerializeOpts serializeOpts = mShell.getSerializeOpts(opts);
		OutputPort stdout = mShell.getEnv().getStdout();
		Destination ser = stdout.asDestination(serializeOpts);
		boolean bFirst = true ;
		boolean bAnyOut = false ;
		
		for( XValue arg : args ){
			
			if( arg.isObject() || arg.isNull() ){
				if( ! bFirst )
					stdout.writeSequenceSeperator(serializeOpts); // Thrashes variable output !
				bFirst = false ;
				if( arg.isNull() )
					Util.writeXdmValue(new XValue("null").asXdmValue(), ser);
				else
					Util.writeXdmValue(new XValue(arg.asObject().getClass().getName()).asXdmValue(), ser);
				bAnyOut = true ;
			}
				
			else {
				eval.setExternalVariable( vqname , arg.asXdmValue()  );
				
			
				for( XdmItem item : eval ){
					bAnyOut = true ;
					if( ! bFirst )
						stdout.writeSequenceSeperator(serializeOpts); // Thrashes variable output !
					bFirst = false ;
					
					
					if( item instanceof XdmNode ){
						XdmNode node = (XdmNode) item ;
						
					}
					
					
					//processor.writeXdmValue(item, ser );
					Util.writeXdmValue(item, ser);
		
					
				}
			}
				
		}
		if( bAnyOut )
			stdout.writeSequenceTerminator(serializeOpts); // write "\n"
		

		return 0;
		
		
		
		
	}

}



//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
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
