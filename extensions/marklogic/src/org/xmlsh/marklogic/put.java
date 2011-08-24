package org.xmlsh.marklogic;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.commands.util.Checksum;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.marklogic.util.MLCommand;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentPermission;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.DocumentRepairLevel;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class put extends MLCommand {

	private static Logger mLogger = LogManager.getLogger(put.class);
	private ContentCreateOptions mCreateOptions;
	private ExecutorService mPool = null;
	private PrintWriter		mOutput = null;
	private boolean 		bVerbose = false ;
	
	private static class SumContent
	{
		String		mURI;
		Content		mContent ; 
		Checksum 	mSum; // Optional
		
		
		public SumContent( String uri, Content content , Checksum sum) {
			mURI  = uri ;
			mContent = content ;
			mSum = sum;
		}
		
	}
	
	
	private  class PutContent implements Runnable
	{
		List<SumContent> mContents;
		Session mSession;
		public PutContent(Session session, List<SumContent> contents) {
			mContents = contents ;
			mSession = session ;
		}

		@Override
		public void run() {
			 print("Thread: " + Thread.currentThread().getName() + " Writing " + mContents.size() + " files");

			Content[] contents = new Content[ mContents.size()];
			int i = 0;
			for( SumContent sc : mContents )
				contents[i++] = sc.mContent;
				
			try {
				mSession.insertContent( contents );

				setChecksums( mSession ,  mContents );
			} catch (RequestException e) {
				printError("Exception submitting data",e);
			}

			
			
		}
		
	}
	
	private 	List<SumContent> 	mContents = null;
	private		int					mMaxFiles = 1;
	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("v=verbose,c=connect:,md5,uri:,baseuri:,m=maxfiles:,r=recurse,d=mkdirs,t=text,b=binary,x=xml,maxthreads:,collection:+,forest:+,perm=permission:+,repair:,buffer:,language:,namespace:,quality:,+resolve,locale:");
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		String uri = opts.getOptString("uri", null);
		String baseUri = opts.getOptString("baseuri", "");
		mMaxFiles = Util.parseInt(opts.getOptString("m", "1"),1);
		boolean bRecurse = opts.hasOpt("r");
		boolean bMkdirs = opts.hasOpt("d");
		boolean bMD5    = opts.hasOpt("md5");
		bVerbose = opts.hasOpt("v");
		int maxThreads = Util.parseInt(opts.getOptString("maxthreads", "1"),1);
		
		
		SerializeOpts serializeOpts = getSerializeOpts( opts );
		
		/*
		 * If content type is speciried the use it otherwise default to system defaults
		 * 
		 */
		boolean bText = opts.hasOpt("t");
		boolean bBinary = opts.hasOpt("b");
		boolean bXml = opts.hasOpt("x");
		

		/*
		 * Get session before parsing args because some parsing requires to ping the server
		 * 
		 */
		mContentSource = getConnection(opts);
		mSession = mContentSource.newSession();
	
		
		
		
		if( bText )
			mCreateOptions = ContentCreateOptions.newTextInstance();
		else
		if( bBinary )
			mCreateOptions = ContentCreateOptions.newBinaryInstance();
		else
		if( bXml )
			mCreateOptions = ContentCreateOptions.newXmlInstance();
		else
			mCreateOptions = new ContentCreateOptions();
		
		/*
		 * Set additional create options
		 */
		List<XValue> collections = opts.getOptValues("collection");
		if( collections != null )
			mCreateOptions.setCollections( Util.toStringArray(collections));
		
		List<XValue> forests = opts.getOptValues("forest");
		if( forests != null)
			mCreateOptions.setPlaceKeys( parseForestIds(forests));
		
		List<XValue> perms = opts.getOptValues("permission");
		if( perms != null )
			mCreateOptions.setPermissions(toPermissionArray(perms));
		
		if( opts.hasOpt("repair"))
			mCreateOptions.setRepairLevel(parseRepair(opts.getOptStringRequired("repair")));
		
		if( opts.hasOpt("buffer"))
			mCreateOptions.setBufferSize(opts.getOptInt("buffer", ContentCreateOptions.MIN_BUFFER_SIZE));
		
		if( opts.hasOpt("language"))
			mCreateOptions.setLanguage(opts.getOptStringRequired("language"));
		
		if( opts.hasOpt("namespace"))
			mCreateOptions.setNamespace(opts.getOptStringRequired("namespace"));
		
		if( opts.hasOpt("resolve"))
			mCreateOptions.setResolveEntities(opts.getOptFlag("resolve", false));
		
		if( opts.hasOpt("quality"))
			mCreateOptions.setQuality(opts.getOptInt("quality", 0));
		
		if( opts.hasOpt("locale"))
			mCreateOptions.setLocale(new Locale( opts.getOptStringRequired("locale")));
		
		
		
		

		
		
		
			
		
		
		mOutput = getEnv().getStderr().asPrintWriter(serializeOpts);

		
		
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

			if( bMkdirs )
				createDirectories(args ,baseUri , bRecurse  );
			
			
			
			/*
			BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(maxThreads);
			mPool = new ThreadPoolExecutor(maxThreads, maxThreads, Integer.MAX_VALUE, TimeUnit.SECONDS, q);
			*/
			
	
				print("Starting thread pool of " + maxThreads + " threads");
				mPool = Executors.newFixedThreadPool(maxThreads);

				load( args , baseUri , bRecurse , bMD5 );
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


	private DocumentRepairLevel parseRepair(String repair) throws InvalidArgumentException {
		
		if( "none".equalsIgnoreCase(repair))
			return DocumentRepairLevel.NONE ;
		else
		if( "full".equalsIgnoreCase(repair))
			return DocumentRepairLevel.FULL ;
		else
		if( "default".equalsIgnoreCase(repair))
			return DocumentRepairLevel.DEFAULT ;
		else
			throw new InvalidArgumentException("Unknown repair level: " + repair + " expected none|full|default" );
		
	}


	private ContentPermission[] toPermissionArray(List<XValue> values) throws InvalidArgumentException {
		ContentPermission[] perms = new ContentPermission[values.size()];
		int i = 0;
		for( XValue v : values )
			perms[i++] = parsePermission( v.toString() );
		return perms;
	}


	private ContentPermission parsePermission(String string) throws InvalidArgumentException {
		
		StringPair pair = new StringPair(string,':');
		if( "r".equals(pair.getLeft()) || "read".equals(pair.getLeft()) )
			return ContentPermission.newReadPermission(pair.getRight());
		else
		if( "x".equals(pair.getLeft()) || "execute".equals(pair.getLeft()) )
			return ContentPermission.newExecutePermission(pair.getRight());
		else
		if( "u".equals(pair.getLeft()) || "update".equals(pair.getLeft()) )
			return ContentPermission.newUpdatePermission(pair.getRight());
		else
		if( "i".equals(pair.getLeft()) || "insert".equals(pair.getLeft()) )
			return ContentPermission.newInsertPermission(pair.getRight());
		else
			throw new InvalidArgumentException("Unable to parse permissin: " + string);
		
		
		
		
	}




	private void createDirectories(List<XValue> args, String baseUri , boolean bRecurse) throws IOException, RequestException {
		
		List<String> dirs = new LinkedList<String>();
		getFiles( dirs , args , baseUri , bRecurse );
		if( dirs.isEmpty())
			return ;
		
		createDirs(dirs);

		
	}


	private void getFiles(List<String> result , List<XValue> files, String baseUri , boolean bRecurse) throws IOException {
		for( XValue v : files ){	
			
			String fname = v.toString();
			File file = getFile(fname);
			
			if( file.isDirectory() ){
				String uri = baseUri + file.getName();
				result.add(uri + "/");
			
				if( bRecurse ){
					List<XValue> sub = new ArrayList<XValue>();
					for( String fn : file.list() ){
						sub.add(new XValue(fname + "/" + fn ));
					}
					getFiles( result , sub ,  uri + "/"  , bRecurse  );
					
				}
			}
		}
			
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


	public void load (InputPort port , String uri , boolean bMD5 ) throws CoreException, IOException
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
			if( bMD5 && ! is.markSupported() ){

				tempf = File.createTempFile("mlput", null);
				FileOutputStream out = new FileOutputStream(tempf);
				sum = Checksum.calcChecksum(is, out);

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
			
			try {
				// boolean bIsRewind = content.isRewindable();

				mSession.insertContent (content);

				
				if( bMD5 && sum != null ){
					List<SumContent> sc = new ArrayList<SumContent>(1);
					sc.add( new SumContent( uri , content , sum ));
					setChecksums( mSession, sc );
					
					
				}

			
			} catch (RequestException e) {
				printError("Exception submitting data",e);
			} finally {
				
				if( tempf != null )
					tempf.delete();
			}

				
			
		} finally {
			Util.safeClose(is);
				
		}
		
	}

	public void load (List<XValue> files , String baseUri,  boolean bRecurse,  boolean bMD5 ) throws CoreException, IOException
	{
		
		for( XValue v : files ){	
			
			String fname = v.toString();
			File file = getFile(fname);
			String uri = baseUri + file.getName() ;

			if( file.isDirectory() ){
				if( ! bRecurse ){
					print("Skipping directory: " + file.getName() );
					continue;
				}
				List<XValue> sub = new ArrayList<XValue>();
				for( String fn : file.list() ){
					sub.add(new XValue(fname + "/" + fn));
				}

				if( ! sub.isEmpty() )
					load( sub , uri + "/" , bRecurse ,  bMD5 );
				continue ;
				
			}
			Content content= ContentFactory.newContent (uri, file, mCreateOptions);
			
			putContent( uri , content , bMD5 ?Checksum.calcChecksum(file): null );
			
		}
		
		
	}


	private void putContent(String uri, Content content, Checksum checksum) throws  UnexpectedException {
		if( mContents == null )
			mContents = 	new ArrayList<SumContent>( mMaxFiles );

		
		
		mContents.add( new SumContent( uri , content , checksum ));
		if( mContents.size() >= mMaxFiles )
			flushContent();
		
		
		
	}

	
	private void flushContent()  
	{
		if( mContents == null )
			return ;
		
		if( ! mContents.isEmpty()){
			
			print("Submitting contents");
			mPool.execute(new PutContent(mSession , mContents) );

			
			
			
		}
		
		mContents = null ;
		
		
	}


	private  static  void setChecksums( Session session , List<SumContent> list ) throws RequestException
	{
		StringBuffer sQuery = new StringBuffer();
		for( SumContent sc : list )
			if( sc.mSum != null )
				sQuery.append( getChecksumString( sc.mURI , sc.mSum ));
			
		if( sQuery.length() == 0 )
			return ;
		sQuery.append("0");
		
		AdhocQuery request = session.newAdhocQuery ( sQuery.toString() );
		session.submitRequest(request).close();
		
		
	}
	
	

	private static String getChecksumString(String uri , Checksum sum) throws RequestException {
		
		return 
			"xdmp:document-set-property( " + quote(uri) +
			", <xmd5 md5='" + sum.getMD5() + 
			"' length='"+ String.valueOf(sum.getLength()) + "'/>),\n" ;
			
					
		
		
		
	}


	private void createDirs( List<String> dirs ) throws RequestException {
		printErr("Creating " + dirs.size() + " directories ...");
		StringBuffer sReq = new StringBuffer();
		for( String d : dirs ){
			String qd =  quote(d);
			
			sReq.append("if( exists(xdmp:document-properties(" + qd+ ")//prop:directory)) then () else xdmp:directory-create(" +qd + ");\n");
			
			
		}
		sReq.append("0");
		
		// printErr( sReq.toString() );
/*		
		AdhocQuery request = session.newAdhocQuery (
"declare variable $dirs as xs:string+ external;\n" +
"for $d in $dirs " +
"return xdmp:directory-create($d) \n" 
);
*/
	/*
		for( XdmValue d : dirs ){
			request.setNewVariable("dirs", ValueType.XS_STRING , d.asString() );
			ResultSequence rs = session.submitRequest (request);
			rs.close();
			
		}
		*/
/*
		
		request.setNewVariable( new XName("", "dirs"),  ValueFactory.newSequence(dirs.toArray(new XdmValue[dirs.size()])));
		

		// SequenceImpl seq = new SequenceImpl( dirs.toArray(new XdmValue[dirs.size()]));
	//	request.setNewVariable("dirs", ValueType.SEQUENCE, seq );
*/		
		AdhocQuery request = mSession.newAdhocQuery ( sReq.toString() );
		ResultSequence rs = mSession.submitRequest (request);

		rs.close();
		
	}
	
	
	
	
	protected void print( String str )
	{
		if( bVerbose ){
			mOutput.println(str);
			mOutput.flush();
		}
			
	}

	
	protected synchronized void printError( String error , Exception e )
	{		
		mOutput.println(error);
		if( e != null )
			mLogger.error( error , e );
		
		mOutput.flush();
	}


}



//
//
//Copyright (C) 2008,2009,2010,2011 , David A. Lee.
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
