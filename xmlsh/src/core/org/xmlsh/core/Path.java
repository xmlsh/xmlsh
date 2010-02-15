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

import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class Path implements Iterable<String> {
	List<String>	mPaths = new ArrayList<String>();
	
	// Empty path
	public Path( ) {}
	
	// Path populated with list of paths from a XValue which could be a sequence
	public Path( XValue pathVar)
	{
		if( pathVar == null || pathVar.isNull())
			return ;
		for( XdmValue v : pathVar.asXdmValue() ){
			mPaths.add( v.toString() );
		}
	}
	
	public Path( String[] vars )
	{
		for( String v : vars )
			mPaths.add(v);
	}
	
	public Path( String path , String sep )
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
	
	public Iterator<String> iterator() { return mPaths.iterator() ; }
	
	
	public String[] getPaths()
	{ 
		return mPaths.toArray( new String[mPaths.size()]);
	}
	
	
	public 	File	getFirstFileInPath( Shell shell , String fname ) throws IOException
	{
		for ( String path  : mPaths ) {
			File dir = shell.getFile(path);
			File	target = new File( dir  , fname );
			if( target.exists() )
				return target;
		}
		return null;
		
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
			String s = Util.fromJavaPath(ps);
			if( sb.length() > 0 )
				sb.append(File.pathSeparator);
			sb.append(s);
			
		}
		return sb.toString();
	}

}
//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
