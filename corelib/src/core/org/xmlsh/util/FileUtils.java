/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

import static java.nio.file.attribute.PosixFilePermission.*;
import static org.xmlsh.util.Util.enumSetOf;

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.shell.ShellConstants;


public class FileUtils
{

	
	static volatile Map<java.nio.file.FileStore,Collection<Class<? extends FileAttributeView> > > 
	   sSupportedAttributes  = new HashMap<>();
	
	static LinkOption[] _pathFollowLinks = new LinkOption[] { LinkOption.NOFOLLOW_LINKS } ;
	static LinkOption[] _pathNoFollowLinks = new LinkOption[0] ;
	
	public static Set<PosixFilePermission> _allRead = enumSetOf( OWNER_READ , GROUP_READ , OTHERS_READ );
	public static Set<PosixFilePermission> _allWrite = enumSetOf( OWNER_WRITE , GROUP_WRITE , OTHERS_WRITE );
    public static Set<PosixFilePermission> _allExec = enumSetOf( OWNER_EXECUTE , GROUP_EXECUTE , OTHERS_EXECUTE );

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
     BasicFileAttributes basic = getFileAttributes(path,BasicFileAttributes.class,BasicFileAttributeView.class,followLinks);
     mLogger.entry(path,followLinks);
     if( basic == null) { // fake it out -- bug on overlay fs
       mLogger.debug("No basic atribues - simulating with File methods");
        File file = path.toFile();
       basic = new BasicFileAttributes() {
          public FileTime creationTime() { return FileTime.fromMillis(file.lastModified()); }
          public Object  fileKey() { return file.hashCode() ; }
          public  boolean isDirectory() { return file.isDirectory() ; }
          public boolean isOther() { return ! file.isDirectory() && ! file.isFile() ; }
          public boolean isRegularFile() { return file.isFile() ; }
          public boolean isSymbolicLink() { return false ; }
          public  FileTime  lastAccessTime() { return creationTime(); }
          public FileTime  lastModifiedTime() { return creationTime(); }
          public long  size() { return file.length() ; }
      };
    }
      return mLogger.exit(basic) ;
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
	   
	   EnumSet<PosixFilePermission> perms = EnumSet
				.noneOf(PosixFilePermission.class);
		if (Files.isReadable(path))
			perms.addAll(_allRead);
		if (Files.isWritable(path))
			perms.addAll(_allWrite);
		if (Files.isExecutable(path))
			perms.addAll(_allExec); 
		return perms ;
   }
 protected static Set<PosixFilePermission> emulatePosixFilePermissions(DosFileAttributes dos , LinkOption...  followLinks ) {
       
       Set<PosixFilePermission> perms = EnumSet
                .noneOf(PosixFilePermission.class);
        if (!dos.isReadOnly())
            perms.addAll(_allWrite);
        perms.addAll( _allRead );
        return Collections.unmodifiableSet(perms);
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
	    assert( name!= null);
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
	    mLogger.entry(path);
	    if( path == null )
	        return "";
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
		if( ! isFilesystemCaseSensitive() )
		   path = path.toLowerCase();
		
		FileSystem fs = FileSystems.getDefault();
		for( Path root : fs.getRootDirectories() ){
		   String sr = toJavaPath(root.toString());
	       if( ! isFilesystemCaseSensitive() )
	           sr = sr.toLowerCase();
		   
		   if( path.startsWith(sr))
			   return sr.length();
		}
		return 0;
		
	}
	
 
	static FileSystem getFileSystem( Path path ){
	    return FileSystems.getFileSystem(path.toUri());
	}
	/*
	 * Special function that would return basename without extension if this is path-like
	 * but otherwise still does something useful - dont use if you know the string is really a path
	 */
	public static String basePathLikeName(String path)
	{
		path = getPathLikeName(path);

		int startpos = 0 ;
		int dotpos = path.indexOf(ShellConstants.kDOT_CHAR, startpos);
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

    public static  <V extends FileAttributeView> V getAttributeView( Path path , Class<V> view ) throws IOException
    {
        return Files.getFileAttributeView(path, view );
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
			int dotpos = name.lastIndexOf(ShellConstants.kDOT_CHAR);
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
	
	/*
	 * Guess the file type for purposes of script or cmd execution
	 * if bScripty then any texty like thing will do
	 */
	
	public static boolean isXScript( Path path , boolean bScripty ,  String encoding ){
		String ext = getExt(path.toString());
		if( Util.isEqual( ext,  ShellConstants.XSH_EXTENSION ) )
		    return true ;
		if( Util.isBlank(ext) ){
		    String line = getTextFileMagic( path , encoding );
		    if( line  != null ){
		        if( bScripty )
		            return true ;
		        if( line.startsWith("#!") ){
		          line=basePathLikeName(line.substring(2));
		          if( Util.isEqual(line,"xmlsh",true) )
		            return true ;
		        }
		    }
		    return false ;
		} 
	    return false ;
	    
	}
	
	
	public static String getTextFileMagic(Path path ,String encoding ){
	    
		mLogger.entry(path,encoding);
		try ( InputStream is = Files.newInputStream(path, StandardOpenOption.READ ) ){
		    byte data[] = new byte[1024];
		    long len = is.read(data);
		    if( len <= 0 )
		    	return null ;
		java.nio.ByteBuffer bb = ByteBuffer.wrap(data,0,(int)len);

		CharsetDecoder decoder=  
				Charset.forName(encoding).newDecoder();  
		
		CharBuffer ret = decoder.decode(bb);
		if( ret.length() <= 0)
			return null  ;
        // Look for some alpaha or reserved word

		int good = 0;
		int max = Math.min( 100 ,  ret.length() );
		int p = 0;
		for( char c : ret.array() ){
		    p++;
		    if( good > max )
				return mLogger.exit("text");
			
			
			if( !Character.isDefined(c) )
				return mLogger.exit(null) ;

			switch( c ){
			case '\0' :
				return mLogger.exit(null) ;
			case '\n' :
			case '\r' :
			    if( good > 2 )
			      return ret.subSequence(0, p).toString();
			    		    
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
			case '=' :
			case ';' :
			
			    good++;
			    continue;
			}
			switch( Character.getType(c)){
			case Character.CONNECTOR_PUNCTUATION :
			case Character.CURRENCY_SYMBOL : 
			case Character. DIRECTIONALITY_PARAGRAPH_SEPARATOR :
			case Character.LINE_SEPARATOR :
			case Character.INITIAL_QUOTE_PUNCTUATION :
			case Character.ENCLOSING_MARK :
			case Character.OTHER_PUNCTUATION :
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
		    		return good > 20 ? ret.subSequence(0, p).toString()  : null ;
			good = 0;
		    }				
		}
		
		  if( good > 0 )
	            return mLogger.exit("text");
	}
  
	
    
	catch (Exception e) {

		return mLogger.exit(null) ;
    }
		return mLogger.exit(null ) ;
	}
	   public static void changeFilePermissions(Path path,
	            Set<PosixFilePermission> change , LinkOption... links ) 
	   {
	       changeFilePermissions( path ,  getUnifiedFileAttributes(path, links) , change , links );
	   }

    public static void changeFilePermissions(Path path,
            UnifiedFileAttributes orig,
            Set<PosixFilePermission> change ,
            LinkOption... links) 
 
    {
        mLogger.entry(path,change);
    
        if( supportsAttributeView(path, PosixFileAttributeView.class)){
            mLogger.debug("using PosixFileAttributeView" );
            try {
                Files.setPosixFilePermissions(path, change);
            } catch (IOException e) {
                mLogger.catching(e);
            }
        }
        
        if( supportsAttributeView(path, DosFileAttributeView.class)){
            mLogger.debug("supports DosFileAttributeView" );
            try {
                DosFileAttributeView dosView = getAttributeView( path , DosFileAttributeView.class );
                if( dosView != null ){
                    mLogger.debug("using DosFileAttributeView");
                    DosFileAttributes origView = orig.getDos();
                    boolean anyWrite = Util.setContainsAny( change , _allWrite );
                    boolean origRO = origView.isReadOnly();
                    if( !anyWrite != origRO )
                        dosView.setReadOnly(!anyWrite);
                    
                    
                }
                else {
                    mLogger.error("using File" );
                    boolean anyWrite = Util.setContainsAny( change , _allWrite );
                    boolean anyRead = Util.setContainsAny( change , _allRead );

                    
                    File f = path.toFile();  
                    if( !anyWrite && ! orig.isReadOnly()  )
                      f.setReadOnly( );
                    
                    if( anyWrite != orig.canWrite() )
                      f.setWritable( anyWrite );
                    
                    if( anyRead != f.canRead())
                        f.setReadable(anyRead);
                    
                    boolean anyExec = Util.setContainsAny( change , _allExec);

                    if( anyExec != orig.canExecute() )
                        f.setExecutable( anyExec );
                }
            } catch (IOException e) {
                mLogger.catching(e);
            }
        }
        return ;
    }
    public static Path resolveLink(Path p) {
        if( p == null ) return null ;
        p = p.normalize();
        try {
          p=p.toRealPath(_pathFollowLinks);
          if( Files.isSymbolicLink(p ))
               return Files.readSymbolicLink(p );
            }  catch( IOException e){
                mLogger.catching(e);
            }
    return p;
    }
    public static Path getParent(Path p) {
        if( p != null ){
          File pf = p.toFile();
          if( pf != null ){
              pf = pf.getParentFile();
              if( pf != null )
                  return pf.toPath();
          }

      }
        return null;
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
