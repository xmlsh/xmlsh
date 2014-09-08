/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.shell;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
/*
 * "Input Field Seperator"
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.Util;


/*
 2.6.5 Field Splitting

After parameter expansion ( Parameter Expansion), command substitution ( Command Substitution), and arithmetic expansion ( Arithmetic Expansion), the shell shall scan the results of expansions and substitutions that did not occur in double-quotes for field splitting and multiple fields can result.

The shell shall treat each character of the IFS as a delimiter and use the delimiters to split the results of parameter expansion and command substitution into fields.

If the value of IFS is a <space>, <tab>, and <newline>, or if it is unset, any sequence of <space>s, <tab>s, or <newline>s at the beginning or end of the input shall be ignored and any sequence of those characters within the input shall delimit a field. For example, the input:

<newline><space><tab>foo<tab><tab>bar<space>

yields two fields, foo and bar.

If the value of IFS is null, no field splitting shall be performed.

Otherwise, the following rules shall be applied in sequence. The term " IFS white space" is used to mean any sequence (zero or more instances) of white space characters that are in the IFS value (for example, if IFS contains <space>/ <comma>/ <tab>, any sequence of <space>s and <tab>s is considered IFS white space).

IFS white space shall be ignored at the beginning and end of the input.

Each occurrence in the input of an IFS character that is not IFS white space, along with any adjacent IFS white space, shall delimit a field, as described previously.

Non-zero-length IFS white space shall delimit a field.

 */
public class IFS
{

	static final String DEFAULT_IFS = " \t\n";
	private boolean bDefault ;
	private String   ifs;    // IFS



	private boolean bInit = false ;
	private boolean bValid = false ;

	private String   ws;  // whitespace in IFS
	private String   nws; // NOT whitepsace in IFS
	private Pattern  leading_ws;   // Leading IFSWS pattern
	private Pattern  trailing_ws;  // Trailing IFSWS pattern
	private Pattern  delim;		      // intraword delimiter pattern
	private static Logger mLogger = LogManager.getLogger();


	public IFS(String sIFS)
	{

		if( sIFS == null ) {
			ifs = DEFAULT_IFS ;
			bDefault = true ;
		} else {
			ifs=sIFS;
			bDefault = DEFAULT_IFS.equals( ifs );
		}


	}



	private void compile()
	{

		if( bDefault ) {
			ws = ifs ;
			nws  = "";
		} else {
			// get all whitespace - use regex to define whitespace
			ws = ifs.replaceAll("[^\\s]",""  );
			nws = ifs.replaceAll("\\s","");
		}

		/*	
		 * Compiling can produce runtime exceptions !
		 */
		try {
			// Leading ws pattern - no escaping needed
			leading_ws = Util.isEmpty(ws) ? null : Pattern.compile( "^[" + ws + "]+");
			trailing_ws = Util.isEmpty(ws) ? null : Pattern.compile( "[" + ws + "]+$");
			if( bDefault ) // delim is ifs
				delim = Pattern.compile( "[" + ifs + "]+");
			else {
				String nwsDelim = "["+Pattern.quote(nws) + "]";
				if( Util.isEmpty(ws)) {
					delim =  Pattern.compile( nwsDelim );
				} else
					// Delim is (nws)(ws*) -- nws needs possible escaping 
					delim = Pattern.compile( nwsDelim + "[" + ws + "]*" );
			}	
			bValid = true ;

		} catch( IllegalArgumentException e ){
			mLogger.info("Exception compiling IFS - bypassing",e);
			ifs = DEFAULT_IFS ;
			bDefault = true ;
		}
		finally {
			bInit = true ;
		}



	}

	boolean isNull() { return Util.isEmpty(ifs); }
	boolean isDefault() { return bDefault ; }
	public char getFirstChar(){
		return isNull() ? '\0' : ifs.charAt(0);
	}


	// Trim leading and trailing WS as per rules
	private String trimWS( String word ) {
		if( ! bValid )
			return word.trim();

		if(leading_ws != null )
			word = leading_ws.matcher(word).replaceFirst("");
		if( trailing_ws != null )
			word = trailing_ws.matcher(word).replaceFirst("");
		return word ;


	}
	public List<String> split( String word ) throws IOException {


		// SNH
		if( isNull() )
			return Collections.singletonList( word );

		if(! bInit )
			compile();

		word = trimWS( word );
		if( word.isEmpty())
			return Collections.emptyList() ;

		if( ! bValid )
			return Arrays.asList(word.split("\\s+"));
		// split by the delimiter
		return Arrays.asList(delim.split(word , 0));


	}
	public boolean isCurrent(String sIFS)
	{
		if( sIFS == null )
			return isDefault() ;

		return sIFS.equals(ifs);

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