/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import org.xmlsh.util.Util;

public class S3Path {
	
	private		String 	mBucket;
	private		String	mKey;
	
	
	/*
	 * Construct a path from
	 * 		bucket
	 * 		bucket/prefix
	 * 		s3://bucket
	 * 		s3://bucket/prefix
	 * 		
	 */
	
	public S3Path(String	path)
	{
		if( path.startsWith("s3://"))
			path = path.substring(5);
		
		int spos = path.indexOf('/');
		if( spos < 0 )
			mBucket = Util.nullIfBlank(path) ;
		else {
			mBucket = Util.nullIfBlank( path.substring(0,spos));
			mKey = Util.nullIfBlank(path.substring(spos+1));
		}
		
	}
	
	
	public S3Path( String bucket , String key )
	{
		mBucket = Util.nullIfBlank(bucket) ;
		mKey = Util.nullIfBlank(key) ;
		
		
	}
	
	public S3Path( S3Path parent , String child )
	{
		mBucket = parent.mBucket ; 
		if( parent.mKey.endsWith("/") || child.startsWith("/"))
			mKey = parent.mKey + child ;
		else
			mKey = parent.mKey + "/" + child ;
		
	}

	public 	String	getBucket(){
		return mBucket ;
	}
	
	public String	getPrefix(){
		return mKey ;
	}
	
	public boolean 	hasBucket()
	{
		return mBucket != null ;
	}

	public boolean 	hasKey()
	{
		return mKey != null ;
	}
	
	
	// Synonym to getPrefix 
	public String getKey() {
		return mKey ;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "s3://" + Util.notNull(mBucket) + ( hasKey() ? ("/" + mKey) : "" );
		
		
	}
	
	public boolean isDirectory()
	{
		return mKey == null || mKey.endsWith("/");
	}
	
	public void appendPath( String path )
	{
		if( mKey == null )
			mKey = path ;
		else
		if( mKey.endsWith("/"))
			mKey = mKey + path ;
		else
			mKey = mKey + "/" + path ;
		
	}
	
	
}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
