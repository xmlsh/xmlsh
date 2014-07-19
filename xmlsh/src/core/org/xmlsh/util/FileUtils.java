/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils
{

	public static String getNullFilePath() {
		if(Util.isWindows())
			return "NUL" ;
		else
			return "/dev/null";
	}
	public static File getNullFile() {
		return new File( getNullFilePath() );
	}

	public static boolean isNullFile( File file ) {
		return isNullFilePath(file.getName()) ;
	}
	public static boolean isNullFilePath(String file)
    {
		return Util.isBlank(file) || file.equals("/dev/null") ||
				(Util.isWindows() && file.equalsIgnoreCase("NUL"));
    }
	@SuppressWarnings("unchecked")
    public static  <T extends InputStream> T getInputStream(InputStream stream, Class<T> cls )
    {
	    if( stream == null )
	    	return null ;
	    if( cls.isInstance( stream ) )
	    	return (T) stream ;
	    if( stream instanceof SynchronizedInputStream )
	    	return getInputStream( ((SynchronizedInputStream)stream).getStream() , cls );
	    
		return null;
		
    }
	
	public boolean hasConsole() {
		return System.console() != null ;
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