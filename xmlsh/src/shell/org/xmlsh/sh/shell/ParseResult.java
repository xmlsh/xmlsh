/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.CharAttributeBuffer;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParseResult {
	// Attribute'd char buffer
	private CharAttributeBuffer    achars = null;

	private Shell mShell;
	public ParseResult( Shell shell ) {
		mShell = shell ;
	}

	public static class RXValue {
		public XValue         			xvalue;
		private CharAttributeBuffer    avalue;
		public boolean       bRaw;
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

		@Override
		public String toString()
		{
			if( xvalue != null )
				return xvalue.toString();
			else
				if( avalue != null )
					return avalue.toString();
			return null ;
		}
		public XValue toXValue() {
			return xvalue != null ? xvalue : XValue.newXValue(avalue == null ? null : avalue.toString()) ;
		}

		public CharAttributeBuffer toAValue() {
			return avalue != null ? avalue :
				new CharAttributeBuffer( xvalue.toString() , 
						(bRaw ? CharAttr.ATTR_PRESERVE : CharAttr.ATTR_NONE).attr() );
		}
		public boolean isRaw() { 
			return bRaw ;
		}
		public boolean isXValue() { 
			return xvalue != null ;
		}
		public boolean isEmpty() {
			return xvalue == null && avalue == null ;
		}

	}

	ParseResult.RXValue			cur = null;		// Current XValue if its unknown to convert to string, only atomic values
	List<ParseResult.RXValue>	result = null ;

	
	public void delim() {
	    flush(); // TODO insert a null token?
	}
	
	private List<ParseResult.RXValue> getResult(){
	  if( result == null )
        result = new ArrayList<ParseResult.RXValue>( 1 );
	  return result ;
	}
	public void flush() 
	{
		if( cur != null ){
			getResult().add(cur); // dont call ajoin adds twice
			cur = null;
		}
		// resetIfEmpty();
		if( achars != null  ) {
		     getResult().add( new RXValue( achars ) );
			achars = null;
		}

	}

	public void add( CharAttributeBuffer cb ) {
		flush();
		getResult().add( new RXValue( cb ) );
	}
	public void add( String s, CharAttr attr)
	{
		flush();
		getResult().add(new RXValue(s,attr));
	}

	public void add( XValue v )
	{
		flush();
		getResult().add( new RXValue(v,false));
	}
	public void add( XValue v , boolean r )
	{
		flush();
		getResult().add( new RXValue(v,r));
	}

	public void append( String s ,CharAttr attr)
	{
		ajoin();
		if( achars == null )
			achars = new CharAttributeBuffer();
		if( s != null )
			achars.append(s,attr.attr());
	}

	public void append( CharAttributeBuffer cb ) {
	  ajoin();
      if( achars == null )
          achars = new CharAttributeBuffer(cb) ;
      else
        achars.append(cb);
	}
	   

	public void append( char c, CharAttr attr  )
	{
		ajoin();
		if( achars == null )
			achars = new CharAttributeBuffer();
		achars.append(c,attr.attr());
	}


	public List<ParseResult.RXValue> 	resolveToRXValues()
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
		if( attr.isPreserve() ) {
			flush();
			add(value, attr.isPreserve());
		}

		else 
		if( value.isAtomic()   ){
			// If in quotes or this is an ajoining value then concatenate 
			if( attr.isQuote()  || cur != null || achars != null   ){

				// Unquoted empty atomic values are ignored 
				String str = value.toString();

				if( attr.isQuote() && Util.isEmpty(str) )
					return ;
				append(str,attr);

			} else
				cur = new RXValue(value,false) ;
		}
		else {
		  // Inquotes - expand sequences via spearator or not
			if(  attr.isQuote()   ){
 
			  if( ! env.expandSequences() || ! value.isSequence() )
					append( value.toString() , attr );
				else {
					// Flatten sequences
					boolean bFirst = true ;
					for( XValue v : value ) {
						if( ! bFirst )
							append(ShellConstants.ARG_SEPARATOR , attr );
						append(v.toString(),attr);
						bFirst = false ;
					}
				}

			} 
			// Not in quotes but joining values // expand sequences ?
			else
			if( env.joinValues() && env.expandSequences() ) {
				// Only join first value 
				boolean bFirst = true ;
				for( XValue v : value ) {
					if( ! bFirst )
						flush();
					append(v,env.withFlagOff(EvalFlag.EXPAND_SEQUENCES),attr);
					bFirst = false ;
				}
			}
			else {
				flush();
				add(value, attr.isPreserve());
			}
		}
	}

	public void append( ParseResult r ) {
		if( r == null )
			return;
		ajoin();
		r.ajoin(); // ??
		if( r.achars != null  ) 
			append( r.achars );
		if( ! r.result.isEmpty() ) {
			flush();
			result.addAll(r.result);
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

	public static List<XValue> expandWild( RXValue rv, File curdir ) {
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
		EvalUtils.expandDir( root == null ? curdir : new File(root) , 
				parent , 
				wilds , 
				rs );


		for( String f : rs ){
			r.add( XValue.newXValue(f));
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

	public   List<XValue>  expandWild(EvalEnv env, File curdir)
	{
	    List<RXValue> resolved = resolveToRXValues();
	    if( resolved == null )
	      return null ;
	    
		ArrayList<XValue> result2 = new ArrayList<XValue>();


		/*
		 * Globbing
		 */
		for( RXValue rv : resolved ){
			if( ! env.expandWild() )
				result2.add( rv.toXValue() );
			else {

				if( rv.bRaw )
					result2.add( rv.xvalue );
				else {
					List<XValue> r = ParseResult.expandWild(  rv, curdir );
					if( r != null )
						result2.addAll( r );
				}
			}
		}

		return result2;
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