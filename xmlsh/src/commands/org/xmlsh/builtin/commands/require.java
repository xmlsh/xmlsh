/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.builtin.commands;

import java.util.List;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.Options.OptionDefs;
import org.xmlsh.core.Options;
import org.xmlsh.core.ThrowException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Version;
import org.xmlsh.sh.shell.Version.Op;
public class require extends BuiltinCommand {

    @Override
	protected int run(List<XValue> args) throws Exception {

		// Require with no args simply requires the require command exists
		// Added in version 1.0.1
		if( args.size() == 0 )
			return 0;
		
		OptionDefs defs = new OptionDefs(   "v=version,java=java-version,version-property:,property:,matches:,gt:,gte:,lt:,lte:,eq=equals:");
		Options opts = new Options(defs,SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		 
		String vtype = "version" ;
		String value = null;
		
	        
		if( opts.hasOpt("v")){
		    vtype = "version";
		    value = Version.getVersion();
		}
		else
		if( opts.hasOpt("java")){
		    vtype = "java";
		    value = Version.getJavaVersion();
		}
		else
		if( opts.hasOpt("version-property")){
		    vtype = "version-property";
		   value = Version.getProperty(opts.getOptStringRequired("version-property"));
		}
		else
		      if( opts.hasOpt("property")){
		            vtype = "property";
		            value = Version.getJavaProperty(opts.getOptStringRequired("property"));
		        }
	   Version.Op op = Op.GTE;
	   String matches = null;
		
		for( String sop : new String [] { "gt","lt","gte","lte","eq","matches" } ){
		   if( opts.hasOpt(sop)){
		       op =  Version.Op.getOp(sop);
		       matches = opts.getOptStringRequired(sop);
		       break ;
		   }
		}
		  
	
		int ret = 0;
		if( matches == null ){
		   
		    requires( !args.isEmpty(), "Expected version match argument");
		    matches = args.get(0).toString();
		}
		    requires( op != null , "Missing matching operator");
		    if( value == null )
		        value = Version.getVersion();
		     ret = Version.matches( value , matches , op ) ? 0 : 1;
		     
		
		if( ret == 0 )
			return 0;
		if( mShell.isInCommandConndition() )
			return 1;


		String err = "requires " + vtype + " "+ value + " " + op.name() + " " + matches ;  
		printErr(err);
		throw new ThrowException(XValue.newXValue(err) );



	}
}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
