/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.CharAttributeBuffer;
import org.xmlsh.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

class Result {
	// Attribute'd char buffer
	private CharAttributeBuffer    achars = null;

	private Shell mShell;
	public Result( Shell shell ) {
		mShell = shell ;
	}

	static class RXValue {
		XValue         			xvalue;
		private CharAttributeBuffer    avalue;
		boolean       bRaw;
		RXValue( XValue v , boolean r ) {
			xvalue = v ;
			bRaw = r;
		}
		RXValue( CharAttributeBuffer av ) {
			avalue  = av ;
			bRaw = false ;
		}
		RXValue( String s , CharAttr attr ) {
		
			avalue = new CharAttributeBuffer(s, attr.attr());
		}
		
		public String toString()
		{
			if( xvalue != null )
				return xvalue.toString();
			else
			if( avalue != null )
				return avalue.toString();
			return null ;
		}
		XValue toXValue() {
			return xvalue != null ? xvalue : new XValue( avalue == null ? null : avalue.toString() ) ;
		}
		
		CharAttributeBuffer toAValue() {
			return avalue != null ? avalue :
				 new CharAttributeBuffer( xvalue.toString() , 
						 (bRaw ? CharAttr.ATTR_PRESERVE : CharAttr.ATTR_NONE).attr() );
		}
		boolean isRaw() { 
			return bRaw ;
		}
		boolean isXValue() { 
			return xvalue != null ;
		}
		boolean isCharAttr() {
			return avalue != null ;
		}
		boolean isEmpty() {
			return xvalue == null && avalue == null ;
		}

	}
	
	Result.RXValue			cur = null;		// Current XValue if its unknown to convert to string, only atomic values
	List<Result.RXValue>	result = new ArrayList<Result.RXValue>();

	void flush() 
	{
		if( cur != null ){
			result.add(cur); // dont call ajoin adds twice
			cur = null;
		}
		if( achars != null 	 ) {
			result.add( new RXValue( achars ) );
			achars = null;
		}
		
	}
	
	void add( CharAttributeBuffer cb ) {
		flush();
		result.add( new RXValue( cb ) );
	}
	void add( String s, CharAttr attr)
	{
		flush();
		result.add(new RXValue(s,attr));
	}

	void add( XValue v )
	{
		flush();
		result.add( new RXValue(v,false));
	}
	void add( XValue v , boolean r )
	{
		flush();
		result.add( new RXValue(v,r));
	}

	void append( String s ,CharAttr attr)
	{
		ajoin();
		if( achars == null )
			achars = new CharAttributeBuffer();
		if( s != null )
		   achars.append(s,attr.attr());
	}

	void append( char c, CharAttr attr  )
	{
		ajoin();
		if( achars == null )
			achars = new CharAttributeBuffer();
		achars.append(c,attr.attr());
	}


	List<Result.RXValue> 	getResult()
	{
		flush();
		return result ;
	}

	/*
	 * Append a value to the result buffer
	 * If currently in-quotes then convert the args to strings and seperated by the first char in IFS.
	 */
	public void append(XValue value, EvalEnv env , CharAttr attr  ) {
	
		if( (value == null || value.isNull() ) && env.omitNulls() )
			return ;
		
		if( value.isAtomic()  && !attr.isPreserve() ){
			// If in quotes or this is an ajoining value then concatenate 
			if( attr.isQuote()  || cur != null || (achars != null && ! achars.isEmpty())  ){

				// Unquoted empty atomic values are ignored 
				String str = value.toString();

				if( attr.isQuote() && Util.isEmpty(str) )
					return ;
				append(str,attr);

			} else
				cur = new RXValue(value,false) ;
		}
		else {
			if(  attr.isQuote()   ){

				if( value.isObject() )
					append( value.toString() , attr );
				else {
					// Flatten sequences
					boolean bFirst = true ;
					for( XdmValue v : value.asXdmValue() ){
						if( ! bFirst )
							append(Expander.sSEPSPACE , attr );
						append(v.toString(),attr);
						bFirst = false ;
					}
				}

			} else {
				flush();
				add(value, attr.isPreserve());
			}
		}
	}

	private void ajoin() {
		if( cur != null ){
			if( achars == null )
				achars = new CharAttributeBuffer();
			achars.append(cur.toString(), CharAttr.ATTR_NONE.attr() );  // TODO ? 
			cur = null;
		}
	}
	
	public void resetIfEmpty()
    {
        if( achars != null && achars.isEmpty() )
        	achars = null;
    }

	public List<XValue> expandWild( RXValue rv ) {
		ArrayList<XValue> r = new ArrayList<XValue>();
	
		if( rv.isEmpty())
			return r;
		
		if( rv.isRaw() ){
			r.add( rv.toXValue());
			return r;
			
		}
			
		if( rv.isXValue()) {
			XValue xv = null;
			xv = rv.toXValue();
			if( ! xv.isAtomic() ){
				r.add( xv);
				return r;
			}
		}
		
		
		CharAttributeBuffer av = rv.toAValue();
		int vslen = av.size();
	
		boolean wildUnQuoted = false ;
		for( int i = 0 ; i < vslen ; i++ ){
			char c = av.charAt(i);
			// To expand wild there must be NO quoting attributes at all
			if(  Util.isWildChar(c) && ( av.attrAt(i) == 0 ) ) {
				wildUnQuoted = true ;
				break ;
			}
		}
	
		if( ! wildUnQuoted ){
		
			r.add( rv.toXValue() );
			return r;
		}
	
	
		List<String>	rs = new ArrayList<String>();
	
		/*
		 * If vs starts with / (or on dos x:) then use that directory as the root
		 * instead of the current directory
		 */
	
		String root = null ;
		String parent = null;
		
		
		if( av.charAt(0) == '/' ){
			root = "/";
			parent = "";
			av.delete( 0, 1 );
		}
	
		if( Util.isWindows() && av.size() >= 2 ) {
	
			char drive = av.charAt(0);
			// Character.isAlphabetic() is V7 only
			if( Character.isLetter(drive) && av.charAt(1) == ':'){
				
				// If windows and matches  <dir>:blah blah
				// make the root <dir>:/
				// If no "/" is used then the current directory of that dir is used which is not shell semantics
	
				root = "" + drive + ":/";
				if( av.size() > 2 && av.charAt(2) == '/' )
					av.delete(0,3);
				else
					av.delete(0,2);
				parent = root;
			
			}
		}
	
		CharAttributeBuffer	wilds[] = av.split('/');
		expandDir( root == null ? mShell.getCurdir() : new File(root) , 
				parent , 
				wilds , 
				rs );
	
	
		for( String f : rs ){
			r.add( new XValue(f));
		}
	
		// If no matches then use arg explicitly
		if( r.size() == 0)
			r.add( rv.toXValue());
	
		return r;
	
	}
	/*
	 * Expand a single level wildcard rooted at a directory
	 * Return list of all matches or null if no matches
	 */

	public  List<XValue> expandWild(EvalEnv env)
	{
	    ArrayList<XValue> result2 = new ArrayList<XValue>();
	
	
		/*
		 * Globbing
		 */
		for( RXValue rv : getResult() ){
			if( ! env.expandWild() )
				result2.add( rv.toXValue() );
			else {
	
				if( rv.bRaw )
					result2.add( rv.xvalue );
				else {
					List<XValue> r = expandWild( rv );
					if( r != null )
						result2.addAll( r );
				}
			}
		}
	
		return result2;
	}

	static void expandDir( File dir , String parent , CharAttributeBuffer wilds[] , List<String> results )
	{
		CharAttributeBuffer wild = wilds[0];
		if( wilds.length < 2 )
			wilds = null ;
		else 
			wilds = Arrays.copyOfRange(wilds, 1, wilds.length );
	
	
		List<String> rs = expandDir( dir , wild, wilds != null   );
		if( rs == null)
			return ;
		for( String r : rs ){
			String path =  parent == null ? r : parent + (parent.endsWith("/") ? "" : "/") + r;
	
			if( wilds == null )
				results.add( path );
			else 
				expandDir( 
						new File( dir , r ) ,
						path , 
						wilds ,
						results );
	
		}
	
	}



	/*
	 * Recursively Expand a possibly multi-level wildcard rooted at a directory
	 * 
	 */

	
	
	static List<String>	expandDir( File dir , CharAttributeBuffer wild, boolean bDirOnly )
	{
		ArrayList<String> results = new ArrayList<String>();
		/*
		 * special case for "." and ".." as the directory component
		 * They dont show up in dir.list() so should always be considered an exact match
		 */
		if( wild.stringEquals(".") || wild.stringEquals(".."))
		{
			results.add(wild.toString());
			return results;
		}
	
	
		boolean bIsWindows = Util.isWindows();
		boolean caseSensitive = ! bIsWindows;
	
		/*
		 * Hack to handle 8.3 windows file names like "Local~1"
		 * If not matched and this is windows
		 * try an exact match to the canonical expanson of the dir and wild
		 */
		if(  bIsWindows && wild.indexOf(0,'~', CharAttr.ATTR_NONE.attr()) >= 0 ){
			String wildString = wild.toString();
			File fwild = new File( dir , wildString );
			if( fwild.exists() ){
				results.add(wildString);
				return results ;
			}
		}
	
	
	
		String[] files = dir.list();
		
		Pattern wp = Util.compileWild(wild, CharAttr.ATTR_ESCAPED.attr() , caseSensitive);
		
		for( String f : files ){
	
			boolean bMatched = wp.matcher(f).matches();
	
			if( bMatched &&
					( bDirOnly ? ( new File( dir , f ).isDirectory() ) : true ) ) 
			{
				results.add(f);
			}
		}
		if( results.size() == 0 )
			return null;
		Collections.sort(results);
		return results;
	
	
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