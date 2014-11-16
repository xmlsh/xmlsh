/**
 * $Id: ls.java 346 2009-12-03 13:14:51Z daldei $
 * $Date: 2009-12-03 08:14:51 -0500 (Thu, 03 Dec 2009) $
 *
 */

package org.xmlsh.posix.commands;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;

/**
 * Posix command ls
 * Rewritten from xquery to native java so that it can stream large lists efficiently
 * @author David A. Lee
 */

public class wc extends XCommand {


	private boolean opt_l = false ;
	private boolean opt_c = false ;
	private boolean opt_w = false ;

	private	 int	total_lc = 0;
	private	 int	total_cc = 0;
	private	 int	total_wc = 0;
	private SerializeOpts  mSerializeOpts ;


	@Override
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("l,c,w", SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();

		mSerializeOpts = this.getSerializeOpts(opts);



		OutputPort stdout = getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		PrintWriter writer  = stdout.asPrintWriter(serializeOpts);


		if( args == null )
			args = new ArrayList<XValue>();
		if( args.size() == 0 )
			args.add(XValue.newXValue("-"));

		opt_l = opts.hasOpt("l");
		opt_c = opts.hasOpt("c");
		opt_w = opts.hasOpt("w");
		if( ! ( opt_l || opt_c || opt_w ))
			opt_l = opt_c = opt_w = true ;


		int ret = 0;
		for( XValue arg : args ){


			count(writer, arg );
		}
		if( args.size() > 1 )
			list( writer , total_lc , total_wc , total_cc , "total");
		// writer.write(serializeOpts.getSequence_term());
		writer.close();

		return ret;
	}

	private void count(PrintWriter writer, XValue file ) throws Exception {

		InputPort inp = getShell().getEnv().getInput(file);
		BufferedReader reader = new BufferedReader(inp.asReader( mSerializeOpts ));


		int lc=0,wc=0,cc=0;
		String line = null;
		while( (line=reader.readLine()) != null ){
			lc++;
			if( opt_c )
				cc += line.length();
			if( opt_w )
				wc = words( line );

		}
		reader.close();

		list( writer , lc , wc , cc , file.toString() );
		total_lc += lc;
		total_wc += wc;
		total_cc += cc ;

	}

	private int words(String line) {
		int w = 1; 
		for( char c : line.toCharArray() ){
			if( Character.isWhitespace(c))
				w++;

		}
		return w;
	}

	private void list(PrintWriter writer, int lc, int wc, int cc, String file) {
		StringBuffer flags = new StringBuffer(); 
		if( opt_l )
			flags.append( lc +  " ");
		if( opt_w )
			flags.append( wc + " ");
		if( opt_c )
			flags.append( cc + " ");

		if(! file.equals("-") )
			flags.append(file);
		writer.write(flags.toString());
		writer.write(mSerializeOpts.getSequence_sep());


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
