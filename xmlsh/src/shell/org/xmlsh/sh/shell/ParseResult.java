/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.xmlsh.sh.shell.CharAttr.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.CharAttributeBuffer;
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.util.Util;

public class ParseResult {
	// Attribute'd char buffer
	private CharAttributeBuffer    achars = null;
 
	static Logger mLogger = LogManager.getLogger();

	public static class RXValue {
		public XValue         			xvalue;
		private CharAttributeBuffer    avalue;
		public CharAttrs  xvalAttrs ;
		RXValue( XValue v , boolean r ) {
			xvalue = v ;
			xvalAttrs = r ? CharAttrs.constInstance(ATTR_PRESERVE) : CharAttrs.constInstance();
		}
		RXValue( CharAttributeBuffer av ) {
			avalue  = av ;
			xvalAttrs = CharAttrs.constInstance();
		}
		RXValue( String s , CharAttrs attr ) {
			avalue = new CharAttributeBuffer(s, attr);
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
			return avalue != null ? avalue.clone()  :
				new CharAttributeBuffer( xvalue.toString() , xvalAttrs ); 
		}
		public boolean isRaw() { 
			return xvalAttrs.isPreserve() ;
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
	public void add( String s, CharAttrs attr)
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

	public void append( String s ,CharAttrs attr)
	{
		ajoin();
		if( achars == null )
			achars = new CharAttributeBuffer();
		if( s != null )
			achars.append(s,attr);
	}

	public void append( CharAttributeBuffer cb ) {
	  ajoin();
      if( achars == null )
          achars = new CharAttributeBuffer(cb) ;
      else
        achars.append(cb);
	}
	   

	public void append( char c, CharAttrs attr  )
	{
		ajoin();
		if( achars == null )
			achars = new CharAttributeBuffer();
		achars.append(c,attr);
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
	public void append(XValue value, EvalEnv env , CharAttrs attr  ) {

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
				achars = new CharAttributeBuffer(cur.toAValue());
			else 
			    achars.append(cur.toAValue());  
			cur = null;
		}
	}

	public void resetIfEmpty()
	{
		if( achars != null && achars.isEmpty() )
			achars = null;
	}

	public static List<XValue> expandWild( RXValue rv, File curdir ) {
	
		assert( rv != null );
	    assert( ! rv.isEmpty() );
	    assert( ! rv.isRaw() );
	    

		CharAttributeBuffer av = rv.toAValue();
	
		if( ! Util.containsWild(av))
			return Collections.singletonList(rv.toXValue() );
			

		ArrayList<XValue> r = new ArrayList<XValue>();	
		
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

		
		/*  Moove to FilesSystem.getRoot */
		
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

		/*******/
		if( ! av.isEmpty() ){
			
			CharAttributeBuffer	wilds[] = av.split('/');
			if( wilds.length > 0 ){

				List<String>	rs = new ArrayList<String>();

				try {
					EvalUtils.expandDir( root == null ? curdir : new File(root) , 
							parent , 
							wilds , 
							rs );
					for( String f : rs ){
						r.add( XValue.newXValue(f));
					}
				} catch (IOException e) {
					mLogger.catching(e);
				}
		    }

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
			if( ! env.expandWild()  ||  rv.isRaw() )
				result2.add( rv.toXValue() ); 
			else {

				if( rv.isXValue() && ! rv.toXValue().isAtomic() )
					result2.add( rv.toXValue() );
				else {
					List<XValue> r = ParseResult.expandWild(  rv, curdir );
					if( r != null )
						result2.addAll( r );
				}
			}
		}

		return result2;
	}
	
	
	// For logging
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("ParseResult");
		if( cur != null )
			sb.append( "cur=["  +cur.toString() + "]");

		if(achars != null ){
			sb.append("achars=[");
			achars.logString(sb);
			sb.append("]");
		}
		if( result != null ){
			sb.append("result=[");
			Util.join(sb, result , ",").append("]");
		}
		return sb.toString();
		
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