/**
 * $Id: xfile.java 112 2009-01-04 23:49:22Z daldei $
 * $Date: 2009-01-04 18:49:22 -0500 (Sun, 04 Jan 2009) $
 *
 */

package org.xmlsh.commands.internal;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.List;


/** 
 * xuri exposes the java URI class as a shell command
 * 
 * Input is a filename either 
 * URI (string)
 * parent URI child URI
 * 
 * 
 */
public class xuri extends XCommand
{
	/*
	 * Get an arg and return null if empty
	 */
	private static String getArg( List<XValue> args , int num )
	{
		String value = args.get(num).toString();
		return Util.isEmpty(value) ? null : value ;

	}


	@Override
	public int run(  List<XValue> args  )	throws Exception
	{

		Options opts = new Options("a=authority,f=fragment,h=host,p=path,P=port,q=query,s=scheme,r=resource:,Q",SerializeOpts.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();

		SerializeOpts serializeOpts = getSerializeOpts(opts);
		PrintWriter out = getEnv().getStdout().asPrintWriter(serializeOpts);
		boolean bIsQuoted = opts.hasOpt("Q"); // query string is pre-quoted

		URI uri = null ;

		if( opts.hasOpt("r")){
			String res = opts.getOptString("r", "");
			URL url = this.getEnv().getShell().getResource(res);
			if( url == null )
				throw new InvalidArgumentException("Resource not found: " + res );

			uri = url.toURI();

		}

		String query ;

		if( uri == null )
			switch( args.size() ){
			case 	0:
				uri = getEnv().getCurdir().toURI();
				break;
			case	1:
				uri = new URI( getArg(args,0) ); 


				break ;

			case	2:
				uri = new URI( getArg(args,0));
				uri = uri.resolve(getArg(args,1));
				break;

			case	3:
				uri = new URI( 
						getArg(args,0), 
						getArg(args,1),
						getArg(args,2)
						); 
				break ;

			case	4:
				uri = new URI( 
						getArg(args,0), 
						getArg(args,1),
						getArg(args,2),
						getArg(args,3)
						); 

				break ;
			case	5:
				query = getArg(args,3);
				if( query == null )
					bIsQuoted = false  ;
				uri = new URI( 
						getArg(args,0), 
						getArg(args,1),
						getArg(args,2),
						bIsQuoted ? null :query , // query
								getArg(args,4)
						); 
				if( bIsQuoted  )
					uri = new URI( uri.toString() + "?" + query );
				break; 
			case	7:
				query = getArg(args,5);
				if( query == null )
					bIsQuoted = false ;

				uri = new URI( 
						getArg(args,0), 
						getArg(args,1),
						getArg(args,2),
						Util.parseInt(getArg(args,3),-1),
						getArg(args,4),
						bIsQuoted ? null : query , // query 
								getArg(args,6)
						); 
				if( bIsQuoted  )
					uri = new URI( uri.toString() + "?" +  query  );


				break ;
			}

		/* 
		 * if there is no scheme
		 * then treat as a FILE URI
		 */
		if( uri.getScheme() == null )
			uri = new File( getArg(args,0) ).toURI();



		if( opts.hasOpt("a"))
			out.println(uri.getAuthority() );
		else
			if( opts.hasOpt("f"))
				out.println(uri.getFragment() );
			else
				if( opts.hasOpt("h"))
					out.println(uri.getHost() );
				else
					if( opts.hasOpt("p"))
						out.println(uri.getPath() );
					else
						if( opts.hasOpt("P"))
							out.println(uri.getPort() );
						else
							if( opts.hasOpt("q")){

								out.println(uri.getQuery() );
							}
							else
								if( opts.hasOpt("s"))
									out.println(uri.getScheme() );		
								else
									if( opts.hasOpt("S"))
										out.println(uri.getSchemeSpecificPart());
									else
										out.println(uri);

		out.flush();


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
