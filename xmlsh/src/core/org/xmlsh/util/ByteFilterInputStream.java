/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.IOException;
import java.io.InputStream;

public class ByteFilterInputStream extends InputStream
{
	private InputStream mIn;
	private int mFilter; // store as int for less casting at runtime
	public ByteFilterInputStream(InputStream is, int filter )  // filter is really a byte ... no encoding
	{
		mIn = is ;
		mFilter = filter ;
	}
	@Override
    public int read() throws IOException
    {
		int c ;
	
		while( ( c = mIn.read()) == mFilter )
			;
		return c;
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