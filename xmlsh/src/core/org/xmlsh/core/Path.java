/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Path {
	List<String>	mPaths = new ArrayList<String>();
	
	// Empty path
	public Path( ) {}
	
	// Path populated with list of paths
	public Path( String[] paths)
	{
		addAll( paths );
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
	
	public Iterator<String> iterate() { return mPaths.iterator() ; }
	
	
	public String[] getPaths()
	{ 
		return mPaths.toArray( new String[mPaths.size()]);
	}
	
	
	public 	File	getFirstFileInPath( String fname )
	{
		for ( String path  : mPaths ) {
			File	target = new File( path , fname );
			if( target.exists() )
				return target;
		}
		return null;
		
	}

}
//
//
//Copyright (C) 2008, David A. Lee.
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
