/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.types;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class XFile /* implements XSerializble */ {
	private static Logger mLogger = Logger.getLogger( XFile.class);
	private File mFile;

	
	public XFile( XValue xv )
	{
		if( ! xv.isAtomic() ){
			try {
				xv = xv.xpath("/file/@path/string()");
			} catch (UnexpectedException e) {
				mLogger.debug("Ingorning exception converting xvalue to file",e);
			}
			
		}
		mFile = new File(xv.toString());
		
	}
	public XFile(String path) {
		this(new File(path));
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
		File fbase = new File(base);
		if( fbase.isAbsolute())
			return fbase;
		else
			return new File(dir, base);
		
		
	}

	public XFile(String dir, String base, String ext) {
		this(resolve(dir, base + ext));
	}

	public XFile(File file) {
		mFile = file;
	}


	public String getName() {
		return Util.toJavaPath(mFile.getName());

	}
	public File getFile()
	{
		return mFile;
	}

	public String getPath() {
		try {
			return Util.toJavaPath(mFile.getCanonicalPath());
		} catch (IOException e) {
			return "";
		}
	}

	public String getDirName() {
		String dir = Util.toJavaPath(mFile.getParent());
		return dir == null ? "." : dir;
	}

	public String getExt() {
		String name = getName();
		int slash = name.lastIndexOf( File.pathSeparatorChar);
		int pos = name.lastIndexOf('.');
		if (pos >= 0 && pos > slash )
			return name.substring(pos);
		else
			return "";

	}

	public String getBaseName() {
		String name = getName();
		int pos = name.lastIndexOf('.');
		if (pos >= 0)
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

	

	public void serialize(XMLStreamWriter writer, boolean all, boolean end ) throws  XMLStreamException {
		
		writer.writeStartElement("file");
		writer.writeAttribute("name", getName());
		writer.writeAttribute("path", getPath());
		if( all ){
			
			
			writer.writeAttribute("length", String.valueOf(mFile.length()));
			
			writer.writeAttribute("type", mFile.isDirectory() ? "dir" : "file");
			writer.writeAttribute("readable", mFile.canRead()? "true" : "false");
			writer.writeAttribute("writable", mFile.canWrite()? "true" : "false");
			writer.writeAttribute("executable", mFile.canExecute() ? "true" : "false");
			writer.writeAttribute("mtime", formatTime(mFile.lastModified()));
			
	
			
		}
		if( end )
			writer.writeEndElement();
	}
	
	// Format time as xs:datetime
	private String formatTime(long lastModified) 
	{
		Date date = new Date(lastModified);
		return Util.formatXSDateTime( date );
		
		
		
		
		
	}
	
	// Filename without any extension

	public String noExtention() {
		String	path = Util.toJavaPath(mFile.getPath());
		String  ext = getExt();
		return path.substring(0 , path.length() - ext.length());
	
	}
	public String getPathName() {
		return Util.toJavaPath(mFile.getPath());
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
// Copyright (C) 2008,2009 , David A. Lee.
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
