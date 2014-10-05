package org.xmlsh.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathMatchOptions {
	public boolean mRecursive;
	public boolean mHidden;
	public boolean mSystem;
	public boolean mDirectoriesOnly;

	public PathMatchOptions(boolean recursive, boolean hidden, boolean system, boolean dirsOnly) {
		mRecursive = recursive;
		mHidden = hidden;
		mSystem = system;
		mDirectoriesOnly = dirsOnly;
	}

	public boolean doVisit( Path path ){
		if( ! mHidden && FileUtils.isHidden(path) ) 
			return false ;
		if( ! mSystem && FileUtils.isSystem( path ) )
			return false ;
		if( mDirectoriesOnly &&  !Files.isDirectory(path) )
			return false ;

		return true ;
	}
}