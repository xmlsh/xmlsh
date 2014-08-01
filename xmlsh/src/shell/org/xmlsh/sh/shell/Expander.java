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
import org.xmlsh.sh.core.EvalUtils;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.Util;
import org.xmlsh.xpath.EvalDefinition;
import org.xmlsh.xpath.ShellContext;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class Expander {
	private static Logger mLogger = LogManager.getLogger( Expander.class);

	public Shell			mShell;
	private		SourceLocation 	mLocation ;

	/* 
	 * Attribute enums
	 */

	/* this"Is a 'string\' in' a $var "string *.x "*.y" \*.z */

	public Expander( Shell shell, SourceLocation loc )
	{
		mShell = shell;
		mLocation = loc ;
	}

	/*
	 * Expand a value into a Results buffer
	 * Used for combining possible joined values by repeated calls
	 * 
	 */
	public ParseResult expandValueToResult(XValue value, EvalEnv env , ParseResult result ) throws IOException, CoreException
	{
		assert( result != null );
		if(!env.preserveValue() && value.isAtomic() ) 
			result = expandStringToResult( value.toString() , env , result );
		else
			result.add( value , env.preserveValue() );
		return result ;
	}



	/*
	 * Expand a string to a list of XValues by  
	 * 1) Parsing Quotes
	 * 2) Expanding Variables
	 * 3) Tokenizing by IFS (expand word) and combining adjacent words 
	 * 4) globbing 
	 */

	public List<XValue> expandStringToList(String arg, EvalEnv env ) throws IOException, CoreException {
		ParseResult result = new ParseResult(mShell) ;
		return expandResultToList(env, expandStringToResult( arg , env, result ));

	}

	public List<XValue> expandResultToList(EvalEnv env, ParseResult result)
	{
		List<XValue> xvresult =  result.expandWild(env,mShell.getCurdir());

		if( env.expandSequences() )
			xvresult = Util.expandSequences( xvresult );
		else  
			xvresult = Util.combineSequence( xvresult );

		return xvresult;
	}


	/*
	 * Expand a single word value :
	 * <{{ ... }}> is a hard multi line quote with nothing inside touched
	 * <[ ... ]>  is an XQuery expression
	 * $xxx variable expressions
	 * "literal" simpmle literal
	 * "$var literal $var" quoted mixed literal
	 * 
	 * If bPreserve is set then this is inside {value} which is used to eval/expand the value but do NO substitution
	 * NO globbing is done
	 */
	public ParseResult expandStringToResult(String arg, EvalEnv env , ParseResult result ) throws IOException, CoreException
	{

		assert( result != null );

		// <{ big quotes }>
		if( arg.startsWith("<{{") && arg.endsWith("}}>")){
			// Add as a raw value
			result.add(new XValue(arg.substring(3,arg.length()-3)), true);
			return result ;
		}

		// <[ XEXPR ]>
		if( arg.startsWith("<[") && arg.endsWith("]>")){
			result.add(parseXExpr(mShell, arg.substring(2,arg.length()-2)), true);
			return result ;
		}

		char c;
		int i;
		CharAttr curAttr = env.preserveValue() ? CharAttr.ATTR_PRESERVE : CharAttr.ATTR_NONE ; 

		for( i = 0 ; i < arg.length() ; i++){

			c = arg.charAt(i);

			if(env.parseQuotes()) {
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
					continue ;
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
						continue ;
					}

			}

			if( ! curAttr.isHardQuote()  && c == '$'){
				if( ++i == arg.length() ){
					result.append('$', curAttr ); // Special case of a single "$"
					break;
				}

				StringBuffer sbv = new StringBuffer();
				if( arg.charAt(i) == '{') {
					i = EvalUtils.readToMatching( arg , i , sbv ,  '}' );
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
									result.append( mShell.getIFS().getFirstChar() , curAttr );
								result.append( v , env ,  curAttr );
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
										result.append( v , env , curAttr );
									else
										result.add( v );
									bFirst = false ;
								}
						}

						else {
							// get value from single variable, parse and field split
							// guarentees no null values and empty unquoted strings were removed


							List<XValue> vs = EvalUtils.evalVar(mShell, var, curAttr );

							// Append the first value to any previous content in the arg
							// N..last-1 become new args
							// Last is a new word but possibly appendable

							if( vs != null ) {
								int vsize = vs.size();
								for( int vi  = 0 ; vi < vsize ; vi++ ) {
									XValue v = vs.get(vi);
									if( vi > 0 )
										result.flush();
									result.append( v , env , curAttr  );
								}
							}

						}


				} else
					result.append('$' , curAttr );

			} else {
				result.append(c,curAttr);
			}

		}

		if( ! env.joinValues() )
			result.flush();

		return result ;

	}

	private XValue parseXExpr(Shell shell, String arg) throws CoreException {



		Processor processor = Shell.getProcessor();

		XQueryCompiler compiler = processor.newXQueryCompiler();
		compiler.setModuleURIResolver(new ShellModuleURIResolver(mShell));


		// Declare the extension function namespace
		// This can be overridden by user declarations
		compiler.declareNamespace("xmlsh", EvalDefinition.kXMLSH_EXT_NAMESPACE);

		NameValueMap<String> ns = shell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);

			}

		}




		XQueryExecutable expr = null;

		StringBuffer sb = new StringBuffer();


		Variables variables = shell.getEnv().getVars();
		Collection<String> varnames = variables.getVarNames();
		for( String name : varnames ) {
			XVariable value = variables.get(name);
			if( ! value.isNull() && value.getFlags().contains( XVarFlag.XEXPR ))

				sb.append("declare variable $").append(value.getName())
				.append(" external ;\n");

		}

		List<XValue> args = shell.getArgs();

		/*
		// Legacy support a single sequence $_ with all args
		sb.append("declare variable $_ external;\n");
		 */


		// 2010-02-04
		// Express each positional parameter as $_1 $_2 ...
		for( int i = 1 ; i <= args.size() ; i++ )
			sb.append("declare variable $_" + i + " external;\n");


		sb.append(arg);

		Shell saved_shell = ShellContext.set(shell);

		try {
			expr = compiler.compile( sb.toString() );

			XQueryEvaluator eval = expr.load();

			for( String name : varnames ) {
				XVariable value = variables.get(name);
				if( !value.isNull() && value.getFlags().contains( XVarFlag.XEXPR )){
					XValue v = value.getValue();
					if( v.isObject() )
						try {
							v = new XValue( (XdmValue) v.convert(XdmValue.class));
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


			return new XValue( TypeFamily.XDM , result  ) ;




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

