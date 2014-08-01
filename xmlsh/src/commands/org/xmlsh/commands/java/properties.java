/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.java;

import org.xmlsh.core.BuiltinFunctionCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.StringPair;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class properties extends BuiltinFunctionCommand
{

	private SerializeOpts sopts;

	public properties()
	{
		super("properties");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception
	{

		Options opts = new Options("t=text,x=xml,f=file,m=map,v=variables,p=pairs");
		opts.parse(args);

		boolean bText = opts.hasOpt("t");   // parse in text format
		boolean bXml = opts.hasOpt("x");    // parse in XML format
		boolean bMap = opts.hasOpt("m");    // parse in Map format
		boolean bVars = opts.hasOpt("v");   // parse pairs of vars
		boolean bPairs = opts.hasOpt("p");  // parse a=b pairs
		boolean bFile  = opts.hasOpt("file") ;  // input from file 

		args = opts.getRemainingArgs();

		Properties props = new Properties();

		if( args.isEmpty() )
			return newValue(props);



		sopts = shell.getSerializeOpts();
		if( bText || bXml ) {

			try( InputStream is = getInputStream( shell , bFile , args , sopts )){
				if( bText )
					props.load(is);
				else
					props.loadFromXML(is);

			}
		}
		else	

			if( bVars ) {
				for( int i = 0 ; i < args.size() - 1 ; i+=2 ) {
					props.put( args.get(i).toString() , args.get(i+1).toString());
				}
			}
			else
				if( bPairs ) {
					for (XValue add : args ){
						StringPair pair = new StringPair( add.toString() , '=');
						props.setProperty( pair.getLeft(), pair.getRight() );
					}
				}

		return newValue(props);

	}

	private InputStream getInputStream(Shell shell , boolean bFile , List<XValue> args, SerializeOpts sopts) throws InvalidArgumentException, IOException, CoreException
	{
		if( bFile   )
			return shell.getEnv().getInput( args.get(0) ).asInputStream(sopts);
		else {
			XValue a = args.get(0);
			return new ByteArrayInputStream( a.toByteArray(sopts));

		}
	}

	public XValue newValue(Properties props)
	{
		return new XValue( TypeFamily.JAVA , props );
	}


}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */