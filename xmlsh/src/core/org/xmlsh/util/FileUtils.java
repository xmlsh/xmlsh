/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.XValue;
import org.xmlsh.posix.commands.ls.ListVisitor;
import org.xmlsh.sh.module.CommandFactory;
import org.xmlsh.sh.shell.Shell;


public class FileUtils
{

	
	static volatile Map<java.nio.file.FileStore,Collection<Class<? extends FileAttributeView> > > 
	   sSupportedAttributes  = new HashMap<>();
	
	static LinkOption[] _pathFollowLinks = new LinkOption[] { LinkOption.NOFOLLOW_LINKS } ;
	static LinkOption[] _pathNoFollowLinks = new LinkOption[0] ;
	

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

	/*
	 * Compare paths strictly by name not pathwise
	 * Intended only for simple names not full paths
	 */
	public static Comparator<Path> alphaPathComparator() {
		return new Comparator<Path>(){
	
			@Override
			public int compare(Path o1, Path o2) {
				// Default use Path compareT
				if( o1 == o2 )
					return 0;
				return  o1.getFileName().toString().compareTo(o2.getFileName().toString());
				
			}
		
		};
	}
	public static Set<PosixFilePermission> getPosixFilePermissions(Path path, LinkOption followLink ) {
		if( supportsAttributeView(path, PosixFileAttributeView.class)){
			try {
				return 
						Files.getPosixFilePermissions(path, (followLink));
			} catch (IOException e) {
				mLogger.catching(e);
			}
		}

		return emulatePosixFilePermissions(path,followLink);
		
	}
	
	
	public static <A extends BasicFileAttributes, V extends BasicFileAttributeView> A
	      getFileAttributes( Path path , Class<A> attrClass , Class<V> viewClass , LinkOption... followLinks) {
		
		// Returns null instead of exception 
		   if( supportsAttributeView(path, viewClass)){
			   try {
					A attrs = Files.readAttributes(path, attrClass, followLinks);
					if( attrs != null )
						return attrs;
				} catch (IOException e) {
					mLogger.catching(e);
				}
		   }
		   return null;
	}
	
   public static LinkOption[] pathLinkOptions(boolean followLinks) {

	   if( followLinks )
		   return _pathFollowLinks ;
	   else
		   return _pathNoFollowLinks ;
	   
   
   }
    public static PosixFileAttributes getPosixFileAttributes(Path path, LinkOption... followLinks ) {
	   return getFileAttributes(path,PosixFileAttributes.class,PosixFileAttributeView.class,followLinks);
	}
	
   public static BasicFileAttributes getBasicFileAttributes(Path path, LinkOption... followLinks ) {
	   
	   
	   return getFileAttributes(path,BasicFileAttributes.class,BasicFileAttributeView.class,followLinks);
   }
   
   public static DosFileAttributes getDosFileAttributes(Path path, LinkOption... followLinks ) {
	   DosFileAttributes dos =  getFileAttributes(path,DosFileAttributes.class,DosFileAttributeView.class,followLinks);
	   return dos ;
   
   }

   public static UnifiedFileAttributes getUnifiedFileAttributes(Path path, LinkOption...  followLinks)
   {
	  return  new UnifiedFileAttributes(path, followLinks );
	   
   }
   
	public static UnifiedFileAttributes getUnifiedFileAttributes(Path path,
			BasicFileAttributes attrs, LinkOption followLinks) {
		  return  new UnifiedFileAttributes(path, attrs , followLinks );

	}
   protected static Set<PosixFilePermission> emulatePosixFilePermissions(Path path, LinkOption...  followLinks ) {
	   
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
	// Assumes java path format  and dont try to convert path to a NIO Path
	public static int rootPathLength(String path)
	{

		if( Util.isBlank(path))
			return 0;
		path = path.toLowerCase();
		
		FileSystem fs = FileSystems.getDefault();
		for( Path root : fs.getRootDirectories() ){
		   String sr = root.toString().toLowerCase(); 
		   
		   if( path.startsWith(sr))
			   return sr.length();
		}
		return 0;
		
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
		Path path = asValidPath(file);
		if( path == null )
			return false ;
		try {
			return supportsAttributeView( Files.getFileStore(path) , view );
		} catch (IOException e) {
			mLogger.trace("Catching:",e);
		}
		return false ;

	}
	
	public static Path asValidPath( File file ){
		if( file == null )
			return null ;
		try {
		  return file.toPath();
		} catch(java.nio.file.InvalidPathException e ){
			mLogger.trace("Invalid path: " , e );
			return null ;
		}
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
					if( store.supportsFileAttributeView(DosFileAttributeView.class))
						set.add(DosFileAttributeView.class);
					
					
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
	/* IsHidden by name only - do Not check file attributes */
	
	public static boolean isHiddenName(Path path) {
			if(  path == null  )
				return true ;
			return path.getFileName().toString().startsWith(".");
	}
	
	public static boolean isSystem(Path path) {
		DosFileAttributes view = getDosFileAttributes(path);
		if( view == null )
			return false ;
		return view.isSystem();
		
	}
	
	/*
	 * Our own version of a FileTreeWalker that is sortable and doesnt follow links
	 */
	public static <V extends IPathTreeVisitor>  void walkPathTree( Path start , boolean recursive , V visitor, PathMatchOptions options ) throws IOException {
		(new PathTreeWalker( start , recursive , options )).walk(visitor);
		
	}
	public static boolean isFilesystemCaseSensitive() {

	    boolean bIsWindows = Util.isWindows();
	    boolean caseSensitive = !bIsWindows;
	    return caseSensitive ;
	}
	
	public static boolean isTextFile( Path path , String encoding ){
		
		mLogger.entry(path,encoding);
		try ( InputStream is = Files.newInputStream(path, StandardOpenOption.READ ) ){
		    byte data[] = new byte[1024];
		    long len = is.read(data);
		    if( len <= 0 )
		    	return false ;
		java.nio.ByteBuffer bb = ByteBuffer.wrap(data);

		CharsetDecoder decoder=  
				Charset.forName(encoding).newDecoder();  
		
		CharBuffer ret = decoder.decode(bb);
		if( ret.length() <= 0)
			return false ;
        // Look for some alpaha or reserved word

		int good = 0;
		for( char c : ret.array() ){
			if( good > 10)
				return mLogger.exit(true) ;
			
			
			if( !Character.isDefined(c) )
				return mLogger.exit(false) ;

			switch( c ){
			case '\0' :
				return mLogger.exit(false) ;
			case '\n' :
			case '\r' :
			case '\t' :
			case '\b' :
			case '\f' :
			case '#' :
			case '!' :
			case '-' :
			case '_' :
			case '(' :
			case ')' :
			case '{' :
			case '}' :
			case '|' :
			case '"' :
			case '\'':
			case '[' :
			case ']' :
			    good++;
			    continue;
			}
			
			if( Character.isJavaIdentifierPart(c)||
			    Character.isJavaIdentifierStart(c)||
				Character.isLetterOrDigit(c) ||
			    Character.isUnicodeIdentifierStart(c) ||
			    Character.isUnicodeIdentifierPart(c)
			    )
				good++;
			else
		    if( ! Character.isWhitespace(c)){
		    	if( Character.isISOControl(c) )
		    		return false ;
				good = 0;
		    }				
		}
		
		
	}
 
    
	catch (Exception e) {

		return mLogger.exit(false) ;
    }
		return mLogger.exit(true) ;
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