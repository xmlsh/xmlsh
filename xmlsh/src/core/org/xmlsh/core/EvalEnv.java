/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.Arrays;
import java.util.EnumSet;

/*
 * Context for evaluating a Word or Expression 
 */
public class EvalEnv

{
	private EnumSet<EvalFlag> evalFlags;
	private final static EnumSet<EvalFlag> _evalFlagsNone = EnumSet.noneOf(EvalFlag.class);

	private final static EvalEnv _evalNone = new EvalEnv();
	
	
	private EvalEnv()
	{
		this(_evalFlagsNone);
	}
	
	private EvalEnv(EnumSet<EvalFlag> flags){
		evalFlags = flags ;
	}

	public static EvalEnv instance(EvalFlag... flags) {
		return new EvalEnv( EnumSet.copyOf(Arrays.asList(flags)) );
	 }
	
	

	public static final EvalEnv evalNone() {
		return _evalNone;
	}
	
	
	
	// Hack for now
	public static EvalEnv  newInstance( boolean bExpandSequences , boolean bExpandWild , boolean bExpandWords, boolean bPreserve )
	{
		EnumSet<EvalFlag> flags = EnumSet.noneOf(EvalFlag.class);
		flags.add(EvalFlag.EXPAND_VAR);
		if( ! bPreserve ) {
			flags.add(EvalFlag.PARSE_QUOTES);
			flags.add(EvalFlag.JOIN_VALUES);
			if( bExpandSequences)
				flags.add(EvalFlag.EXPAND_SEQUENCES );
			if( bExpandWild )
				flags.add(EvalFlag.EXPAND_WILD);
			if( bExpandWords )
				flags.add(EvalFlag.SPLIT_WORDS);
			flags.add(EvalFlag.OMIT_NULL);
		}
		return new EvalEnv(flags);
	}
	
	public static EvalEnv  newInstance(  boolean bExpandWild , boolean bExpandWords, boolean bPreserve )
	{
		return newInstance( false , bExpandWild, bExpandWords , bPreserve );
	}
	
	public boolean expandVar () { 
		return evalFlags.contains(EvalFlag.EXPAND_VAR);
	}
	
	public boolean parseQuotes () { 
		return evalFlags.contains(EvalFlag.PARSE_QUOTES);
	}
	
	public boolean joinValues () { 
		return evalFlags.contains(EvalFlag.JOIN_VALUES);
	}
	
	// Globbing 
	public boolean expandWild() { 
		return evalFlags.contains(EvalFlag.EXPAND_WILD);
	}
	
	// Word expansion 
	public boolean expandWords() {
		return evalFlags.contains(EvalFlag.SPLIT_WORDS);
	}
	
	// Was tongs
	public boolean preserveValue() {
		return evalFlags.isEmpty() || evalFlags.equals( EnumSet.of( EvalFlag.EXPAND_VAR));

	}
	public boolean expandSequences() {
		return evalFlags.contains(EvalFlag.EXPAND_SEQUENCES);

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