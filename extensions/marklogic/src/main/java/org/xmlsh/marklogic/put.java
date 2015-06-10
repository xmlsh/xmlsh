package org.xmlsh.marklogic;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.saxon.s9api.XdmItem;

import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.IXdmItemInputStream;
import org.xmlsh.marklogic.util.MLCommand;
import org.xmlsh.marklogic.util.MLUtil;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;
import org.xmlsh.util.commands.Checksum;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentPermission;
import com.marklogic.xcc.DocumentRepairLevel;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public class put extends MLCommand {


	

	private ContentCreateOptions mCreateOptions;
	private ThreadPoolExecutor mPool = null;

	
	private static class SumContent
	{
		String		mURI;
		Content		mContent ; 
		Checksum 	mSum; // Optional
		File		mDeleteFile; // optinal
		
		
		public SumContent( String uri, Content content , Checksum sum, File deleteFile ) {
			mURI  = uri ;
			mContent = content ;
			mSum = sum;
			mDeleteFile = deleteFile;
		}
		
	}
	
	private  abstract class  ContentIterator
	{
		
		abstract boolean hasNext() throws IOException, CoreException;
		abstract Content next(String baseUri);
		abstract boolean canReset();
		abstract void reset() throws UnimplementedException;
		abstract boolean isDirectory() ;
		abstract File getFile();
		abstract String getUri(String baseURI);

	}
	private  abstract class  ContentFilenameIterator extends ContentIterator
	{
		protected File				mFile = null ;
		private   String			mName = null ;
		
		abstract protected String nextName() throws IOException;
		
		@Override
		boolean hasNext() throws IOException {
			mName = nextName();
			
			if( mName == null )
				mFile = null ;
			else
				mFile = getShell().getFile( mName );
			
			return mFile != null;
			
		}
		
		
		public boolean isDirectory() {
			return mFile.isDirectory();
		}
		@Override
		String getUri(String baseUri) {
			// TODO Auto-generated method stub
			return baseUri +mName;
		}

		
		@Override
		Content next(String baseUri)
		{ 
			
			Content content= ContentFactory.newContent (getUri(baseUri) , mFile , mCreateOptions);
			return content ;  
			
		} 
		File getFile() { return mFile ; }
		
	}
	
	
	
	
	private  class ContentListIterator extends ContentFilenameIterator
	{
		private List<String>		mList;
		private Iterator<String>	mIter;

		public ContentListIterator( List<String> list ){
			mList = list ;
			mIter = list.iterator();
		}
		@Override
		protected String nextName()
		{

			if( mIter.hasNext() )
				return mIter.next();
			else
				return null ;
					
		}
		
		

		
		@Override
		boolean canReset() { return true ; }
		
		@Override
		void	reset(){
			mIter = mList.iterator();
		}



	}
	
	
	private  class ContentStreamIterator extends ContentFilenameIterator
	{
		private BufferedReader		mReader;

		public ContentStreamIterator( Reader reader ){
			mReader = new BufferedReader(reader) ;
		}
		
		@Override
		boolean canReset() { return false ; }
		
		@Override
		void	reset() throws UnimplementedException{
			throw new UnimplementedException("reset not implemented");
		}
		
		@Override		
		protected String nextName() throws IOException
		{
			
			return  mReader.readLine();
			
		}
		
		
		
		
	}
	

	public class ContentXdmStreamIterator extends ContentIterator {

		private IXdmItemInputStream mInput ;
		private XdmItem mItem ;
		private String mUriPattern;
		
		
		
		public ContentXdmStreamIterator(IXdmItemInputStream input, String uriPattern ) {
			mInput = input ;
			mUriPattern = uriPattern ;
			
		}

		@Override
		boolean hasNext() throws IOException, CoreException {
			mItem = mInput.read();
			return mItem != null ;
		}

		@Override
		Content next(String baseUri) {
			
			
			Content content;
			try {
				content = ContentFactory.newContent (getUri(baseUri) , MLUtil.bytesFromItem(mItem,getSerializeOpts()) , mCreateOptions);
			} catch (Exception e) {
				mShell.printErr("Exception serializing XML" , e );
				return null ;
			}
			return content ;  
	
			
			
		}

		@Override
		boolean canReset() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		void reset() throws UnimplementedException {
			// TODO Auto-generated method stub

		}

		@Override
		boolean isDirectory() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		File getFile() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		String getUri(String baseURI) {
			return baseURI + parseURI( mUriPattern );
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
				
				for(SumContent sc : mContents)
					if(sc.mDeleteFile  != null )
						sc.mDeleteFile.delete();
				
			} catch (RequestException e) {
				printError("Exception submitting data",e);
			}
			finally {
				mSession.close();
			}
		}
		
	}
	
	
	
	
	private 	List<SumContent> 	mContents = null;
	private		int					mMaxFiles = 1;
	private    boolean		mDelete = false ;
	private int mSequence = 1;
	private Random mRandom;
	

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options("f=filenames:,v=verbose,c=connect:,md5,uri:,baseuri:,m=maxfiles:,r=recurse,d=mkdirs,t=text,b=binary,x=xml,maxthreads:,collection:+,forest:+,perm=permission:+,repair:,buffer:,language:,namespace:,quality:,+resolve,locale:,delete,stream:",SerializeOpts.getOptionDefs());
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
		XValue	 listFileName = opts.getOptValue("filenames");
		mDelete = opts.hasOpt("delete");
		String  stream = opts.getOptString("stream", null);
		setSerializeOpts(opts);

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

		
		
		// Case with single file to put
		if( stream == null && listFileName == null && args.size() == 0 || (args.size() == 1 && baseUri.equals("") ) ){
			InputPort in = null;
			if( args.size() > 0 )
				in = this.getInput(args.get(0));
			else
				in = getStdin();
			
			if( uri == null )
				uri = in.getSystemId();
			this.load(in, uri,bMD5);
		}
		else {
			
			if(! baseUri.equals("") && ! baseUri.endsWith("/") )
				baseUri = baseUri + "/";

			
			// Get list iterator 
			ContentIterator  contentIter = null;
			
			// Streaming option 
			if( stream != null ){
				InputPort port = this.getEnv().getInputPort(stream);
				if( port == null )
					throw new CoreException("Cannot open port: " + stream );
				
				contentIter = new ContentXdmStreamIterator( port.asXdmItemInputStream(getSerializeOpts()) , uri );
				
			}
			else
			if( listFileName != null  )
				
				contentIter = new ContentStreamIterator( (getInput(listFileName)).asReader(serializeOpts)  );
			else {
				List<String> filenames = new LinkedList<String>();
			
				getFiles(filenames , args, "" , bRecurse);
				contentIter = new ContentListIterator(filenames);
			}

			
			if( bMkdirs ){
				if( contentIter.canReset() ){
					createDirs( contentIter ,baseUri  );
					contentIter.reset();
				} else 
					getShell().printErr("Skipping mkdirs on non rewindable conent source");
			}
			
			
			
			/*
			BlockingQueue<Runnable> q = new ArrayBlockingQueue<Runnable>(maxThreads);
			mPool = new ThreadPoolExecutor(maxThreads, maxThreads, Integer.MAX_VALUE, TimeUnit.SECONDS, q);
			*/
			
	
			print("Starting thread pool of " + maxThreads + " threads");
			// mPool = Executors.newFixedThreadPool(maxThreads);

			mPool = new ThreadPoolExecutor(maxThreads, maxThreads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(maxThreads * 2 ), new ThreadPoolExecutor.CallerRunsPolicy() );
			
			
			
			
			
			load( contentIter , baseUri ,  bMD5 );
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






	private void getFiles(List<String> result , List<XValue> files, String baseUri , boolean bRecurse) throws IOException {
		for( XValue v : files ){	
			
			String fname = v.toString();
			File file = getFile(fname);
			String uri = baseUri + file.getName();
			if( file.isDirectory() ){

				result.add(uri + "/");
			
				if( bRecurse ){
					List<XValue> sub = new ArrayList<XValue>();
					for( String fn : file.list() ){
						sub.add(XValue.newXValue(fname + "/" + fn));
					}
					getFiles( result , sub ,  uri + "/"  , bRecurse  );
					
				}
			} else 
				result.add(uri);
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
					sc.add( new SumContent( uri , content , sum ,null));
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

	public void load (ContentIterator contentIter , String baseUri,   boolean bMD5 ) throws CoreException, IOException
	{
		

		while( contentIter.hasNext() ){
			if( contentIter.isDirectory() ){ 
					continue;
			}
			
		    
			Content content = contentIter.next(baseUri);
			Checksum cs = null ;
			if( bMD5 ){
				if( content.isRewindable() ){
					cs = Checksum.calcChecksum( content.openDataStream() );
					content.rewind();
				} else
					getShell().getEnv().printErr("Skipping checksum on non rewindable content: " + content.getUri() );
			}
			
			
			putContent( content.getUri() , content , cs , mDelete ? contentIter.getFile() : null  );
			
		}
		
		
	}


	private void putContent(String uri, Content content, Checksum checksum, File deleteFile)  {
		if( mContents == null )
			mContents = 	new ArrayList<SumContent>( mMaxFiles );

		
		
		mContents.add( new SumContent( uri , content , checksum, deleteFile ));
		if( mContents.size() >= mMaxFiles )
			flushContent();
		
		
		
	}

	
	private void flushContent()  
	{
		if( mContents == null )
			return ;
		
		if( ! mContents.isEmpty()){
			
			print("Submitting contents");
			mPool.execute(new PutContent(mContentSource.newSession() , mContents) );

			
			
			
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
			"xdmp:document-set-property( " + MLUtil.quote(uri) +
			", <xmd5 md5='" + sum.getMD5() + 
			"' length='"+ String.valueOf(sum.getLength()) + "'/>),\n" ;
			
					
		
		
		
	}


	private void createDirs(ContentIterator contentIter , String baseURI ) throws RequestException, IOException, CoreException {

		StringBuffer sReq = new StringBuffer();
		while( contentIter.hasNext() ) {
		     if( contentIter.isDirectory() ){
			
		    	 String uri = contentIter.getUri( baseURI );
			     String qd =  MLUtil.quote(uri);
			
			     sReq.append("if( exists(xdmp:document-properties(" + qd+ ")//prop:directory)) then () else xdmp:directory-create(" +qd + ");\n");
			
		     }
		}
		if( sReq.length() == 0 )
			return ;
		
		
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
		AdhocQuery request = mSession.newAdhocQuery ( 
				"xquery version \"1.0-ml\"; " + 
				sReq.toString() );
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

	
	
	String parseURI( String uri )
	{
		if( !uri.contains("{"))
			return uri ;
		
		return uri.replaceAll("\\{random}", random() ).
		   replaceAll("\\{seq}", sequence() );
		
		
	}


	private synchronized String sequence() {
		return String.valueOf(mSequence ++);
	}


	private synchronized String random() {
		if( mRandom == null )
			mRandom = new Random();
		
		long l = mRandom.nextLong();
		if( l < 0 )
			l = -l;
		return String.valueOf(l);
		
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
