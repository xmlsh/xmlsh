/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.CommandFactory;
import org.xmlsh.sh.shell.Shell;


public class FileUtils
{

	static volatile Map<java.nio.file.FileStore,Collection<Class<? extends FileAttributeView> > > 
	   sSupportedAttributes  = new HashMap<>();
	
	   

	static Logger mLogger = LogManager.getLogger();
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

	
	public static Set<PosixFilePermission> getPosixFilePermissions(Path path, boolean followLink ) {
		if( supportsAttributeView(path, PosixFileAttributeView.class)){
			try {
				return 
						Files.getPosixFilePermissions(path,
						  LinkOption.NOFOLLOW_LINKS );
			} catch (IOException e) {
				mLogger.catching(e);
			}
		}

		return emulatePosixFilePermissions(path);
		
	}
	
	
	public static <A extends BasicFileAttributes, V extends BasicFileAttributeView> A
	      getFileAttributes( Path path , Class<A> attrClass , Class<V> viewClass , boolean followLinks) {
		
		   if( supportsAttributeView(path, viewClass)){
			   try {
					A attrs = Files.readAttributes(path, attrClass, LinkOption.NOFOLLOW_LINKS);
					if( attrs != null )
						return attrs;
				} catch (IOException e) {
					mLogger.catching(e);
				}
		   }
		   return null;
	}
	
   public static PosixFileAttributes getPosixFileAttributes(Path path, boolean followLinks ) {
	   return getFileAttributes(path,PosixFileAttributes.class,PosixFileAttributeView.class,followLinks);
	}
	
   public static BasicFileAttributes getBasicFileAttributes(Path path, boolean followLinks ) {
	   return getFileAttributes(path,BasicFileAttributes.class,BasicFileAttributeView.class,followLinks);
   }
   
   public static DosFileAttributes getDosFileAttributes(Path path, boolean followLinks ) {
	   return getFileAttributes(path,DosFileAttributes.class,DosFileAttributeView.class,followLinks);
   }

   public static class UnifiedFileAttributes {
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
	  

	   public PosixFileAttributes  posix;
	   public BasicFileAttributes  basic;
	   public DosFileAttributes    dos ;
	   private Set<PosixFilePermission>  posixPermissions;
	   private Path mPath ;
	   
	   
	public UnifiedFileAttributes(Path path, boolean followLinks) {
		mPath = path;
		   posix = getPosixFileAttributes( path , followLinks );
		   dos   = getDosFileAttributes( path , followLinks );
		   if( posix != null ){
			   basic = posix ;
			   posixPermissions = posix.permissions();
		   }
		   else
		   if( dos != null )
			   basic = dos  ;
		   else
			   basic =  getBasicFileAttributes( path , followLinks );
	 
		   if(posixPermissions  == null ){
			   posixPermissions= emulatePosixFilePermissions(path);
		   }
		   
		   
	}
	public UserPrincipal owner() {
		return posix.owner();
	}
	public FileTime lastModifiedTime() {
		return basic.lastModifiedTime();
	}
	public FileTime lastAccessTime() {
		return basic.lastAccessTime();
	}
	public FileTime creationTime() {
		return basic.creationTime();
	}
	public boolean isRegularFile() {
		return basic.isRegularFile();
	}
	public boolean isDirectory() {
		return basic.isDirectory();
	}
	public boolean isSymbolicLink() {
		return basic.isSymbolicLink();
	}
	public boolean isOther() {
		return basic.isOther();
	}
	public long size() {
		return basic.size();
	}
	public Object fileKey() {
		return basic.fileKey();
	}
	public boolean isArchive() {
		return dos.isArchive();
	}
	public boolean isSystem() {
		return dos.isSystem();
	}
	public Set<PosixFilePermission> getPermissions() {
		return posixPermissions;
	}
	
	public boolean isHidden() {

		if( dos != null && dos.isHidden())
			return true ;
		return FileUtils.isHidden( mPath );
		
	}
	
	public FileType  getFileType() {
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
	
	   
	   
	   
   };
   
   public static UnifiedFileAttributes getUnifiedFileAttributes(Path path, boolean followLinks)
   {
	   
	  return  new UnifiedFileAttributes(path, followLinks );
	
	   
   }
   protected static Set<PosixFilePermission> emulatePosixFilePermissions(Path path ) {
	Set<PosixFilePermission> perms = EnumSet
				.noneOf(PosixFilePermission.class);
		if (Files.isReadable(path))
			perms.add(PosixFilePermission.OWNER_READ);
		if (Files.isWritable(path))
			perms.add(PosixFilePermission.OWNER_WRITE);
		if (Files.isExecutable(path))
			perms.add(PosixFilePermission.OWNER_EXECUTE); 
		return perms ;
   }
   
	public static String getSystemTextEncoding() {
		return System.getProperty("file.encoding");
	}
	public static boolean hasDirectory(String name) {
		/* Dont use Paths ... it bombs on bad names
		 Path p = Paths.get(name);
		 return p.getNameCount() > 1 ;
		 */
		return name.contains( File.separator) ||
				( Util.isWindows() && name.contains("/"));
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
	public static String toJavaPath( Path path )
	{
		return toJavaPath( path.toString());
	}


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
	
	public static boolean supportsAttributeView( Path path , Class<? extends FileAttributeView> view )
	{
		try {
			return supportsAttributeView( Files.getFileStore(path) , view );
		} catch (IOException e) {
			mLogger.trace("Catching:",e);
		}
		return false ;
	}
	
	public static boolean supportsAttributeView( File file , Class<? extends FileAttributeView> view ){
		try {
			return supportsAttributeView( Files.getFileStore(file.toPath()) , view );
		} catch (IOException e) {
			mLogger.trace("Catching:",e);
		}
		return false ;

	}

	
	public static boolean    supportsAttributeView( FileStore store , Class<? extends FileAttributeView> view )
	{

		if( store == null )
			return false;
		Collection<Class<? extends FileAttributeView> > set = sSupportedAttributes.get(store);
		if( set == null ){
				set = sSupportedAttributes.get(store);
				if( set == null ){
					set = new ArrayList< Class<? extends FileAttributeView> >();
					if( store.supportsFileAttributeView(FileAttributeView.class))
						set.add(FileAttributeView.class);
					if( store.supportsFileAttributeView(BasicFileAttributeView.class))
						set.add(BasicFileAttributeView.class);
					if( store.supportsFileAttributeView(AclFileAttributeView.class))
						set.add(AclFileAttributeView.class);
					if( store.supportsFileAttributeView(UserDefinedFileAttributeView.class))
						set.add(UserDefinedFileAttributeView.class);
					if( store.supportsFileAttributeView(PosixFileAttributeView.class))
						set.add(PosixFileAttributeView.class);
				}
				synchronized( sSupportedAttributes ){
					sSupportedAttributes.put( store ,  set );
				}
		}

		return set.contains(view);
		
	}

	
	
	/*
	 * Return OS localized extension or ""
	 *   -- tolower if on case insensitive filesystems
	 * foo.bar => .bar
	 * /foobaar/xyz => ""
	 * .foobar  => ""
	 * /foo/bar/.bar =>""
	 */
	
	
	
	public static String getExt(String name) {

		
		mLogger.entry(name);
		name = getPathLikeName( name );
			// Try the hard way.
			int dotpos = name.lastIndexOf('.');
			if( dotpos > 0 && dotpos < name.length() ) // ".xyz" not an extension
				return mLogger.exit(name.substring(dotpos ));
		return mLogger.exit("");
		
	}
	public static boolean isHidden(Path path) {
		try {
			if(  path == null || Files.isHidden(path) )
				return true ;
			// Specical check on windows 
			if( Util.isWindows() ){
				int names =  path.getNameCount() ;
				if( names > 0 &&   path.getName(names-1).toString().startsWith(".") )
					return true ;
				return false ;
			}
		} catch (Throwable e) {
			mLogger.catching(e);;
	
		}
		return false ;
	}
	public static boolean isSystem(Path path) {
		DosFileAttributes view = getDosFileAttributes(path, false);
		if( view == null )
			return false ;
		return view.isSystem();
		
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