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
import net.sf.saxon.trans.XPathException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

class Expander {
	private static final String sSEPSPACE = " ";
	private static Logger mLogger = LogManager.getLogger( Expander.class);
	
	private 	Shell			mShell;
	private		SourceLocation 	mLocation ;
	

	
	
	private static class Result {

		static class RXValue {
			public XValue         xvalue;
			public boolean       bRaw;
			RXValue( XValue v , boolean r ) {
				xvalue = v ;
				bRaw = r;
			}
			RXValue( String s ) {
				xvalue = new XValue(s);
				bRaw = false ;
			}
			public String toString()
			{
				return xvalue.toString();
			}
			
		}
		StringBuffer	sb = new StringBuffer();
		RXValue			cur = null;		// Current XValue if its unknown to convert to string, only atomic values
		List<RXValue>	result = new ArrayList<RXValue>();
		
		void flush() 
		{
			if( cur != null ){
				result.add(cur); // dont call ajoin adds twice
				cur = null;
			}
				
			
			if( sb.length() > 0 )
				add(sb.toString());
			sb.setLength(0);
			
		}
		
		void add( String s )
		{
			ajoin();
			result.add(new RXValue(s));
		}
		
		void add( XValue v )
		{
			ajoin();
			result.add( new RXValue(v,false));
		}
		void add( XValue v , boolean r )
		{
			ajoin();
			result.add( new RXValue(v,r));
		}
		
		void append( String s )
		{
			ajoin();
			sb.append(s);
		}
		
		void append( char c )
		{
			ajoin();
			sb.append(c);
		}
		

		
		List<RXValue> 	getResult()
		{
			flush();
			return result ;
		}

	
		/*
		 * Append a value to the result buffer
		 * If currently in-quotes then convert the args to strings and space seperate
		 */
		public void append(XValue value, boolean inQuotes, boolean bTongs ) {
			if( value.isAtomic()  && ! bTongs ){
				
				
				
				// If in quotes or this is an ajoining value then concatenate 
				if( inQuotes || cur != null || sb.length() > 0 ){

					// Unquoted empty atomic values are ignored 
					String str = value.toString();

					if(!inQuotes && Util.isEmpty(str) )
						return ;

					
					ajoin();
					sb.append( str );
					
				} else
					cur = new RXValue(value,false) ;
			}
			else {
				if( inQuotes ){
					
					if( value.isObject() )
						append(escapeQuotes(value.toString()));
					else {
						// Flatten sequences
						boolean bFirst = true ;
						for( XdmValue v : value.asXdmValue() ){
							if( ! bFirst )
								append(sSEPSPACE);
							
							// DAL: Need to escape quotes if converting XML to string inside quotes
							String sv = v.toString();
							
							append(escapeQuotes(sv));
							bFirst = false ;
						}
					}
					
				} else {
				
					flush();
					add(value, bTongs);
				}
			}
		}

		/*
		 * Escape any double quotes with \
		 */
		private String escapeQuotes(String sv) {
			StringBuffer sb = new StringBuffer();
			int len = sv.length();
			for( int i = 0 ; i < len ; i++ ){
				char c = sv.charAt(i) ;
				if( c == '"')
					sb.append('\\');
				sb.append(c);
				
				
			}
			return sb.toString();
		}

		private void ajoin() {
			if( cur != null ){
				sb.append(cur.toString());
				cur = null;
			}
		}
		
	};
	
	
	
	Expander( Shell shell, SourceLocation loc )
	{
		mShell = shell;
		mLocation = loc ;
		
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
	

	List<XValue> expand(String arg, boolean bExpandWild , boolean bExpandWords, boolean bTongs ) throws IOException, CoreException
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
				if( ++i == arg.length() ){
					result.append('$'); // Special case of a single "$"
					break;
				}
				
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
						for( XValue v : mShell.getArgs() ){
							// result.add( quote(v) );
							if( cQuote != 0  ){
								if( ! bFirst )
									result.append(sSEPSPACE  );
								result.append(quote(v),true, bTongs );
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
						for( XValue v : mShell.getArgs() )
							result.add(  v , true );
					}

					
					
					else
						value = extractSingle(var, cQuote != '\0' , bTongs );
					
					
					if( value != null )
						result.append( value , cQuote != '\0', bTongs );
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
		
		
		for( Result.RXValue v : result.getResult() ){
			/*
			 * If v is an atomic string value then dequote and expand
			 * DO NOT dequote variable expansions
			 */
			if( ! bExpandWild )
				result2.add( v.bRaw ? v.xvalue : removeQuotes(v.xvalue) );
			else {
		
		        if( v.bRaw )
		        	result2.add( v.xvalue );
		        else {
					
					
					List<XValue> r = expandWild( v.xvalue );
					if( r != null )
						result2.addAll( r );
		        }
			}
		}
			
		
		return result2;
		
		
	}

	
		
	private XdmValue parseXExpr(String arg) throws CoreException {
		
		
		
		Processor processor = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		compiler.setModuleURIResolver(new ShellModuleURIResolver(mShell));

		
		
		
		// Declare the extension function namespace
		// This can be overridden by user declarations
		compiler.declareNamespace("xmlsh", EvalDefinition.kXMLSH_EXT_NAMESPACE);
		
		NameValueMap<String> ns = mShell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}

		

		
		XQueryExecutable expr = null;

		StringBuffer sb = new StringBuffer();
		

		Variables variables = mShell.getEnv().getVars();
		Collection<String> varnames = variables.getVarNames();
		for( String name : varnames ) {
			XVariable value = variables.get(name);
			if( ! value.isNull() && value.getFlags().contains( XVarFlag.XEXPR ))
			
				sb.append("declare variable $").append(value.getName())
				.append(" external ;\n");
			
		}
		
		List<XValue> args = mShell.getArgs();
		
		/*
		// Legacy support a single sequence $_ with all args
		sb.append("declare variable $_ external;\n");
		*/
		
		
		// 2010-02-04
		// Express each positional parameter as $_1 $_2 ...
		for( int i = 1 ; i <= args.size() ; i++ )
			sb.append("declare variable $_" + i + " external;\n");

		
		sb.append(arg);
		
		Shell saved_shell = ShellContext.set(mShell);
			
		try {
			expr = compiler.compile( sb.toString() );
			
			XQueryEvaluator eval = expr.load();
			
			for( String name : varnames ) {
				XVariable value = variables.get(name);
				if( !value.isNull() && value.getFlags().contains( XVarFlag.XEXPR )){
					XValue v = value.getValue();
					if( v.isObject() )
						try {
							v = new XValue( v.convert(XdmValue.class));
						} catch (XPathException e) {
							throw new CoreException(e);
						}
					
					eval.setExternalVariable( new QName(value.getName()), v.asXdmValue());
				}
			}

			
			/*
			 * Legacy support for $_ 
			 *
			eval.setExternalVariable( new QName("_") , new XValue(args).asXdmValue() );
			*/
			
			
			for( int i = 1 ; i <= args.size() ; i++ )
				eval.setExternalVariable( new QName("_" + i) , args.get(i-1).asXdmValue() );
			
			XdmValue result =  eval.evaluate();
			
			
			return result ;
			
			
			
			
		} catch (SaxonApiException e) {
			String msg = "Error expanding xml expression: " + arg ;
			mLogger.warn( msg , e );
			if( mLocation != null )
				mShell.printErr( mLocation.toString());
			mShell.printErr( msg  , e );
		    throw new CoreException(msg  , e );
		}
		finally {
			ShellContext.set(saved_shell);
		
		}

	
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
			if( c == '\\' && i < vslen -1 ){
				c = vs.charAt(++i);
				if( c != '"' && c != '\'' )
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
			
			sb.append(c);
		}
		String sbs = sb.toString();
		// Preserve original value/type of no dequoting was done
		if( vs.equals(sbs))
			return v;
		else
		return new XValue(sbs);
		
	}


	private List<XValue> expandWild(XValue v) {
		ArrayList<XValue> r = new ArrayList<XValue>();
		
		if( v.isXExpr() || v.isObject() ){
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
			if( c == '\\' && i < vslen-1 ){
				
				// sb.append(c);
				c = vs.charAt(++i);
				if( c != '"' && c != '\'' )
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
		
		
		String sbs = sb.toString();
		
		
		/*
		 * Special case if wildUnQuoted but string == "[" then this is just the test command
		 */
		if( wildUnQuoted && sbs.equals("["))
			wildUnQuoted = false ;
		
		if( ! wildUnQuoted ){

			// IF we havent unquoted or changed any value then preserve the original type/value
			if( sbs.equals(vs))
				r.add( v );
			else
				r.add( new XValue(sbs));
			return r;
		}
		
		
		vs = sbs;
		
		
		
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
		
		
		boolean bIsWindows = Util.isWindows();
		boolean caseSensitive = ! bIsWindows;
		
		/*
		 * Hack to handle 8.3 windows file names like "Local~1"
		 * If not matched and this is windows
		 * try an exact match to the canonical expanson of the dir and wild
		 */
		if(  bIsWindows && wild.indexOf('~') >= 0 ){
			File fwild = new File( dir , wild );
			if( fwild.exists() ){
				results.add(wild);
				return results ;
			}
			
			
			
		}
		
		
		
		
		
		String[] files = dir.list();
		for( String f : files ){
			
			
			boolean bMatched = Util.wildMatches( wild , f, caseSensitive );
			
			
			
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
	private XValue extractSingle(String var, boolean quoted , boolean btongs ) throws IOException, CoreException {
	
		XValue v = extractSingle(var);
		if( v == null || btongs || ! v.isAtomic() )
			return v;
		
		// Java objects are not mucked with
		if( v.isObject() )
			return v;

		String s = v.toString();
			
		if( ! quoted && Util.isEmpty(s))
			return null ;
			
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

	private XValue extractSingle(String varname) throws IOException, CoreException {
		
		
		
		if( varname.equals("#"))
			return new XValue( mShell.getArgs().size() );
		else
		// Special vars
		if( varname.equals("$"))
			return new XValue(Thread.currentThread().getId());
		else
		if( varname.equals("?"))
			return new XValue( mShell.getStatus());
		else
		if( varname.equals("!")){
			
				return new XValue( mShell.getLastThreadId() );
		}
		
			
		
		else {
			// ${#var} notation
			if( varname.startsWith("#") ) {
				varname = varname.substring(1);
				XValue val = mShell.getEnv().getVarValue( varname );
				int sz = (val == null || val.isNull()) ?  0 : val.asXdmValue().size();
				return new XValue( sz);
				
				
			}
			
			// Get the XVariable
			String ind = null; // [ind] expr
			String tie = null; // :tie expr
			
			// Strip off tie expr
			if( varname.contains(":")) {
				int as = varname.indexOf(':');
				if( as > 0 ){
					tie = varname.substring(as+1 );
					varname = varname.substring( 0 , as );
				}
				
			}
			
					
			
			// Look for array notation 
			// ${var[3]}
			if( varname.contains("[")){
				int as = varname.indexOf('[');
				ind = varname.substring(as+1 , varname.indexOf(']')).trim();
				/*
				 * Expand index if it starts with "$"
				 */
				if( ind.startsWith("$")){
					XValue indv = extractSingle( ind.substring(1) );
					if( indv != null )
						ind = indv.toString();
					
				}
				
				
				varname = varname.substring(0,as);
				
			}
			
			
			if( Util.isInt(varname,false)){
					int n = Util.parseInt(varname, -1);
					if( n == 0 )
						return new XValue(mShell.getArg0());
					else
					if( n > 0 && n <= mShell.getArgs().size() ){
						XValue value = mShell.getArgs().get(n-1);
						return ind == null ? value : new XValue( value.asXdmValue(ind) ) ;
						
					}
					else
						return null;	// unfound args, do not get used, 
			}
			

			
			
			XVariable var = mShell.getEnv().getVar(varname);
			if( var == null )
				return null;

			
			
			return var.getValue(  mShell, ind ,
					tie == null ? null : new XValue(tie) );
		}
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

