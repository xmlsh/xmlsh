package org.xmlsh.util;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.EnumSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.xmlsh.sh.core.CharAttributeBuffer;
import org.xmlsh.util.UnifiedFileAttributes.MatchFlag;

/*
 * Match a Path against names, attributes and globs 
 * 
 */

public class PathMatchOptions {
	private  EnumSet<MatchFlag>  mMatchMask;
	private EnumSet<MatchFlag>  mHideMask;     // Hide mask has precidence 
	

    public static class NameMatcher {

    	 private Pattern     mMatchPattern = null ;
	     private String      mMatchLiteral = null ;
	     private boolean    mCaseSensitive = false ;
	     
	    
		public NameMatcher(Pattern matchPattern) {
			mMatchPattern = matchPattern;
		}
		public NameMatcher(	String matchLiteral, boolean bCase ) {
			mMatchLiteral = matchLiteral;
			mCaseSensitive = bCase ;
		}

		public boolean matches(String name ){
			if( name == null )
				return false ;
			if( mMatchLiteral != null )
				return 
				  mCaseSensitive ? mMatchLiteral.equals(name) : mMatchLiteral.equalsIgnoreCase(name);
		    if( mMatchPattern != null )
		    	return 
		    	  mMatchPattern.matcher(name).matches();
		   
		     return false ;
			
			
		}
		 
	 };
	 
    private NameMatcher mNameMatcher = null ;

	public PathMatchOptions(PathMatchOptions that) {
		mMatchMask = that.mMatchMask.clone();
		mHideMask =  that.mHideMask.clone();
		mNameMatcher = that.mNameMatcher;
	}
	
	public PathMatchOptions clone()
	{ 
		return new PathMatchOptions(this);
	}

	
	public PathMatchOptions() {
		mMatchMask = EnumSet.allOf(MatchFlag.class);
		mHideMask  = EnumSet.noneOf(MatchFlag.class);
	}
	public PathMatchOptions(EnumSet<MatchFlag> matchFlags , EnumSet<MatchFlag> hideFlags) {
		mMatchMask = matchFlags;
		mHideMask  =hideFlags;
	}
	
	public PathMatchOptions withNameMatching( String literal , boolean bCase) {
		mNameMatcher = new NameMatcher( literal , bCase );
		return this ;
	}
	public PathMatchOptions withWildMatching( Pattern pattern ) {
		mNameMatcher = new NameMatcher(pattern) ;
		return this;
	}
	public PathMatchOptions withWildMatching( String wild  , boolean bCase ) {
		try {
			Pattern pattern = Util.compileWild(wild, bCase );
			return withWildMatching( pattern );

		} catch( PatternSyntaxException e ) {
			return withNameMatching( wild , bCase );
		}

	}
	public PathMatchOptions withWildMatching(String wild) {
		return withWildMatching( wild ,  FileUtils.isFilesystemCaseSensitive() );
	}

	
	public PathMatchOptions withFlagsMatching( MatchFlag... flags)
	{
		mMatchMask = Util.withEnumsAdded(mMatchMask, flags);
		return this ;
	}
	public PathMatchOptions withFlagsHidden( MatchFlag... flags)
	{
		mHideMask = Util.withEnumsAdded(mHideMask, flags);
		return this ;
	}
	
	public PathMatchOptions withoutFlagsHidden( MatchFlag... flags  ){
		mHideMask = Util.withEnumsRemoved(mHideMask , flags ) ; 
		return this ;
	}
	

	public PathMatchOptions withoutFlagsMatched( MatchFlag... flags  ){
		mMatchMask = Util.withEnumsRemoved(mMatchMask, flags ) ; 
		return this ;
	}

	public boolean doVisit( Path path , LinkOption... followLinks ){
		return doVisit( path , FileUtils.getUnifiedFileAttributes(path, followLinks));
	} 


	public boolean doVisit( Path path , UnifiedFileAttributes attrs ){

		if( mHideMask != null && attrs.isAnyFlagMatch(mHideMask) )
			return false ;

		if( mMatchMask != null && ! attrs.isAnyFlagMatch(mMatchMask) )
			return false ;
		if( mNameMatcher != null && ! mNameMatcher.matches( path.getFileName().toString()))
			return false ;
		return true ;
	}

	

}