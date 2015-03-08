package org.xmlsh.util;

import static java.nio.file.attribute.PosixFilePermission.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


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

	static Logger mLogger = LogManager.getLogger();
	private PosixFileAttributes  posix;
	private BasicFileAttributes  basic;
	private DosFileAttributes    dos ;
	private Set<PosixFilePermission>  posixPermissions;
	private Path mPath ;
	private boolean bInit = false ;
	private boolean bExists = false ;

	private LinkOption[] mLinkOpts;

	public UnifiedFileAttributes(Path path, LinkOption...  followLinks) {
		this( path , null , followLinks );


	}
	public UnifiedFileAttributes(Path path, BasicFileAttributes attrs, LinkOption...  followLinks) {
		mLogger.entry(path, attrs, followLinks);
		mPath = path;

		mLinkOpts = followLinks;
		setBasic(attrs);
		mLogger.exit( );

	}
	

	private void init()
	{

		try {
			if( bInit )
				return ;
			// No basic attrs passed - check existance
			if( basic == null ){
				// Check if file exists first
				if( ! Files.exists(mPath, mLinkOpts))
					return ;
				basic = FileUtils.getBasicFileAttributes(mPath, mLinkOpts);
				if( basic == null )
					return ;
				
			}
			bExists = true ;

			this.posix = FileUtils.getPosixFileAttributes( mPath , mLinkOpts );
			this.dos = FileUtils.getDosFileAttributes( mPath , mLinkOpts );
			if( posix != null  ){
				if( basic == null )
					basic = posix ;
				posixPermissions =posix.permissions();
			}
	
			if( basic == null ){
				if( dos != null )
					basic  = dos  ;
				else
					basic = FileUtils.getBasicFileAttributes( mPath , mLinkOpts );
			}
			
			if(posixPermissions  == null && dos != null ){
			    
	             posixPermissions= FileUtils.emulatePosixFilePermissions(dos , mLinkOpts );
				
			}
			if( posixPermissions == null )
			   posixPermissions= FileUtils.emulatePosixFilePermissions(mPath, mLinkOpts );
		} finally {
			bInit = true ;
		}
		
		
	}
	public boolean hasBasic() {
		if( ! bInit ) init() ;
		
		return getBasic() != null ;
	}
	public boolean hasPosix() {
		if( ! bInit ) init() ;

		return getPosix() != null ;
	}
	public boolean hasDos() {
		if( ! bInit ) init() ;
		return getDos() != null ;
	}
	public boolean hasAny() {
		if( ! bInit ) init() ;
		if( ! bExists ) return false ;

		return hasBasic() || hasDos() || hasPosix() ;
	}
	public UserPrincipal owner() {
		if( ! bInit ) init() ;

		return getPosix().owner();
	}
	public FileTime lastModifiedTime() {
		if( ! bInit ) init() ;

		return getBasic().lastModifiedTime();
	}
	public FileTime lastAccessTime() {
		if( ! bInit ) init() ;

		return getBasic().lastAccessTime();
	}
	public FileTime creationTime() {
		if( ! bInit ) init() ;
		if( ! bExists ) return null; ;

		return getBasic().creationTime();
	}
	public boolean isRegularFile() {
		if( ! bInit ) init() ;
		if( ! bExists ) return false  ;

		return getBasic().isRegularFile();
	}
	public boolean isDirectory() {
		if( ! bInit ) init() ;
		if( ! bExists ) return false  ;


		return getBasic().isDirectory();
	}
	public boolean isSymbolicLink() {
		if( ! bInit ) init() ;
		if( ! bExists ) return false  ;

		return getBasic().isSymbolicLink();
	}
	public boolean isOther() {
		if( ! bInit ) init() ;
		if( ! bExists ) return true  ;

		return getBasic().isOther();
	}
	public long size() {
		if( ! bInit ) init() ;
		if( ! bExists ) return -1;

		return getBasic().size();
	}
	public Object fileKey() {
		if( ! bInit ) init() ;
		if( ! bExists ) return null  ;

		return getBasic().fileKey();
	}
	public boolean isArchive() { 
		if( ! bInit ) init() ;
		if( ! bExists ) return false ;

		return 
				(getDos() == null) ? false : getDos().isArchive();
	}
	public boolean isSystem() {
		if( ! bInit ) init() ;
		if( ! bExists ) return false ;

		return 
				(getDos() == null) ? false : getDos().isSystem();
	}
   public boolean isReadOnly() {
        if( ! bInit ) init() ;
        if( ! bExists ) return false ;

        if( hasDos()  )
            return getDos().isReadOnly();
        if( hasPosix()){
            Set<PosixFilePermission> perms = getPosix().permissions();
            return !( perms.contains(OWNER_WRITE ) ||
                    perms.contains(GROUP_WRITE ) ||
                    perms.contains(OTHERS_WRITE ) );
            
        }

        return false ;
    }
	   
	public Set<PosixFilePermission> getPermissions() {
		if( ! bInit ) init() ;
		return posixPermissions;
	}

	public boolean isHidden() {
		if( ! bInit ) init() ;
		if( ! bExists ) return false ;

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
		if( ! bInit ) init() ;
		if( ! bExists ) return FileType.OTHER;

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
		if( ! bInit ) init() ;
		return dos;
	}
	private void setDos(DosFileAttributes dos) {
		this.dos = dos;
	}
	public BasicFileAttributes getBasic() {
		if( ! bInit ) init() ;
		return basic;
	}
	private void setBasic(BasicFileAttributes basic) {
		this.basic = basic;
	}
	public PosixFileAttributes getPosix() {
		if( ! bInit ) init() ;
		return posix;
	}
	private void setPosix(PosixFileAttributes posix) {
		this.posix = posix;
	}
	public boolean isFlagMatch(  MatchFlag flag )
	{
		if( ! bInit ) init() ;

		if( ! bExists ) return false ;
		
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
          mLogger.entry( flags );
		if( ! bInit ) init() ;
		if( ! bExists ) 
			return mLogger.exit(false );

		for( MatchFlag flag : flags )
			if( isFlagMatch(flag))
				return mLogger.exit(true );
		return mLogger.exit(false );
	}



}
