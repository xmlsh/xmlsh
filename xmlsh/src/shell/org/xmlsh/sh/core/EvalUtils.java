/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import net.sf.saxon.s9api.XdmEmptySequence;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.CharAttr;
import org.xmlsh.sh.shell.Expander;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellConstants;
import org.xmlsh.types.ITypeFamily;
import org.xmlsh.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class EvalUtils
{

	/*
	 * Evaluate a variable expression and extract its value
	 */
	public static XValue evalVar(Shell shell, String varname) throws IOException, CoreException
	{

		if(varname.equals("#"))
			return new XValue(shell.getArgs().size());
		else
			// Special vars
			if(varname.equals("$"))
				return new XValue(Thread.currentThread().getId());
			else if(varname.equals("?"))
				return new XValue(shell.getStatus());
			else if(varname.equals("!")) {

				return new XValue(shell.getLastThreadId());
			}

			else {
				// ${#var} notation
				if(varname.startsWith("#")) {
					varname = varname.substring(1);
					XVariable var = shell.getEnv().getVar(varname);
					if(var == null)
						return new XValue(0);
					return new XValue( var.getSize());


				}

				// Get the XVariable
				String ind = null; // [ind] expr
				String tie = null; // :tie expr

				// Strip off tie expr
				if(varname.contains(":")) {
					int as = varname.indexOf(':');
					if(as > 0) {
						tie = varname.substring(as + 1);
						varname = varname.substring(0, as);
					}

				}

				// Look for array notation
				// ${var[3]}
				if(varname.contains("[")) {
					int as = varname.indexOf('[');
					ind = varname.substring(as + 1, varname.indexOf(']')).trim();
					/*
					 * Expand index if it starts with "$"
					 */
					if(ind.startsWith("$")) {
						XValue indv = evalVar(shell, ind.substring(1));
						if(indv != null)
							ind = indv.toString();

					}
					varname = varname.substring(0, as);
				}

				if(Util.isInt(varname, false)) {
					int n = Util.parseInt(varname, -1);
					if(n == 0)
						return new XValue(shell.getArg0());
					else if(n > 0 && n <= shell.getArgs().size()) {
						XValue value = shell.getArgs().get(n - 1);
						return ind == null ? value : getIndexedValue( value , ind );

					} else
						return null; // unfound args, do not get used,
				}


				XVariable var = shell.getEnv().getVar(varname);
				if(var == null)
					return null;
				if( ind == null && tie == null )
					return var.getValue();
				else
					return var.getValue(shell, ind, tie );
			}
	}

	public static XValue getIndexedValue(XValue xvalue, String ind) throws CoreException
	{
		assert( xvalue != null );
		assert( ! Util.isBlank(ind));

		if( xvalue == null )
			return XValue.nullValue();
		if( Util.isBlank(ind))
			return xvalue ;

		ITypeFamily tf = xvalue.typeFamilyInstance();
		if( tf == null )
			return xvalue ;

		Object obj =  xvalue.asObject() ;
		xvalue = tf.getMethods(obj.getClass()).getXValue(obj, ind );
		return xvalue ;
	}

	/*
	 * Recursively Expand a possibly multi-level wildcard rooted at a directory
	 * 
	 */
	public static List<String>	expandDir( File dir , CharAttributeBuffer wild, boolean bDirOnly )
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

	public static void expandDir( File dir , String parent , CharAttributeBuffer wilds[] , List<String> results )
	{
		CharAttributeBuffer wild = wilds[0];
		if( wilds.length < 2 )
			wilds = null ;
		else 
			wilds = Arrays.copyOfRange(wilds, 1, wilds.length );


		List<String> rs = EvalUtils.expandDir( dir , wild, wilds != null   );
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

	public static ParseResult expandStringToResult(Shell shell,String value , EvalEnv env, SourceLocation loc , ParseResult result ) throws IOException, CoreException {
		Expander e = new Expander( shell , loc );
		return e.expandStringToResult(value, env, result == null ? new ParseResult(shell) : result );

	}
	public static ParseResult expandValueToResult(Shell shell, XValue xv , EvalEnv env, SourceLocation loc , ParseResult result ) throws IOException, CoreException {
		Expander e = new Expander( shell , loc );
		return  e.expandValueToResult(xv, env  , result == null ? new ParseResult(shell) : result  );
	}


	public static List<XValue> expandResultToList(Shell shell, ParseResult result , EvalEnv env, SourceLocation loc  ) throws IOException, CoreException {
		Expander e = new Expander( shell , loc );
		return e.expandResultToList( env, result  );
	}

	public static List<XValue> expandValueToList(Shell shell, XValue xv , EvalEnv env, SourceLocation loc  ) throws IOException, CoreException {
		Expander e = new Expander( shell , loc );
		return e.expandResultToList( env, e.expandValueToResult(xv, env  , new ParseResult(shell) ) );
	}


	public static List<XValue> expandStringToList(Shell shell, String s , EvalEnv env, SourceLocation loc  ) throws IOException, CoreException {
		Expander e = new Expander( shell , loc );
		return  e.expandStringToList(s, env   );
	}

	public static String expandStringToString(Shell shell, String value , EvalEnv env, SourceLocation loc ) throws IOException, CoreException {
		List<XValue> ret = expandStringToList(shell,value, env, loc  );
		if( ret.size() == 0 )
			return "";
		else
			if( ret.size() == 1 )
				return ret.get(0).toString();
		return Util.joinValues(ret, ShellConstants.ARG_SEPARATOR);

	}

	// Expand a word and return as a single XValue
	// Preserves sequences and expands 
	public	static XValue	 expandStringToValue( Shell shell ,  String value , EvalEnv env, SourceLocation loc ) throws IOException, CoreException {
		List<XValue> ret = expandStringToList(shell,value, env, loc  );
		return expandListToValue(env, ret);

	}

	// Converts a List<XValue> into single XValue 
	public static XValue expandListToValue(EvalEnv env, List<XValue> ret)
	{
		if( ret.size() == 0 )
			return new XValue(  env.omitNulls() ? null : XdmEmptySequence.getInstance());
		else
			if( ret.size() == 1 )
				return ret.get(0);

		return new XValue( ret );
	}

	public static ParseResult expandListToResult(Shell shell , List<XValue> list , EvalEnv env, SourceLocation loc ) throws IOException, CoreException
	{
		Expander e = new Expander( shell , loc );
		ParseResult result  = new ParseResult(shell); 
		for( XValue xv : list )
			result = e.expandValueToResult(xv, env, result);
		return result ;
	}





	public	static XValue	 expandResultToValue( Shell shell ,  ParseResult result , EvalEnv env, SourceLocation loc ) throws IOException, CoreException {
		List<XValue> ret = expandResultToList(shell,result, env, loc  );
		return expandListToValue(env, ret);

	}


	public static int readToMatching( String arg , int i , StringBuffer sbv , char match )
	{
		char start = arg.charAt(i++);
		int matchCount = 1;

		// Eat up to match char '}'
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



	public static XValue splitStringToValue(Shell shell, String word, EvalEnv env) throws IOException
	{
		// if expand word then need to do IFS splitting
		if( env.expandWords() && ! env.preserveValue() )
			return new XValue( (String[]) shell.getIFS().split(word).toArray() );
		else
			return new XValue( word);
	}


	public static ParseResult splitStringToResult(Shell shell, String word, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException, CoreException
	{
		Expander e = new Expander( shell , loc );
		// if expand word then need to do IFS splitting
		if( env.expandWords() && ! env.preserveValue() ) {
			for( String s : shell.getIFS().split(word) )
				result = e.expandStringToResult(s, env, result);
		}
		else
			e.expandStringToResult(word, env, result);
		return result ;


	}

	/*
	 * Evaluate a variable and return either a list of zero or more values
	 */

	public static List<XValue> evalVar(Shell shell, String var, CharAttr attr ) throws IOException, CoreException {

		XValue v = evalVar(shell, var);
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
		fields = shell.getIFS().split(s );
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

	public static int getSize(XValue xvalue)
	{
		if( xvalue == null || xvalue.isNull() )
			return 0;
		ITypeFamily tf = xvalue.typeFamilyInstance();
		if( tf == null )
			return 0;
		assert( tf != null );
		Object obj =  xvalue.asObject() ;
		return tf.getMethods(obj.getClass()).getSize(obj);
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