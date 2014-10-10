/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.Logger;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellThread;
import org.xmlsh.util.UnifiedFileAttributes.FileType;

public class XFile /* implements XSerializble */ {
	private static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger( XFile.class);
	private Path mPath;
	private Path mCurdir;		// Current directory at time of creation
    private UnifiedFileAttributes mAttrs;

	public XFile(Shell shell , XValue xv )
	{
		assert( xv != null);
		if( xv.isXdmNode() ){
			try {
				xv = xv.xpath(shell, "/(file|dir)/@path/string()");
			} catch (UnexpectedException e) {
				mLogger.debug("Ignoring exception converting xvalue to file",e);
			}
		}
		assert( xv != null );
		mCurdir = Shell.getCurPath().normalize();
		Path path = Paths.get( xv.toString() );
		mPath = path;

	}

	public XFile( Path dir , Path base ,UnifiedFileAttributes attrs ){
		this(dir,base);
		mAttrs = attrs;
	}
	public XFile( Path dir , Path base  ){
		mPath =  base.resolve(dir);
 	    mCurdir = Shell.getCurPath();
		
	}
	public XFile(Path path) {
		mPath = path ;
		mCurdir = Shell.getCurPath() ;
	}
	public XFile(Path path, UnifiedFileAttributes attrs) {
		this( path );
		mAttrs = attrs ;
	
	}
	public XFile(String dir, String base) {
		this( Paths.get(dir,base) );
	}
	/* 
	 * Resolves a base against a directory
	 * Similar to URI Resolution, if base is an absolute path
	 * then ignore the directory
	 */
	private static Path resolve(String dir, String base) {
		return resolvePath( Paths.get(dir) , Paths.get(base) );
	}
	
	
	private static Path resolvePath(Path pdir,  Path base) {
		return pdir.resolve(base);


	}
	public XFile(String dir, String base, String ext) {
		this(Paths.get(dir), Paths.get(base + ext));
	}

	public XFile(File file) {
		this( file.toPath());
	}


	// Get the simple name 
	public String getName() {
		mLogger.entry(mPath);
		assert( mPath != null);
		String name = mPath.getFileName().toString();
		/*
		if( mBasedir != null )
			name =  mBasedir.relativize(mPath).toString();
		if( name == null && mCurdir != null )
			return mCurdir.relativize( mPath).toString();
		*/
		
		return mLogger.exit(name) ;
	}
	public File getFile()
	{
		assert( mPath!= null);
		return mPath.toFile();
	}

	public Path toPath(){
		assert( mPath!= null);

		return mPath.normalize();
	}
	
	// Get the full path name but dont try to turn into a real file 
	public String getPath() {
			Path path = toPath();
			if( path != null)
			  return FileUtils.toJavaPath(path);
			else
				return "";

	}

	public Path getRelpath(Path root) {
		return root.relativize(toPath());
	}

	public String getDirName() {
		String dir = FileUtils.toJavaPath(mPath.getParent());
		return Util.isEmpty(dir) ? "." : dir;
	}

	public String getExt() {
		return FileUtils.getExt(getName());

	}

	public String getBaseName() {
		String name = getName();
		int pos = name.lastIndexOf('.');
		if (pos > 0) // .x  doessnt count
			return name.substring(0, pos);
		else
			return name;
	}

	public String getBaseName(String ext) {
		String name = getName();
		if (name.endsWith(ext))
			return name.substring(0, name.length() - ext.length());
		else
			return name;
	}

	
	public 	Set<PosixFilePermission> getPosixFilePermissions(boolean followLinks)
	{
		return getFileAttributes(followLinks).getPermissions();
	}
	
	public PosixFileAttributes  getPosixFileAttributes(boolean followLinks) {
		return getFileAttributes(followLinks).getPosix();
	}
	
	public BasicFileAttributes  getBasicFileAttributes(boolean followLinks) {
		return getFileAttributes(followLinks).getBasic();
	}
	
	public DosFileAttributes  getDosFileAttributes(boolean followLinks) {
		return getFileAttributes(followLinks).getDos();
	}
	public UnifiedFileAttributes  getFileAttributes(boolean followLinks) {
		if( mAttrs == null )
		  mAttrs = FileUtils.getUnifiedFileAttributes(toPath(),followLinks);
		return mAttrs;
	}
	
	public void serialize(XMLStreamWriter writer, boolean all, boolean end, boolean pathrel ) throws  XMLStreamException {


		UnifiedFileAttributes attrs = FileUtils.getUnifiedFileAttributes(mPath, false );
		
		writer.writeStartElement(attrs.isDirectory() ? "dir" : "file");
		writer.writeAttribute("name", getName());
		
		writer.writeAttribute("path", pathrel ? getPwdRelativeName() : getPath()  );


		if( all ){
			writer.writeAttribute("abspath",  getPath()  );

			if(mCurdir != null )
				writer.writeAttribute("relpath",getPwdRelativeName() );
			writer.writeAttribute("length", String.valueOf(attrs.size()));
			
			FileType type = attrs.getFileType();
			
			
			writer.writeAttribute("type", type.toString() );
			writer.writeAttribute("readable", attrs.canRead() ? "true" : "false");
			writer.writeAttribute("writable", attrs.canWrite()? "true" : "false");
			writer.writeAttribute("executable", attrs.canExecute() ? "true" : "false");
			writer.writeAttribute("mtime", Util.formatXSDateTime(attrs.lastModifiedTime().toMillis()));

		}
		if( end )
			writer.writeEndElement();
	}
	public String getPwdRelativeName() {
		return FileUtils.toJavaPath(mCurdir.relativize(toPath()).toString());
	}

	public boolean isDirectory() {
		return Files.isDirectory(mPath, LinkOption.NOFOLLOW_LINKS);
	}

	public String noExtension() {
		String	path = FileUtils.toJavaPath( mPath.toString() );
		String  ext = getExt();
		return path.substring(0 , path.length() - ext.length());

	}
	public String getPathName() {
		return FileUtils.toJavaPath(mPath.toString());
	}
	

	/*
	 * 
	 * public void serialize( XMLStreamWriter writer ) throws XMLStreamException {
	 * writer.writeStartElement("file"); writer.writeAttribute("name", name);
	 * writer.writeAttribute("path", path); writer.writeEndElement();
	 *  }
	 */

}
//
//
// Copyright (C) 2008-2014    David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
