/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import org.xmlsh.util.Util;

public class S3Path {

	private		String 	mBucket = null;
	private		String	mKey = null;
  private String mDelim = kDEF_DELIM ;
  public static final String kDEF_DELIM = "/";


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
     initFromPath( path );
	}
	  
	private void initFromPath( String path ){
	  if( Util.isBlank(path))
       return ;
  		if( path.startsWith("s3://"))
  			path = path.substring(5);
  
  		int spos = path.indexOf(mDelim);
  		if( spos < 0 )
  			mBucket = Util.nullIfBlank(path) ;
  		else {
  			mBucket = Util.nullIfBlank( path.substring(0,spos));
  			mKey = Util.nullIfBlank(path.substring(spos+mDelim.length()));
  		}
	}


	public S3Path( String bucket , String key )
	{
	  if( Util.isBlank(bucket) || 
	       ( !Util.isBlank(key ) && key.startsWith("s3:/") ))
	    initFromPath( key );
	  else {
		  mBucket = Util.nullIfBlank(bucket) ;
		  mKey = Util.nullIfBlank(key) ;
	  }
	}

	public S3Path( S3Path parent , String child )
	{
		mBucket = parent.mBucket ; 
		if (child.startsWith(mDelim)) child = child.substring(1);
		mKey = child;
		if(parent.hasKey())
			mKey = (parent.mKey.endsWith(mDelim) ? parent.mKey : parent.mKey + mDelim) + mKey ;		
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

		return "s3://" + Util.notNull(mBucket) + ( hasKey() ? (mDelim + mKey) : "" );


	}

	public boolean isDirectory()
	{
    return mKey == null || mKey.endsWith(mDelim);
	}

	public void appendPath( String path )
	{
		if( mKey == null )
			mKey = path ;
		else
			if( mKey.endsWith(mDelim))
				mKey = mKey + path ;
			else
				mKey = mKey + mDelim + path ;

	}


	/**
	 * @param bucket the bucket to set
	 */
	public void setBucket(String bucket) {
		mBucket = Util.nullIfBlank(bucket) ;


	}


	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		mKey = Util.nullIfBlank(key) ;

	}

  public static boolean isDirectory(String key, String delim ) {
    return key != null && delim != null && key.endsWith(delim);
  }


  public static boolean isDirectory(String key) {
    return key != null && key.endsWith(kDEF_DELIM);
  }
}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
