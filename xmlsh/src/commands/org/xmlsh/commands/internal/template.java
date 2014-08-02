/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XValueMap;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.types.XTypeUtils;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.List;
import java.util.Properties;

public class template extends XCommand
{
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( "t=template:,f=format:,V:,v,Vf:,vf:" , SerializeOpts.getOptionDefs());
		opts.parse(args);
		setSerializeOpts( opts );

		String format = opts.getOptString("format", "text");
		InputPort template = opts.hasOpt("template") ?
				mShell.getEnv().getInput( opts.getOptValue("template") ):
					mShell.getEnv().getStdin();


				OutputPort out = mShell.getEnv().getStdout();

				XValueMap variables = new XValueMap();

				if( opts.hasOpt("V") )
					variables.addAll( parseMap( opts.getOptValue("V") ));
				//if( opts.hasOpt("Vf") )
				//	variables.addAll( parseValueMap( mShell.getEnv().getInput( opts.getOptValue("Vf") ) ) );
				if( opts.hasOpt("vf") )
					variables.addAll( parseTextMap( mShell.getEnv().getInput( opts.getOptValue("vf") ) ) );

				args = opts.getRemainingArgs();
				for( int i = 0 ; i < args.size() - 1 ; i+=2 ) {
					variables.put( args.get(i).toString() , args.get(i+1));
				}



				runTemplate( template , out , format , variables  );


				return 0;
	}

	private boolean runTemplate(InputPort template, OutputPort out, String format, XValueMap variables) throws IOException, CoreException
	{

		StrSubstitutor  sub = new StrSubstitutor( variables );
		try ( InputStream is = template.asInputStream(getSerializeOpts()) ;
				Writer w = out.asPrintWriter(getSerializeOpts()) 	;
				){

			StringBuilder b = new StringBuilder(
					Util.readString(is, getSerializeOpts().getInput_text_encoding() ) );

			boolean result = sub.replaceIn(b);

			w.write( b.toString());

			w.flush();
			return result ;

		}

	}

	private XValueMap parseTextMap(InputPort input) throws IOException, CoreException
	{
		Properties props =new Properties();
		try ( InputStream is = input.asInputStream(getSerializeOpts()) ){
			props.load(is);
			return XTypeUtils.newMapFromProperties( props );
		}
	}

	/*
	private XValueMap parseValueMap(InputPort input)
    {
		Properties props;
		try ( InputStream is = input.asInputStream(getSerializeOpts()) ){
			props.load(is);
			return XValueMap.fromInput( input );
		}

    }
	 */
	private XValueMap parseMap(XValue value ) throws InvalidArgumentException
	{
		return XTypeUtils.newMapFromValue( value );
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