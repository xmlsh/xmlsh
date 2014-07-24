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
	public static String convertPath(String name, boolean bSystem) {
		if( bSystem && File.separatorChar != '/')
			return name.replace('/', File.separatorChar);
		else
			return name.replace(File.separatorChar, '/');
			
	}
	/*
	 * Reverse the conversion of toJavaPath
	 */
	public static String fromJavaPath( String path )
	{
		if( path == null )
			return null;
		if( File.separatorChar != '/')
			return path.replace('/' , File.separatorChar);
		else
			return path;
	}
	/**
	 * Convert a Path or name in DOS format to Java format
	 * This means converting \ to / 
	 */
	
	public static String toJavaPath( String path )
	{
		if( path == null )
			return null;
		if( File.separatorChar != '/')
			return path.replace(File.separatorChar, '/');
		else
			return path;
	}
	// Return the number of chars that include the root part of a path 
	// Include windows drive: 
	// Assumes java path format
	public static int rootPathLength(String path)
	{
		
		int len = 0;
		int plen = path.length();
		if( Util.isWindows() && plen >= 2 ) {
			char drive = path.charAt(0);
			// Character.isAlphabetic() is V7 only
			if( Character.isLetter(drive) && path.charAt(1) == ':')
				len = 2 ;
			
		}
		
		while( len < plen && path.charAt(len) == '/' )
		  len++;
	
		 return len ;
	}
	/*
	 * Special function that would return basename without extension if this is path-like
	 * but otherwise still does something useful - dont use if you know the string is really a path
	 */
	public static String basePathLikeName(String path)
	{
		path = getPathLikeName(path);
	
		int startpos = 0 ;
		int dotpos = path.indexOf('.', startpos);
		if( dotpos < 0 )
			dotpos = path.length();
		return path.substring(startpos,dotpos);
	}
	
	// Take a path like string and return just the name.ext component
	public static String getPathLikeName(String path)
    {

		if( Util.isBlank(path))
			return "" ;
		
		int startpos = 0;
		// get rid of any windowy drive paths and leading /s
		int rlen = FileUtils.rootPathLength(path); 
		if( rlen > 0 )
			startpos = rlen;
		
		int slashpos = path.lastIndexOf('/');
		int slashpos2  =  (File.separatorChar != '/' ) ? 
				path.lastIndexOf( File.separatorChar ) : -1 ;
		slashpos = Math.max(slashpos, slashpos2);
		if( slashpos > startpos )
			startpos = slashpos + 1 ;
		if( startpos >= rlen )
			return path.substring(startpos );
		return "";
    
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