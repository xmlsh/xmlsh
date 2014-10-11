package org.xmlsh.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.Set;


public class UnifiedFileAttributes {
	   public static enum FileType {
		   FILE("file"),
		   DIRECTORY("dir"),
		   SYMLINK("link"),
		   OTHER("other") ;
		   
		   private String name;
		   FileType( String name){
			   this.name = name ;
		   }
		   
		   public String toString() 
		   { 
		      return name ;
		   }
	   };
	   

		public static enum MatchFlag {
			HIDDEN_SYS,
			HIDDEN_NAME,
			SYSTEM,
			FILES,
			DIRECTORIES,
			LINKS,
			OTHER,
			READABLE,
			WRITABLE,
			EXECUTABLE
			
		}

public static class PathMatchOptions {
	EnumSet<MatchFlag>  mMatchMask;
	EnumSet<MatchFlag>  mHideMask;     // Hide mask has precidence 

	
	
	public PathMatchOptions() {
		mMatchMask = EnumSet.allOf(MatchFlag.class);
		mHideMask  = EnumSet.noneOf(MatchFlag.class);
	}
	public PathMatchOptions(EnumSet<MatchFlag> matchFlags , EnumSet<MatchFlag> hideFlags) {
		mMatchMask = matchFlags;
		mHideMask  =hideFlags;
	}
	
	public PathMatchOptions withFlagMatching( MatchFlag flag ){
		mMatchMask = Util.withEnumAdded(mMatchMask , flag );
		return this ;
	}
	public PathMatchOptions withFlagMatching( MatchFlag flag , boolean on ){
		mMatchMask = on ? Util.withEnumAdded(mMatchMask , flag ) : 
			Util.withEnumRemoved(mHideMask , flag ) ; 
		return this ;
	}
	
	public PathMatchOptions withFlagHidden( MatchFlag flag ){
		mHideMask = Util.withEnumAdded(mHideMask , flag );
		return this ;
	}
	public PathMatchOptions withFlagHidden( MatchFlag flag , boolean on ){
		mHideMask = on ? Util.withEnumAdded(mHideMask , flag ) : 
			Util.withEnumRemoved(mHideMask , flag ) ; 
		return this ;
	}

	public PathMatchOptions withFlag( MatchFlag flag , boolean showHide ){
	  if( showHide )
		  mMatchMask = Util.withEnumAdded(mMatchMask , flag);
	  
	  else
		  mHideMask = Util.withEnumAdded(mHideMask , flag ) ;
 
		  return this ;
    	
	}
	
	public boolean doVisit( Path path , boolean followLinks ){
		return doVisit( path , FileUtils.getUnifiedFileAttributes(path, followLinks));
	} 
	
	
	public boolean doVisit( Path path , UnifiedFileAttributes attrs ){

		if( attrs.isAnyFlagMatch(mHideMask) )
			return false ;
		
		return attrs.isAnyFlagMatch(mMatchMask) ;
	}


	
	
	
}
	  

	   private PosixFileAttributes  posix;
	   private BasicFileAttributes  basic;
	   private DosFileAttributes    dos ;
	   private Set<PosixFilePermission>  posixPermissions;
	   private Path mPath ;
	   
	   
	public UnifiedFileAttributes(Path path, boolean followLinks) {
		this( path , null , followLinks );
		   
		   
	}
	public UnifiedFileAttributes(Path path, BasicFileAttributes attrs, boolean followLinks) {
		   mPath = path;
		   setBasic(attrs) ;
		   setPosix(FileUtils.getPosixFileAttributes( path , followLinks ));
		   setDos(FileUtils.getDosFileAttributes( path , followLinks ));
		   if( getPosix() != null  ){
			   if( getBasic() == null )
			      setBasic(getPosix()) ;
			   posixPermissions = getPosix().permissions();
		   }
		   
		   if( getBasic() == null ){
			   if( getDos() != null )
				   setBasic(getDos())  ;
			   else
				   setBasic(FileUtils.getBasicFileAttributes( path , followLinks ));
		   }
		   if(posixPermissions  == null ){
			   posixPermissions= FileUtils.emulatePosixFilePermissions(path, followLinks );
		   }
	
	}
	public boolean hasBasic() {
		return getBasic() != null ;
	}
	public boolean hasPosix() {
		return getPosix() != null ;
	}
	public boolean hasDos() {
		return getDos() != null ;
	}
	public boolean hasAny() {
		return hasBasic() || hasDos() || hasPosix() ;
	}
	public UserPrincipal owner() {
		return getPosix().owner();
	}
	public FileTime lastModifiedTime() {
		return getBasic().lastModifiedTime();
	}
	public FileTime lastAccessTime() {
		return getBasic().lastAccessTime();
	}
	public FileTime creationTime() {
		return getBasic().creationTime();
	}
	public boolean isRegularFile() {
		return getBasic().isRegularFile();
	}
	public boolean isDirectory() {
		return getBasic().isDirectory();
	}
	public boolean isSymbolicLink() {
		return getBasic().isSymbolicLink();
	}
	public boolean isOther() {
		return getBasic().isOther();
	}
	public long size() {
		return getBasic().size();
	}
	public Object fileKey() {
		return getBasic().fileKey();
	}
	public boolean isArchive() { 
		return 
		 (getDos() == null) ? false : getDos().isArchive();
	}
	public boolean isSystem() {
		return 
				 (getDos() == null) ? false : getDos().isSystem();
	}
	public Set<PosixFilePermission> getPermissions() {
		return posixPermissions;
	}
	
	public boolean isHidden() {
		if( getDos() != null && getDos().isHidden())
			return true ;
		try {
			if( Files.isHidden(mPath))
				return true ;
		} catch (IOException e) {
			FileUtils.mLogger.catching(e);
			// try by name 
		}
		return false ;
	}
	
	public boolean isHiddenName() {
		return FileUtils.isHiddenName( mPath );

	}

	
	public UnifiedFileAttributes.FileType  getFileType() {
		if( isDirectory() )
			return FileType.DIRECTORY ;
		if( isRegularFile() )
			return FileType.FILE ;
		if( isOther() )
			return FileType.OTHER ;
		if( isSymbolicLink())
			return FileType.SYMLINK ;
		return FileType.OTHER ;
	}
	public boolean canRead() {
		return Files.isReadable(mPath);
	}
	public boolean canWrite() {
		return Files.isWritable(mPath);

	}
	public boolean canExecute() {
		return Files.isExecutable(mPath);

	}
	public Path getPath() {
		return mPath ;
	}
	public DosFileAttributes getDos() {
		return dos;
	}
	public void setDos(DosFileAttributes dos) {
		this.dos = dos;
	}
	public BasicFileAttributes getBasic() {
		return basic;
	}
	public void setBasic(BasicFileAttributes basic) {
		this.basic = basic;
	}
	public PosixFileAttributes getPosix() {
		return posix;
	}
	public void setPosix(PosixFileAttributes posix) {
		this.posix = posix;
	}
	public boolean isFlagMatch(  MatchFlag flag )
	{
	    switch( flag ){
	    case DIRECTORIES :
	    	return isDirectory();
	    	
		case EXECUTABLE:
			return canExecute();
		case FILES:
			return isRegularFile();
		case HIDDEN_NAME:
			return isHiddenName();
		case HIDDEN_SYS:
			return isHidden();
		case LINKS:
			return isSymbolicLink();
		case OTHER:
			return isOther();
		case READABLE:
			return canRead();
		case SYSTEM:
			return isSymbolicLink();
		case WRITABLE:
			return canRead();
		default:
			return false ;
	    }
	
	}
	public boolean isAnyFlagMatch( EnumSet<MatchFlag> flags )
	{
		for( MatchFlag flag : flags )
			if( isFlagMatch(flag))
				return true ;
	   return false ;
   }
	   
	   
	   
   }