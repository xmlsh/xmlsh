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
import org.xmlsh.util.FileUtils.UnifiedFileAttributes;
import org.xmlsh.util.FileUtils.UnifiedFileAttributes.FileType;

public class XFile /* implements XSerializble */ {
	private static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger( XFile.class);
	private Path mPath;


	public XFile(Shell shell , XValue xv )
	{
		assert( xv != null);
		if( xv.isXdmNode() ){
			try {
				xv = xv.xpath(shell, "/file/@path/string()");
			} catch (UnexpectedException e) {
				mLogger.debug("Ignoring exception converting xvalue to file",e);
			}
		}
		assert( xv != null );
		mPath = shell.getPath(xv);

	}
	public XFile(Path path) {
		mPath = path ;
	}

	public XFile(String dir, String base) {
		this(resolve(dir, base));
	}
	/* 
	 * Resolves a base against a directory
	 * Similar to URI Resolution, if base is an absolute path
	 * then ignore the directory
	 */
	private static File resolve(String dir, String base) {
		return resolvePath(dir,base).toFile();


	}
	private static Path resolvePath(String dir, String base) {
		Path pdir = Paths.get(dir);
		return pdir.resolve(base);


	}
	public XFile(String dir, String base, String ext) {
		this(resolve(dir, base + ext));
	}

	public XFile(File file) {
		mPath = file.toPath();
	}


	public String getName() {
		mLogger.entry(mPath);
		assert( mPath != null);
		Path pname = mPath.getFileName();
		String fname = null ;
		
		if( pname == null ){
			if( mPath.getNameCount() > 0)
				pname = mPath.getName( mPath.getNameCount() - 1);
			else
				pname = mPath.getRoot();
		}

		assert( pname != null );
		fname = pname.toString();
		assert(! Util.isEmpty(fname) );
		
		String name = FileUtils.toJavaPath(fname);

		return mLogger.exit(name) ;
	}
	public File getFile()
	{
		assert( mPath!= null);
		return mPath.toFile();
	}

	public Path toPath(){
		assert( mPath!= null);

		return mPath;
	}
	public String getPath() {
		try {
			Path path = mPath.toRealPath(LinkOption.NOFOLLOW_LINKS);
 
			if( path != null)
			  return FileUtils.toJavaPath(path);
		} catch (IOException e) {
			mLogger.catching(e);
		}
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

	public void serialize(XMLStreamWriter writer, boolean all, boolean end, Path pwdRelative ) throws  XMLStreamException {

		serialize(writer, all, end, pwdRelative, null);
	}
	
	public 	Set<PosixFilePermission> getPosixFilePermissions(boolean followLinks)
	{
		return FileUtils.getPosixFilePermissions(toPath(), followLinks);
	}
	
	public PosixFileAttributes  getPosixFileAttributes(boolean followLinks) {
		return FileUtils.getPosixFileAttributes(toPath(),followLinks);
	}
	
	public BasicFileAttributes  getBasicFileAttributes(boolean followLinks) {
		return FileUtils.getBasicFileAttributes(toPath(),followLinks);
	}
	
	public DosFileAttributes  getDosFileAttributes(boolean followLinks) {
		return FileUtils.getDosFileAttributes(toPath(),followLinks);
	}
	public UnifiedFileAttributes  getFileAttributes(boolean followLinks) {
		return FileUtils.getUnifiedFileAttributes(toPath(),followLinks);
	}
	
	public void serialize(XMLStreamWriter writer, boolean all, boolean end, Path pwdRelative , Path rootRelative ) throws  XMLStreamException {


		UnifiedFileAttributes attrs = FileUtils.getUnifiedFileAttributes(mPath, false );
		
		writer.writeStartElement(attrs.isDirectory() ? "dir" : "file");
		writer.writeAttribute("name", getName());
		
		
		writer.writeAttribute("path", getPath() );
		if(pwdRelative != null )
			writer.writeAttribute("pwd-relpath", FileUtils.toJavaPath(pwdRelative.relativize(toPath()).toString()));
		if( rootRelative != null )
			writer.writeAttribute("relpath", FileUtils.toJavaPath(rootRelative.relativize(toPath()).toString()));

		if( all ){
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

	public boolean isDirectory() {
		return Files.isDirectory(mPath, LinkOption.NOFOLLOW_LINKS);
	}
	public void serialize(XMLStreamWriter writer, boolean all, boolean end ) throws  XMLStreamException {
		serialize(writer, all ,end, null);
	}

	public String noExtension() {
		String	path = FileUtils.toJavaPath(getPath());
		String  ext = getExt();
		return path.substring(0 , path.length() - ext.length());

	}
	public String getPathName() {
		return FileUtils.toJavaPath(getPath());
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
