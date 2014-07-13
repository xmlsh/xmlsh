/**
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

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
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.Variables;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.XVariable.XVarFlag;
import org.xmlsh.sh.core.CharAttributeBuffer;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

class Expander {
	private static final String sSEPSPACE = " ";
	private static Logger mLogger = LogManager.getLogger( Expander.class);

	private 	Shell			mShell;
	private		SourceLocation 	mLocation ;
	private IFS mIFS ;

	
	/* 
	 * Attribute enums
	 */
	
	/* this"Is a 'string\' in' a $var "string *.x "*.y" \*.z */
	
	private static class Result {
		// Attribute'd char buffer
		private CharAttributeBuffer    achars = null;


		static class RXValue {
			private XValue         			xvalue;
			private CharAttributeBuffer    avalue;
			private boolean       bRaw;
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
		
		RXValue			cur = null;		// Current XValue if its unknown to convert to string, only atomic values
		List<RXValue>	result = new ArrayList<RXValue>();

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


		List<RXValue> 	getResult()
		{
			flush();
			return result ;
		}

		/*
		 * Append a value to the result buffer
		 * If currently in-quotes then convert the args to strings and seperated by the first char in IFS.
		 */
		public void append(XValue value, CharAttr attr  ) {
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
						append(value , attr );
					else {
						// Flatten sequences
						boolean bFirst = true ;
						for( XdmValue v : value.asXdmValue() ){
							if( ! bFirst )
								append(sSEPSPACE /* NOT IFS FIRST CHAR */ , attr );
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
   
		
	};



	Expander( Shell shell, SourceLocation loc )
	{
		mShell = shell;
		mLocation = loc ;
		mIFS = mShell.getIFS();

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


	/*
	 * Expand a single word value :
	 * <{{ ... }}> is a hard multi line quote with nothing inside touched
	 * <[ ... ]>  is an XQuery expression
	 * $xxx variable expressions
	 * "literal" simpmle literal
	 * "$var literal $var" quoted mixed literal
	 * 
	 * If bTongs is set then this is inside {value} which is used to eval/expand the value but do NO substitution
	 * if bExpandWild is set then globbing is done
	 */
	List<XValue> expandToList(String arg, EvalEnv env ) throws IOException, CoreException
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

		char c;
		int i;
		CharAttr curAttr = env.preserveValue() ? CharAttr.ATTR_PRESERVE : CharAttr.ATTR_NONE ; 
		
		for( i = 0 ; i < arg.length() ; i++){

			c = arg.charAt(i);
			
			// Quote - if in quotes then clear only the matching quote
			if( CharAttr.isQuote(c) ){
				CharAttr ca = CharAttr.valueOf(c);
				if( curAttr.isQuote() ) { // in quotes
					curAttr.clear( ca );
                    if( curAttr.isQuote() )
                    	result.append(c, curAttr);
				}
				else {
					result.append( (String)null, curAttr);
					curAttr.set( ca );
				}
			}


			// Escape
			//  foo\bar		-> 	foobar
			//  "foo\bar" 	-> "foo\bar"
			//  "foo\\bar" 	-> "foo\bar"
			//	'foo\\bar'  -> 'foo\\bar'


			/*
			 * http://pubs.opengroup.org/onlinepubs/009695399/utilities/xcu_chap02.html
			 */
			else
			if( c == '\\'){
				if( curAttr.isHardQuote())
					result.append(c, curAttr);
				else 
					
					if( i < arg.length()){
						char nextc = arg.charAt(++i);
						if( curAttr.isSoftQuote())
						{
							switch( nextc ) {
							case '$' : 
							case '`':
							case '"' :
							case '\\' :
							case '\n' :
								break ;
							default :
								result.append(c,curAttr);
								break;
							}
						}
						
						// For one char we escape 
						CharAttr cAttr = CharAttr.ATTR_ESCAPED ;
						cAttr.set(curAttr);
						result.append( nextc , cAttr );
					}
			}
			else
			if( ! curAttr.isHardQuote()  && c == '$'){
				if( ++i == arg.length() ){
					result.append('$', curAttr ); // Special case of a single "$"
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
					if( var.equals("*")){
						// Add all positional variables as args 
						boolean bFirst = true ;
						for( XValue v : mShell.getArgs() ){
							if( curAttr.isSoftQuote() ) {
								if( ! bFirst )
									result.append( mIFS.getFirstChar() , curAttr );
								result.append( v , curAttr );
							}
							else
							{
								result.flush();
								result.add( v );
							}
							bFirst = false ;
						}

					} 
					else
					if( var.equals("@")){
						// Add all positional variables as args except "$@" with 0 args is dropped
						boolean bFirst = true ;
						List<XValue> args = mShell.getArgs();
						if( args.isEmpty() ) 
							result.resetIfEmpty();
						else
						
						for( XValue v : args ){
							if( curAttr.isSoftQuote() && bFirst  ) 
								result.append( v , curAttr );
							else
								result.add( v );
							bFirst = false ;
						}
					}

					else {
						// get value from single variable, parse and field split
						// guarentees no null values and empty unquoted strings were removed
						
						
						List<XValue> vs = evalVar(var, curAttr );

						// Append the first value to any previous content in the arg
						// N..last-1 become new args
						// Last is a new word but possibly appendable
						
						if( vs != null ) {
							int vsize = vs.size();
							for( int vi  = 0 ; vi < vsize ; vi++ ) {
								XValue v = vs.get(vi);
								if( vi > 0 )
									result.flush();
								result.append( v , curAttr  );
							}
						}
						
					}


				} else
					result.append('$' , curAttr );

			} else {
					result.append(c,curAttr);
			}

		}

		result.flush();

		ArrayList<XValue> result2 = new ArrayList<XValue>();


		for( Result.RXValue rv : result.getResult() ){
			/*
			 * If v is an atomic string value then dequote and expand
			 * DO NOT dequote variable expansions
			 */
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
                        } catch (Exception e) {
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
			mShell.printErr( msg  , e , mLocation );
			throw new CoreException(msg  , e );
		}
		finally {
			ShellContext.set(saved_shell);

		}


	}



	private List<XValue> expandWild( Result.RXValue rv ) {
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
			if( xv.isXExpr() || xv.isObject() ){
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
			if(  ( c == '*' || c == '?' || c == '[')  &&
					( av.attrAt(i) == 0 ) ) {
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
			if( Character.isAlphabetic(drive) && av.charAt(1) == ':'){
				
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

	private List<String>	expandDir( File dir , CharAttributeBuffer wild, boolean bDirOnly )
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



	/*
	 * Recursively Expand a possibly multi-level wildcard rooted at a directory
	 * 
	 */

	private void expandDir( File dir , String parent , CharAttributeBuffer wilds[] , List<String> results )
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
	 * Evaluate a variable and return either a list of zero or more values
	 */
	
	private List<XValue> evalVar(String var, CharAttr attr ) throws IOException, CoreException {

		XValue v = evalVar(var);
		if( v == null )
			return null ; 
		if(  attr.isPreserve() || ! v.isAtomic() )
			return Collections.singletonList( v );
		
 
		// Non tong null values go away
		if( ! attr.isPreserve() && v.isNull())
		   return null;   
		
		// Java objects are not mucked with
		if( v.isObject() )
			return Collections.singletonList( v );

		
		List<String> fields;
		if(  attr.isQuote()  || ! v.isString() )
			return Collections.singletonList( v );

		String s = v.toString();
		// Extract fields
		fields = mIFS.split(s );
		if( Util.isEmpty(fields))
			return null ;
		
	     // Try to preserve original value
		if( fields.size() == 1 &&  Util.isEqual(fields.get(0),s)  )
				return Collections.singletonList(v);
		
		List<XValue> xv = new ArrayList<XValue>( fields.size());
		for( String f : fields ) {
			if( Util.isEmpty(f))
				continue ;
			xv.add( new XValue(f ));
		}
		return xv;

	}

	/*
	 * Evaluate a variable expression and extract its value
	 */

	private XValue evalVar(String varname) throws IOException, CoreException {



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
								XValue indv = evalVar( ind.substring(1)  );
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

