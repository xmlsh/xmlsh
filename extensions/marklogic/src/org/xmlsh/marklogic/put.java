package org.xmlsh.marklogic;


import java.io.File;
import java.io.IOException;
import java.util.List;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.util.MLCommand;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class put extends MLCommand {

	private Session session;

	private ContentCreateOptions options;

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("c=connect:,uri:",args);
		opts.parse();
		args = opts.getRemainingArgs();
		
		String uri = opts.getOptString("uri", null);
		
		InputPort in = null;
		if( args.size() > 0 )
			in = getInput( args.get(0));
		else
			in = getStdin();
		
		if( uri == null )
			uri = in.getSystemId();
		
		
		ContentSource cs = getConnection(opts);
	
	

		session = cs.newSession();
		this.load(in, uri);
		session.close();
		
		
		return 0;
	}


	/**
	 * Load the provided {@link File}s, using the provided URIs, into
	 * the content server.
	 * @param uris An array of URIs (identifiers) that correspond to the
	 *  {@link File} instances given in the "files" parameter.
	 * @param files An array of {@link File} objects representing disk
	 *  files to be loaded.  The {@link ContentCreateOptions} object
	 *  set with {@link #setOptions(com.marklogic.xcc.ContentCreateOptions)},
	 *  if any, will be applied to all documents when they are loaded.
	 * @throws RequestException If there is an unrecoverable problem
	 *  with sending the data to the server.  If this exception is
	 *  thrown, none of the documents will have been committed to the
	 *  contentbase.
	 */
	public void load (File file , String uri ) throws RequestException
	{

		Content content= ContentFactory.newContent (uri, file, options);
		

		session.insertContent (content);
	}

	public void load (InputPort port , String uri ) throws CoreException, IOException, RequestException
	{

		Content content= ContentFactory.newUnBufferedContent (uri, port.asInputStream(getSerializeOpts()), options);
		

		session.insertContent (content);
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
