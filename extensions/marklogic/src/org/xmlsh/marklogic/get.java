package org.xmlsh.marklogic;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FileOutputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.util.MLCommand;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class get extends MLCommand {



	private ExecutorService mPool = null;
	private PrintWriter		mOutput = null;
	private boolean		bMkdirs = false ;
	
	private static class SumContent
	{
		String			mURI;			// source
		OutputPort		mOutput;		// target
		
		
		public SumContent( String uri, OutputPort out ) {
			mURI  = uri ;
			mOutput = out ; 
		}
		
	}
	
	
	
	private  class GetContent implements Runnable
	{
		List<SumContent> mContents;
		Session mSession;
		
		public GetContent(Session session, List<SumContent> contents) {
			mContents = contents ;
			mSession = session ;
		}

		@Override
		public void run() {
			 print("Thread: " + Thread.currentThread().getName() + " Writing " + mContents.size() + " files");

	
				
			try {
				for( SumContent sc : mContents )
					getContent( sc );

				
			} catch (Exception e) {
				printError("Exception getting data",e);
			}

			
			
		}

		private void getContent(SumContent sc) throws RequestException, FactoryConfigurationError, IOException, XMLStreamException, SaxonApiException, CoreException {
			get.this.getContent( mSession , sc.mURI , sc.mOutput , true , true );

			
		}
		
	}
	
	private 	List<SumContent> 	mContents = null;
	private		int					mMaxFiles = 1;
	private SerializeOpts mSerializeOpts;
	
	
	
	/**
	 * 
	 *  get [-baseuri base] uri				# single to stdout
	 *  get [-baseuri base] uri uri ... 	# 1+ to stdout
	 *  get -r [-baseuri base] uri uri ...  # 1+ to stdout 
	 *  get -d dir ..						# 1+ to dir
	 *  get -o file ... 					# 1+ to file 
	 */

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("v=verbose,c=connect:,baseuri:,m=maxfiles:,r=recurse,maxthreads:,t=text,binary,d=directory:,o=output:");
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		String baseUri = opts.getOptString("baseuri", "");
		mMaxFiles = Util.parseInt(opts.getOptString("m", "1"),1);
		
		boolean bRecurse = opts.hasOpt("r");
		bMkdirs = opts.hasOpt("d");
		boolean bText = opts.hasOpt("t");
		boolean bBinary = opts.hasOpt("binary");
		String	 dirName = opts.getOptString("d", null);
		File 	outDir = (Util.isBlank(dirName) ? null : getFile(dirName));
		XValue 	outName = opts.getOptValue("o");
		
		bVerbose = opts.hasOpt("v");
		
		
		int maxThreads = Util.parseInt(opts.getOptString("maxthreads", "1"),1);
		
		
		mSerializeOpts = getSerializeOpts( opts );
		
		
			
		mContentSource = getConnection(opts);
		mSession = mContentSource.newSession();
		
		
		mOutput = getEnv().getStderr().asPrintWriter(mSerializeOpts);

		
		/*
		 * If only 1 arg and no -r then get inline
		 */
		
		if( args.size() == 0 ){
			usage();
			return 1;
		}
		
		if( args.size() == 1 && ! bRecurse ){
			// If -d then resolve to directory
			String uri = args.get(0).toString();
			OutputPort out = getOutputPort( uri , outDir , outName  );
			
			getContent( mSession,   resolveUri( baseUri , uri ) , out , bText , bBinary );
			
			
			
		}
		else 
		{
	
				print("Starting thread pool of " + maxThreads + " threads");
				mPool = new ThreadPoolExecutor(maxThreads, maxThreads,
	                    0L, TimeUnit.MILLISECONDS,
	                    new LinkedBlockingQueue<Runnable>(maxThreads * 2 ), new ThreadPoolExecutor.CallerRunsPolicy() );

				getContent( Util.toStringList(args) , baseUri , outDir,   bRecurse  );
				flushContent();

		
		}
		
		
		/* 
		 * Wait for all tasks to complete
		 */
		if( mPool != null ){
			print("Waiting for tasks to complete");
			mPool.shutdown();
		
			mPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS ) ;
		}
		
		
		mSession.close();
		print("Complete");
		mOutput.close();
		
		return 0;
	}


	private String resolveUri(String baseUri, String uri) {
		if(Util.isEmpty(baseUri) )
			return uri ;
		
		StringBuffer sb = new StringBuffer( baseUri );
		if( ! baseUri.endsWith("/"))
			sb.append('/');
		
		if( uri != null ){
			if( uri.startsWith("/"))
				uri = uri.substring(1);
			sb.append(uri);
		}
		
		return sb.toString();
			
		
		
	}


	/*
	 * Resolve an output name to a output file 
	 */
	private OutputPort getOutputPort(String uri, File outDir, XValue outName) throws CoreException, IOException {
		// Specific name - resolve to directory ignore uri and baseUri
		if( outName != null ){
			if( outDir == null )
				return getOutput( outName , false );
			else
				return new FileOutputPort( this.mShell.getExplicitFile(outDir, outName.toString() , false ), false , false );
			
			
		}
		else
		if( outDir == null )	
			return getStdout();
		
		
		// No output name - use uri output relative to outDir
		return new FileOutputPort( this.mShell.getExplicitFile(outDir , uri , false ) , false , false  );
		
	}



	private void getContent(Session session , String uri , OutputPort output , boolean bText , boolean bBinary ) throws RequestException, FactoryConfigurationError, IOException, XMLStreamException, SaxonApiException, CoreException {
		
		if( bMkdirs && output.isFile()  ){
			output.getFile().getParentFile().mkdirs();
			
		}
		
		RequestOptions options = null ; 
		AdhocQuery request = session.newAdhocQuery (
				"declare variable $doc external;" +
				"fn:doc($doc)"
		
		);
		request.setNewStringVariable("doc", uri );
		request.setOptions (options);

	    ResultSequence rs = session.submitRequest (request);
	    writeResult(rs, output , mSerializeOpts, bText , bBinary );
	    rs.close();
	    
	    if( ! bText && ! bBinary )
	    	output.writeSequenceTerminator(mSerializeOpts);
	    
	    output.release();
	
		
	}
	




	
	public void getContent(List<String> uris , String baseUri , File outDir , boolean bRecurse  ) throws CoreException, IOException, RequestException
	{
		
		for( String uri : uris ){	
			if( bRecurse ){
				List<String> children = list( resolveUri(baseUri , uri ) );
				for( String child : children ){
					// Strip off baseUri 
					String childName = child.substring( baseUri.length() );
					
					OutputPort out = getOutputPort( childName , outDir , null );
					getContent( child , out );
					
					
				}
				return ;
			}
			
			

			OutputPort out = getOutputPort( uri , outDir , null );
			
						
			getContent( resolveUri( baseUri , uri ) , out );
			
		}
		
		
	}


	private List<String> list(String uri) throws RequestException {
		
		
		RequestOptions options = null ; 
		AdhocQuery request = mSession.newAdhocQuery (
				" xquery version \"1.0-ml\"; " + 
				" declare variable $dir external; " + 
				" for $d in xdmp:directory($dir,'infinity')/base-uri() " + 
				" order by $d " +
				" return $d "
		
		);
		
		request.setNewStringVariable("dir", uri);
		request.setOptions (options);

	    ResultSequence rs = mSession.submitRequest (request);
	    String[] result = rs.asStrings();
	    
	    rs.close();
	    return Arrays.asList(result);
		
		
	}


	private void getContent(String uri, OutputPort out ) throws  UnexpectedException {
		if( mContents == null )
			mContents = 	new ArrayList<SumContent>( mMaxFiles );

		
		
		mContents.add( new SumContent( uri , out ));
		if( mContents.size() >= mMaxFiles )
			flushContent();
		
		
		
	}

	
	private void flushContent()  
	{
		if( mContents == null )
			return ;
		
		if( ! mContents.isEmpty()){
			
			print("Getting contents...");
			mPool.execute(new GetContent(mSession , mContents) );
			
			
		}
		
		mContents = null ;
		
		
	}

	
	

	
	
	


}



//
//
//Copyright (C) 2008-2013    David A. Lee.
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
