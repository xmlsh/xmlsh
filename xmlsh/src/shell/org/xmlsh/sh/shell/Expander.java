/**
 * $Id: $
 * $DateTime: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.core.Command;
import org.xmlsh.sh.core.EvalScriptCommand;
import org.xmlsh.sh.grammar.ParseException;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.PipedStream;
import org.xmlsh.util.Util;

class Expander {
	
	private static Logger mLogger = LogManager.getLogger( Expander.class);
	
	private 	Shell		mShell;
	private		List<XValue> 	mArgs;
	
	private static class Result {
		StringBuffer	sb = new StringBuffer();
		List<XValue>	result = new ArrayList<XValue>();
		boolean			bQuoted;
		
		
		Result( boolean quoted )
		{
			bQuoted = quoted ;
		}
		
		void flush() 
		{
			if( sb.length() > 0 || (bQuoted && result.isEmpty()) ){
				// Quoted strings that evaluate to "" get saved as args
				add(sb.toString());
				sb.setLength(0);
			}

			
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

		public void append(XValue value) {
			if( value.isString() )
				append( value.toString());
			else {
				flush();
				add(value);
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
	

	public List<XValue> expand(String arg) {
		
		// 1a) single quote expansion
		if( arg.startsWith("'")){
			List<XValue> 	r = new ArrayList<XValue>(1);
			r.add( new XValue(arg.substring(1,arg.length()-1)));
			return r;
		}
		
		// ( XEXPR )
		if( arg.startsWith("<[") && arg.endsWith("]>")){
			List<XValue> 	r = new ArrayList<XValue>(1);
			r.add( new XValue( parseXExpr(arg.substring(2,arg.length()-2))));
			return r;
		}
		
		// $<(sub comd>)
		if( arg.startsWith("$<(") && arg.endsWith(")>")){
			List<XValue> 	r = new ArrayList<XValue>(1);
			r.add( new XValue( parseXCmd(arg.substring(3,arg.length()-2))));
			return r;
		}
		
		boolean bQuoted = false ;
		// 1b) double quote expansion 
		if( arg.startsWith("\"")){
			bQuoted = true ;
			arg = arg.substring(1,arg.length()-1);
		}

		Result	result = new Result(bQuoted);

		
		char c;
		int i;
		for( i = 0 ; i < arg.length() ; i++){
			c = arg.charAt(i);
			// Escape
			if( c == '\\'){
				if( i < arg.length())
					result.append(arg.charAt(++i));
				continue;
			}
			if( c == '$'){
				if( ++i == arg.length() )
					break;
				
				StringBuffer sbv = new StringBuffer();
				if( arg.charAt(i) == '{') {
					i = readToMatching( arg , i , sbv ,  '}' );
					
				} 
				else
				if( arg.charAt(i) == '(' )
				{
					StringBuffer sbc = new StringBuffer();
					i = readToMatching( arg , i , sbc , ')' );
					// Expand the result into a command output then continue
					result.append( runCmd(sbc.toString()).split("\n"));
					
				}
				
				else { 
					// Speical case 
					// $?  $*  $@ $$ $0...$9
					c = arg.charAt(i);
					if( c == '?' ||  c == '@' || c == '$' || c == '#' || c == '*' || Character.isDigit(c)){
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
						
						result.add( mArgs );
					} else
						value = extractSingle(var);
					
					
					if( value != null )
						result.append( value );
				}
			
			} else 
				result.append(c);
			
		}
		
		result.flush();
		
		// If quoted then dont do wildcard expansion
		if( bQuoted )
			return result.getResult();
		
		
		
		ArrayList<XValue> result2 = new ArrayList<XValue>();
		for( XValue v : result.getResult() ){
			List<XValue> r = expandWild( v );
			if( r != null )
				result2.addAll( r );
		}
			
		
		return result2;
		
		
	}

	/*
	 * Parse an XML command expression and build the XdmValue by running 
	 * a sub shell and parsing the XML through a pipe 
	 * NOTE: Not entirely sure this is better then simply outputting the XML as a string
	 * then parsing the result text.
	 */
	
	

	private XdmValue parseXCmd(String cmd) 
	{
	
		ShellThread sht = null;


		try {
			InputStream script = new StringBufferInputStream(cmd);
			PipedStream pipe = new PipedStream();

			Shell shell = mShell.clone();
			shell.getEnv().setStdout( pipe.getOutput() );
			shell.getEnv().setStdin( new NullInputStream() );
			
			Command c = new EvalScriptCommand( cmd );
			
			sht = new ShellThread( shell , c );


			 sht.start();
			
			 DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
			 XdmNode node = builder.build(new StreamSource(pipe.getInput()));
			 
			if( sht != null )
				sht.join();
				
			return node ;
			 
		} catch ( Exception e )
		{
			mShell.printErr("Exception running command", e); 
			return null;
			
			
		} 
		
		
		
	}
		
	private XdmValue parseXExpr(String arg) {
		
		
	
		
		Processor processor = mShell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		XQueryExecutable expr = null;

		StringBuffer sb = new StringBuffer();
		

		for( String name : mShell.getEnv().getVarNames() ){
			XValue value = mShell.getEnv().getVarValue(name);
			
			sb.append("declare variable $").append(name)
			.append(" as ")
			.append( value.isString() ? "xs:string" : "item()*")
			.append(" external ;\n");
			
		}
		sb.append("declare variable $_ external;\n");
		
		
		sb.append(arg);
		try {
			expr = compiler.compile( sb.toString() );
			
			XQueryEvaluator eval = expr.load();
			for( String name : mShell.getEnv().getVarNames() ){
				XValue value = mShell.getEnv().getVarValue(name);
				eval.setExternalVariable( new QName(name), value.toXdmValue());
				
			}
			
			eval.setExternalVariable( new QName("_") , new XValue(mShell.getArgs()).toXdmValue() );
			
			
			return eval.evaluate();
			
		} catch (SaxonApiException e) {
			mLogger.warn("Error expanding xml expression: " + arg , e );
			mShell.printErr("Error expanding xml expression");
		}

		return null;
		
	
		
		
		
		
	}

	private String runCmd(String cmd) {

		
		InputStream script = new StringBufferInputStream(cmd);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Shell shell = mShell.clone();
		try {
		
			shell.getEnv().setStdout( out );
			shell.getEnv().setStdin( new NullInputStream() );
			shell.runScript(script);
			
			
			return out.toString();
	
			
			
		} catch (ParseException e) {
			shell.printErr( e.getMessage() );
			return "";
		} 
	
		
		finally {
			shell.close();
			try {
				script.close();
			} catch ( IOException e) {
				mLogger.error("IOException closing buffered script",e);
			}
			
			
		}

		
	}

	/*
	 * hasWild - returns true if the string has any wildcard chars "*?[]"
	 * 
	 */
	private boolean hasWild(String s)
	{
		// Special case "[" alone
		if( s.equals("["))
			return false ;
		return Util.hasAnyChar( s , "*?[]");
		
		
		
	}
	


	private List<XValue> expandWild(XValue v) {
		ArrayList<XValue> r = new ArrayList<XValue>();
		
		if( ! hasWild(v.toString())){
			r.add( v);
			return r;
		}
		
		String vs = v.toString();
		int slash = vs.lastIndexOf('/');
		File dir = null;
		String dirName = null;
		String wild;
		
		if( slash >= 0 ){
			dir = new File( mShell.getCurdir() , dirName = vs.substring(0,slash));
			wild = vs.substring(slash+1);
		}
		else {
			wild = vs;
			dir  = mShell.getCurdir(); 
			
		}

		
		String[] files = dir.list();
		for( String f : files ){
			if( Util.wildMatches( wild , f))
				r.add( new XValue(dirName == null ? f : dirName + "/" + f ));
		}
		
		// If no matches then use arg explicitly
		if( r.size() == 0)
			r.add(v);
		
		return r;
		
		
	}



	private XValue extractSingle(String var) {
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
		else
			return mShell.getEnv().getVarValue( var );
	}

}

//
//
//Copyright (C) 2008, David A. Lee.
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

