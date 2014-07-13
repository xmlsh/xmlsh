/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.EnumSet;

/*
 * Context for evaluating a Word or Expression 
 */
public class EvalEnvironment
{
	enum EvalFlag {
		EXPAND_WILD,
		EXPAND_WORDS,
		TONGS, EXPAND_SEQUENCES
	}
	
	
	private EnumSet<EvalFlag> evalFlags;
	private final static EnumSet<EvalFlag> _evalFlagsNone = EnumSet.noneOf(EvalFlag.class);

	private final static EvalEnvironment _evalNone = new EvalEnvironment();
	
	
	private EvalEnvironment()
	{
		this(_evalFlagsNone);
	}
	
	private EvalEnvironment(EnumSet<EvalFlag> flags){
		evalFlags = flags ;
	}

	

	public static final EvalEnvironment evalNone() {
		return _evalNone;
	}
	
	// Hack for now
	public static EvalEnvironment  newInstance( boolean bExpandSequences , boolean bExpandWild , boolean bExpandWords, boolean bTongs )
	{
		EnumSet<EvalFlag> flags = EnumSet.noneOf(EvalFlag.class);
		if( bExpandSequences)
			flags.add(EvalFlag.EXPAND_SEQUENCES );
		if( bExpandWild )
			flags.add(EvalFlag.EXPAND_WILD);
		if( bExpandWords )
			flags.add(EvalFlag.EXPAND_WORDS);
		if( bTongs )
			flags.add(EvalFlag.TONGS);
		return new EvalEnvironment(flags);
	}
	
	// Globbing 
	public boolean expandWild() { 
		return evalFlags.contains(EvalFlag.EXPAND_WILD);
	}
	
	// Word expansion 
	public boolean expandWords() {
		return evalFlags.contains(EvalFlag.EXPAND_WORDS);
	}
	
	public boolean tongs() {
		return evalFlags.contains(EvalFlag.TONGS);

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