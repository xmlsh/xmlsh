package org.xmlsh.marklogic;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlsh.commands.util.Checksum;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.util.MLCommand;
import org.xmlsh.util.Util;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class put extends MLCommand {

	private Session session;

	private ContentCreateOptions mCreateOptions;
	
	private static class SumContent
	{
		String		mURI;
		Checksum mSum;
		public SumContent( String uri, Checksum sum) {
			mURI  = uri ;
			mSum = sum;
		}
		
	}

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("c=connect:,md5,uri:,baseuri:,m=maxfiles:,r=recurse,d=mkdirs,t=text,b=binary,x=xml");
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		String uri = opts.getOptString("uri", null);
		String baseUri = opts.getOptString("baseuri", "");
		int  maxFiles = Util.parseInt(opts.getOptString("m", "1"),1);
		boolean bRecurse = opts.hasOpt("r");
		boolean bMkdirs = opts.hasOpt("d");
		boolean bMD5    = opts.hasOpt("md5");
		
		
		/*
		 * If content type is speciried the use it otherwise default to system defaults
		 * 
		 */
		boolean bText = opts.hasOpt("t");
		boolean bBinary = opts.hasOpt("b");
		boolean bXml = opts.hasOpt("x");
		
		if( bText )
			mCreateOptions = ContentCreateOptions.newTextInstance();
		else
		if( bBinary )
			mCreateOptions = ContentCreateOptions.newBinaryInstance();
		else
		if( bXml )
			mCreateOptions = ContentCreateOptions.newXmlInstance();
		
			
		ContentSource cs = getConnection(opts);
	
		// printErr("maxfiles is " + maxFiles );

		session = cs.newSession();
		
		if( args.size() == 0 || (args.size() == 1 && baseUri.equals("") ) ){
			InputPort in = null;
			if( args.size() > 0 )
				in = this.getInput(args.get(0));
			else
				in = getStdin();
			
			if( uri == null )
				uri = in.getSystemId();
			try {
				this.load(in, uri,bMD5);
			} finally {
				in.close();
			}
			
		}
		else {
			
			if(! baseUri.equals("") && ! baseUri.endsWith("/") )
				baseUri = baseUri + "/";

			
			int start = 0 ;
			int end = args.size() ;
			
			while( start < end ){
				int last = start + maxFiles;
				if( last > end )
					last = end ;
				
				load( args.subList(start, last), baseUri , bRecurse, bMkdirs , bMD5 );
				start += maxFiles ;
				
				
			}
			
			
		
		}

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

		Content content= ContentFactory.newContent (uri, file, mCreateOptions);
		

		session.insertContent (content);
	}

	public void load (InputPort port , String uri , boolean bMD5 ) throws CoreException, IOException, RequestException
	{
		
		InputStream is = port.asInputStream(getSerializeOpts());
		
		try {
			/*
			 * if Not rewindable then make a temp file
			 * 
			 */
			Checksum sum = null ;
			Content content = null ;
			File tempf = null;
			if( ! is.markSupported() ){

				tempf = File.createTempFile("mlput", null);
				FileOutputStream out = new FileOutputStream(tempf);
				if( bMD5 )
					sum = Checksum.calcChecksum(is, out);
				else
					Util.copyStream(is,out);
				is.close();
				is = null;
				out.close();
				
				content= ContentFactory.newContent (uri, tempf, mCreateOptions);

			} else {
				if( bMD5 ){
					is.mark(Integer.MAX_VALUE);
					sum = Checksum.calcChecksum(is);
					is.reset();
				}
				content = ContentFactory.newContent(uri, is, mCreateOptions);
			}
			

			session.insertContent (content);
				
			if( bMD5 && sum != null )
				setChecksum(uri,sum);
		} finally {
			Util.safeClose(is);
				
		}
		
	}

	public void load (List<XValue> files , String baseUri,  boolean bRecurse, boolean bMkdirs, boolean bMD5 ) throws CoreException, IOException, RequestException
	{
		printErr("Putting " + files.size() + " files to " + baseUri );
		List<Content>	contents = new ArrayList<Content>(files.size());
		List<SumContent> sums = bMD5 ? new ArrayList<SumContent>(files.size()) : null ;
		
		
		int i = 0;
		for( XValue v : files ){	
			
			String fname = v.toString();
			File file = getFile(fname);
			String uri = baseUri + file.getName() ;

			if( file.isDirectory() ){
				if( ! bRecurse ){
					printErr("Skipping directory: " + file.getName() );
					continue;
				}
				List<XValue> sub = new ArrayList<XValue>();
				for( String fn : file.list() ){
					sub.add(new XValue(fname + "/" + fn));
				}
				if( bMkdirs )
					createDir( uri + "/" );
				if( ! sub.isEmpty() )
					load( sub , uri + "/" , bRecurse , bMkdirs ,  bMD5 );
				continue ;
				
			}
			Content content= ContentFactory.newContent (uri, file, mCreateOptions);
			contents.add(content);
			if( bMD5 )
				sums.add( new SumContent(  uri , Checksum.calcChecksum(file)));
			
		}
		
		if( ! contents.isEmpty() )
			session.insertContent (contents.toArray(new Content[ contents.size()]));

		if( bMD5 && ! sums.isEmpty())
			setChecksum( sums );
		
	}


	private void setChecksum(List<SumContent> sums) throws RequestException {
		for( SumContent  sum : sums )
			setChecksum( sum.mURI , sum.mSum);
		
	}


	private void setChecksum(String uri , Checksum sum) throws RequestException {
		
		AdhocQuery request = session.newAdhocQuery (
"declare variable $uri  as xs:string external ;\n" +
"declare variable $md5 as xs:string external;\n" +
"declare variable $length as xs:integer external;\n" +
"xdmp:document-set-property( $uri , <xmd5 md5='{$md5}' length='{$length}'/>)"
);


		request.setNewStringVariable("uri", uri );
		request.setNewStringVariable("md5" , sum.getMD5() );
		request.setNewIntegerVariable("length", sum.getLength() );
		session.submitRequest(request);
		
		
		
	}


	private void createDir(String uri) throws RequestException {
		printErr("Making directory: " + uri );
		AdhocQuery request = session.newAdhocQuery ("xdmp:directory-create('" + uri + "')" );
		ResultSequence rs = session.submitRequest (request);

        // writeResult(rs, out, asText );
		
	}


}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
