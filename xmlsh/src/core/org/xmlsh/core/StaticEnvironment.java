/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core;

import java.util.EnumSet;

public class StaticEnvironment 
{

	private EnumSet<StaticContextFlag> mFlags = EnumSet.noneOf(StaticContextFlag.class);
	private static final EnumSet<StaticContextFlag> noFlags = EnumSet.noneOf(StaticContextFlag.class);
	private static final StaticEnvironment _defaultEnv  = new StaticEnvironment();


	private StaticEnvironment(  EnumSet<StaticContextFlag>  flags ) {
		mFlags = flags ;
	}

	private StaticEnvironment() {
		mFlags = noFlags ;
	}
	/**
	 * @return the tongs
	 */
	public boolean getTongs()
	{
		return mFlags.contains(StaticContextFlag.BLOCK_TONG);
	}


	public static final StaticEnvironment defaultContext()
	{
		return _defaultEnv;
	}

	public StaticEnvironment addContext( StaticContextFlag flag ) {
		StaticEnvironment newEnv = this ;
		if( newEnv == _defaultEnv )
			newEnv = new StaticEnvironment( EnumSet.of(flag));
		else 
			mFlags.add(flag);
		return newEnv;
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