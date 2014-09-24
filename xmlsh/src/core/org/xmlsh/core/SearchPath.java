/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;

// avoid java.nio.Path
public class SearchPath implements Iterable<String> {
	
	static Logger mLogger = LogManager.getLogger();
	public String toString() {
		return "PATH[" + Util.stringJoin(mPaths, ",-") + "]";
	}
	
	List<String>	mPaths = new ArrayList<String>();

	// Empty path
	public SearchPath( ) {
	}

	// Path populated with list of paths from a XValue which could be a sequence
	public SearchPath( XValue pathVar)
	{
		if( pathVar == null || pathVar.isNull())
			return ;
		for( XValue v : pathVar ){
			mPaths.add( v.toString() );
		}
	}

	public SearchPath( String[] vars )
	{
		for( String v : vars )
			mPaths.add(v);
	}

	public SearchPath( String path , String sep )
	{
		this( path.split(sep));
	}



	public void	add( String path )
	{
		mPaths.add(path);
	}

	public void 	addAll( String[] paths)
	{
		for( String path : paths ){
			add( path );
		}
	}

	@Override
	public Iterator<String> iterator() { return mPaths.iterator() ; }


	public String[] getPaths()
	{ 
		return mPaths.toArray( new String[mPaths.size()]);
	}


	public 	File	getFirstFileInPath( Shell shell , String fname ,  boolean isFile ) throws IOException
	{
		mLogger.entry( fname, isFile);
		for ( String path  : mPaths ){
			File dir = shell.getFile(path);
			File	target = new File( dir  , fname );
			
			if( target.exists() && ( isFile ? target.isFile() : target.isDirectory() ) )
				return mLogger.exit(target);
		}
		return mLogger.exit(null);
	}

	/*
	 * Convert to OS format string 
	 * 1) Convert directory seperator to \ (windows)
	 * 2) concatenate with path seperator 
	 */
	String 	toOSString()
	{
		StringBuffer sb = new StringBuffer();
		for( String ps : mPaths ){
			String s = FileUtils.fromJavaPath(ps);
			if( sb.length() > 0 )
				sb.append(File.pathSeparator);
			sb.append(s);

		}
		return sb.toString();
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
