/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import org.xmlsh.util.Util;

import java.util.EnumSet;

// Evaluation options  - in order they are evaluated 
public enum EvalFlag {
	// String -> String
	PARSE_QUOTES,    // Process ",\ and '
	EXPAND_VAR,      // Expand variable references -> list of XValue
	JOIN_VALUES,      // Join values in string context 
	SPLIT_WORDS,     // Split string info words (IFS expansion)
	EXPAND_WILD,     // Glob (wildcards) the result
    OMIT_NULL,       // drop null values
	EXPAND_SEQUENCES // expand single values as sequences into multiple values 
	;
	

	public static EnumSet<EvalFlag>  expandVarFlags() { 
		return  EnumSet.of( EvalFlag.EXPAND_VAR );
	}
	
	public static EnumSet<EvalFlag>  expandWordsFlags() { 
		return  EnumSet.of( EvalFlag.SPLIT_WORDS );
	}
	
	
	public static EnumSet<EvalFlag>  expandSequencesFlags() { 
		return  EnumSet.of( EvalFlag.EXPAND_SEQUENCES );
	}
	
	public static EnumSet<EvalFlag>  expandWildFlags() { 
		return  EnumSet.of( EvalFlag.EXPAND_WILD );
	}
	
	// Was tongs
	public static EnumSet<EvalFlag>  preserveValueFlags() {
		return  EnumSet.of( EvalFlag.EXPAND_VAR);
	}
	

		

	public static EnumSet<EvalFlag>  evalFlags( boolean bExpandSequences , boolean bExpandWild , boolean bExpandWords, boolean bPreserve )
	{ 
		EnumSet<EvalFlag> flags = EnumSet.of( EXPAND_VAR);
		if( ! bPreserve ) {
			flags.add(PARSE_QUOTES);
			flags.add(JOIN_VALUES);
			if( bExpandSequences)
				flags.add(EXPAND_SEQUENCES );
			if( bExpandWild )
				flags.add(EXPAND_WILD);
			if( bExpandWords )
				flags.add(SPLIT_WORDS);
			flags.add(OMIT_NULL);
		}
		return flags ;
	}

	final static EnumSet<EvalFlag> _evalFlagsNone = EnumSet.noneOf(EvalFlag.class);
	
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