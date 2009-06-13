/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmValue;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;

class Expander {
	private static final String sSEPSPACE = " ";
	private static Logger mLogger = LogManager.getLogger( Expander.class);
	
	private 	Shell		mShell;
	private		List<XValue> 	mArgs;
	

	
	
	private static class Result {

		StringBuffer	sb = new StringBuffer();
		List<XValue>	result = new ArrayList<XValue>();
		
		void flush() 
		{
			if( sb.length() > 0 )
				add(sb.toString());
			sb.setLength(0);
			
		}
		
		void add( String s )
		{
			result.add(new XValue(s));
		}
		
		void add( XValue v )
		{
			result.add(v);
		}
		void append( String s )
		{
			sb.append(s);
		}
		
		void append( char c )
		{
			sb.append(c);
		}
		
		void add( List<XValue> args )
		{
			flush();
			result.addAll( args );
		}
		
		List<XValue> 	getResult()
		{
			flush();
			return result ;
		}

		public void append(String[] args) {
			// @TODO : BROKEN !   $*foo doesnt concat the last arg
			for( String a : args ){
				if( sb.length() > 0 ){
					sb.append(a);
					add(sb.toString());
					sb = new StringBuffer();
				} else
					add(a);
			}
			
		}

		/*
		 * Append a value to the result buffer
		 * If currently in-quotes then convert the args to strings and space seperate
		 */
		public void append(XValue value, boolean inQuotes ) {
			if( value.isString() || value.isAtomic() )
				append( value.toString());
			else {
				if( inQuotes ){
					// Flatten sequences
					boolean bFirst = true ;
					for( XdmValue v : value.asXdmValue() ){
						if( ! bFirst )
							append(sSEPSPACE);
						append( v.toString());
						bFirst = false ;
					}
					
				} else {
				
					flush();
					add(value);
				}
			}
		}
		
	};
	
	
	
	Expander( Shell shell )
	{
		mShell = shell;
		mArgs = shell.getArgs();
		
		
	}
	
	private int readToMatching( String arg , int i , StringBuffer sbv , char match )
	{
		char start = arg.charAt(i++);
		int matchCount = 1;
		
		// Eat up to '}'
		for( ; i < arg.length() ; i++ ){
			char c = arg.charAt(i);
			if( c == match ){
				if( --matchCount == 0 )
					break ;
			} else
			if( c == start )
				matchCount++;

			sbv.append(c);

		}
		return i;
	}
	

	List<XValue> expand(String arg, boolean bExpandWild , boolean bExpandWords ) throws IOException, CoreException
	{
		

		// <{ big quotes }>
		if( arg.startsWith("<{{") && arg.endsWith("}}>")){
			List<XValue>	r = new ArrayList<XValue>(1);
			r.add( new XValue(arg.substring(3,arg.length()-3)) );
			return r;		
		}
		
		
		// <[ XEXPR ]>
		if( arg.startsWith("<[") && arg.endsWith("]>")){
			List<XValue> 	r = new ArrayList<XValue>(1);
			r.add( new XValue( parseXExpr(arg.substring(2,arg.length()-2))));
			return r;
		}
		
				

		Result	result = new Result();

		char cQuote = 0;
		char c;
		int i;
		for( i = 0 ; i < arg.length() ; i++){

			c = arg.charAt(i);
			
			// Just detect quotes in this phase, dont strip them
			// we need them to determine when to do wildcard expansion
			if( c == '\'' || c == '"'){
				if( cQuote == c ) // end quote
					cQuote = 0;
				else 
				if(cQuote == 0 )
					cQuote = c;
			}
			
			
			// Escape
			//  foo\bar		-> 	foobar
			//  "foo\bar" 	-> "foo\bar"
			//  "foo\\bar" 	-> "foo\bar"
			//	'foo\\bar'  -> 'foo\\bar'
			
			
			if( c == '\\'){
				if( i < arg.length()){
					char nextc = arg.charAt(++i);
					if( cQuote == 0 ){ // strip backslash, ignore next 
						if( nextc == '"' || nextc == '\'' )
							result.append(c);
						result.append(nextc);
					}
					
					
					if( cQuote == '"' ) {// preserve 1, strip 2 
						result.append(c);
						if( nextc != '\\' )
							result.append(nextc);
					}
					
					if( cQuote == '\'') {
						result.append(c);
						result.append(nextc);
					}
				
				}
				continue;
			}
			if( cQuote != '\'' && c == '$'){
				if( ++i == arg.length() )
					break;
				
				StringBuffer sbv = new StringBuffer();
				if( arg.charAt(i) == '{') {
					i = readToMatching( arg , i , sbv ,  '}' );
				} 
				
				
				else { 
					// Speical case 
					// $?  $*  $@ $$ $0...$9
					c = arg.charAt(i);
					if( c == '?' ||  c == '@' || c == '$' || c == '#' || c == '*' || c == '!' || Character.isDigit(c)){
						boolean bKeepGoing ;
						do {
							bKeepGoing = false ;
							sbv.append(c);
							
							// Special case for $<dig><dig>...
							// NOTE: Differs from sh/bsh/ksh - $11 is [arg 11] not [arg 1]1
							// 
							if( Character.isDigit(c)){
								if( i < arg.length()-1 && Character.isDigit(c=arg.charAt(i+1))){
									i++;
									bKeepGoing = true ;
								}
							}
						} while( bKeepGoing );
					}
					else
					{
						// Eat up all a-zA-Z_
						for(  ; i < arg.length(); i++){
							c = arg.charAt(i);
							if( Util.isIdentifier(c) )
								sbv.append(c);
							else {
								i--; // back up 
								break;
							}
						}
					}
				}
				
				String var = sbv.toString();
				if( !Util.isBlank(var) ){
					XValue value = null;

					if( var.equals("*")){
						// Add all positional variables as args 
						boolean bFirst = true ;
						for( XValue v : mArgs ){
							// result.add( quote(v) );
							if( cQuote != 0  ){
								if( ! bFirst )
									result.append(sSEPSPACE  );
								result.append(quote(v),true);
							}
							else
							{
								result.flush();
								result.add(quote(v));
							}
							bFirst = false ;
						}
						
					} 
					if( var.equals("@")){
						// Special case if sb has a single " nuke it
						if( result.sb.length() == 1 && result.sb.charAt(0) == '"' )
							result.sb.setLength(0);
						else
							result.flush();
						for( XValue v : mArgs )
							result.add( quote(v) );
					}

					
					
					else
						value = extractSingle(var, cQuote != '\0' );
					
					
					if( value != null )
						result.append( value , cQuote != '\0' );
				} else
					result.append('$');
			
			} else {
				
				// If adding a closing quote and there is nothing in the buffer
				// then skip adding it.  This lets  "$@" work without adding an empty arg
				
				if( c == '"' && cQuote == 0 && result.sb.length() == 0 )
					;
				else
					result.append(c);
			
			
			}
			
		}
		
		result.flush();

		ArrayList<XValue> result2 = new ArrayList<XValue>();
		
		
		for( XValue v : result.getResult() ){
			/*
			 * If v is an atomic string value then dequote and expand
			 * DO NOT dequote variable expansions
			 */
			if( ! bExpandWild )
				result2.add( removeQuotes(v));
			else {
			
				List<XValue> r = expandWild( v );
				if( r != null )
					result2.addAll( r );
			}
		}
			
		
		return result2;
		
		
	}

	
		
	private XdmValue parseXExpr(String arg) {
		
		
	
		
		Processor processor = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		
		NameValueMap<String> ns = mShell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}
			

		
		XQueryExecutable expr = null;

		StringBuffer sb = new StringBuffer();
		

		Collection<XVariable> vars = mShell.getEnv().getVars().values();
		for( XVariable value : vars ){
			
			if( ! value.isNull() && value.getFlags().contains( XVarFlag.XEXPR ))
			
				sb.append("declare variable $").append(value.getName())
				.append(" external ;\n");
			
		}
		sb.append("declare variable $_ external;\n");
		
		
		sb.append(arg);
		try {
			expr = compiler.compile( sb.toString() );
			
			XQueryEvaluator eval = expr.load();
			
			for( XVariable value : vars ){
				
				if( !value.isNull() && value.getFlags().contains( XVarFlag.XEXPR ))
			
					eval.setExternalVariable( new QName(value.getName()), value.getValue().asXdmValue());
				
			}
			
			eval.setExternalVariable( new QName("_") , new XValue(mShell.getArgs()).asXdmValue() );
			
			
			return eval.evaluate();
			
		} catch (SaxonApiException e) {
			mLogger.warn("Error expanding xml expression: " + arg , e );
			mShell.printErr("Error expanding xml expression");
		}

		return null;
		
	
	}




	private XValue removeQuotes(XValue v) {
		if( v.isXExpr() )
			return v;

		String vs = v.toString();
		int vslen = vs.length();
		StringBuffer sb = new StringBuffer();
		
		char cQuote = 0;
		for( int i = 0 ; i < vslen ; i++ ){
			char c = vs.charAt(i);
			if( c == '\\' && i < vslen ){
				sb.append(c);
				c = vs.charAt(++i);
				sb.append(c);
				continue;
				
			}
			if( c == '"' || c == '\''){
				if( c == cQuote ){
					cQuote = 0;
					continue ;
				}
				else
				if( cQuote == 0 ){
					cQuote = c ;
					continue ;
				}
			}
			
			sb.append(c);
		}
		
		return new XValue(sb.toString());
		
	}


	private List<XValue> expandWild(XValue v) {
		ArrayList<XValue> r = new ArrayList<XValue>();
		
		if( v.isXExpr() ){
			r.add( v);
			return r;
		}
		
		String vs = v.toString();
		int vslen = vs.length();
		StringBuffer sb = new StringBuffer();
		
		char cQuote = 0;
		boolean wildUnQuoted = false ;
		for( int i = 0 ; i < vslen ; i++ ){
			char c = vs.charAt(i);
			if( c == '\\' && i < vslen ){
				
				// sb.append(c);
				c = vs.charAt(++i);
				if( c != '"' && c != '\'')
					sb.append('\\');
				sb.append(c);
				continue;
				
			}
			if( c == '"' || c == '\''){
				if( c == cQuote ){
					cQuote = 0;
					continue ;
				}
				else
				if( cQuote == 0 ){
					cQuote = c ;
					continue ;
				}
			}
			
			if( cQuote == 0 && ( c == '*' || c == '?' || c == '['))
				wildUnQuoted = true ;
			sb.append(c);
			
			
		}
		
		if( ! wildUnQuoted ){
			r.add( new XValue(sb.toString()));
			return r;
		}
		
		
		vs = sb.toString();
		
		
		
		List<String>	rs = new ArrayList<String>();
		
		// Convert // to /
		vs = vs.replaceAll("//", "/");
		
		
		/*
		 * If vs starts with / (or on dos x:) then use that directory as the root
		 * instead of the current directory
		 */
		
		String root = null ;
		String parent = null;
		if( vs.startsWith("/") ){
			root = "/";
			parent = "";
			vs = vs.substring(1);
		}
		
		if( Util.isWindows() && vs.matches("^[a-zA-Z]:.*") ){
			// If windows and matches  <dir>:blah blah
			// make the root <dir>:/
			// If no "/" is used then the current directory of that dir is used which is not shell semantics
			
			root = vs.substring(0,2) +"/";
			vs = vs.substring(2);
			if( vs.startsWith("/") )
				vs = vs.substring(1);
			parent = root;
		}
		
		
		String	wilds[] = vs.split("/");
		expandDir( root == null ? mShell.getCurdir() : new File(root) , 
				parent , 
				wilds , 
				rs );
		
		
		for( String f : rs ){
				r.add( new XValue(f));
		}
		
		// If no matches then use arg explicitly
		if( r.size() == 0)
			r.add(v);
		
		return r;
		
	}
	/*
	 * Expand a single level wildcard rooted at a directory
	 * Return list of all matches or null if no matches
	 */
	
	private List<String>	expandDir( File dir , String wild, boolean bDirOnly )
	{
		ArrayList<String> results = new ArrayList<String>();
		/*
		 * special case for "." and ".." as the directory component
		 * They dont show up in dir.list() so should always be considered an exact match
		 */
		if( wild.equals(".") || wild.equals(".."))
		{
			results.add(wild);
			return results;
		}
		
		
		boolean caseSensitive = ! Util.isWindows();
		String[] files = dir.list();
		for( String f : files ){
			if( Util.wildMatches( wild , f, caseSensitive ) &&
				( bDirOnly ? ( new File( dir , f ).isDirectory() ) : true ) ) 
				results.add(f);
		}
		if( results.size() == 0 )
			return null;
		Collections.sort(results);
		return results;
	
		
	}
	
	
	
	/*
	 * Recursively Expand a possibly multi-level wildcard rooted at a directory
	 * 
	 */

	private void expandDir( File dir , String parent , String wilds[] , List<String> results )
	{
		String wild = wilds[0];
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
	private XValue extractSingle(String var, boolean quoted) throws IOException, CoreException {
	
		XValue v = extractSingle(var);
		if( v == null || v.isXExpr() )
			return v;
		

			String s = v.toString();
			
			if( hasQuotes(s) )
				return new XValue(quote(s));
			else
				return v;

				
		
	
	}

	private String quote(String s) 
	{
		
		StringBuffer sb = new StringBuffer( s.length() );
		for( int i = 0 ; i < s.length() ; i++ ){
			char c = s.charAt(i);
			if( c == '"' || c == '\'' || c == '\\')
				sb.append('\\');
			sb.append(c);
		}
		return sb.toString();
		
	}

	public XValue quote( XValue v)
	{
		if( v == null || v.isXExpr() )
			return v;
		return new XValue(quote(v.toString()));
	}
	
	
	private boolean hasQuotes(String s) {
		return s.indexOf('\'')  >= 0 ||
		 		s.indexOf('\"') >= 0 ;
				
	}

	private XValue extractSingle(String var) throws IOException, CoreException {
		
		
		
		if( var.equals("#"))
			return new XValue( mArgs.size() );
		else
		// Special vars
		if( var.equals("$"))
			return new XValue(Thread.currentThread().getId());
		else
		if( var.equals("?"))
			return new XValue( mShell.getStatus());
		else
		if( var.equals("!")){
			
				return new XValue( mShell.getLastThreadId() );
		}
		
			
		else
		if( Util.isInt(var,false)){
			int n = Util.parseInt(var, -1);
			if( n == 0 )
				return new XValue(mShell.getArg0());
			else
			if( n > 0 && n <= mArgs.size() )
				return mArgs.get(n-1);
			else
				return null;	// unfound args, do not get used, 
		}
		else {
			// ${#var} notation
			if( var.startsWith("#") ) {
				var = var.substring(1);
				XValue val = mShell.getEnv().getVarValue( var );
				int sz = val.asXdmValue().size();
				return new XValue( String.valueOf(sz));
				
				
			}
			
			
			// Look for array notation 
			// ${var[3]}
			if( var.contains("[")){
				int as = var.indexOf('[');
				String ind = var.substring(as+1 , var.indexOf(']'));
				var = var.substring(0,as);
				
				XValue val = mShell.getEnv().getVarValue( var );
				if( ind.equals("*")) // special case ${var[*]}
					return val ;
				
				XdmValue xval = val.asXdmValue().itemAt( Util.parseInt(ind, 0) - 1 );
				val = new XValue(xval);
				return val;
			}
			
			return mShell.getEnv().getVarValue( var );
		}
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

