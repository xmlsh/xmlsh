/**
 * $Id: xcat.java 388 2010-03-08 12:27:19Z daldei $
 * $Date: 2010-03-08 07:27:19 -0500 (Mon, 08 Mar 2010) $
 *
 */

package org.xmlsh.commands.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xzip extends XCommand {

	
	
	public int run( List<XValue> args )	throws Exception
	{
		


		Options opts = new Options( "f=file:" ,  SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		XValue zipfile = opts.getOptValue("f");
		
		args = opts.getRemainingArgs();
		
		SerializeOpts serializeOpts = getSerializeOpts(opts);

		ZipOutputStream zos = new ZipOutputStream( 
				zipfile == null ?
				getStdout().asOutputStream(serializeOpts) :
				this.getOutputStream( zipfile.toString(), false, serializeOpts )
		);
		
		
		try {
		
			int ret = 0;
			ret = zip( zos ,  args );
			
			zos.finish();
			return ret;
		
		} finally {
			zos.close();
		}
		

		


	}

	
	
	
	
	
	
	
	
	
	private int zip(ZipOutputStream zos, List<XValue> args) throws IOException 
	{
		int ret ;
		for( XValue v : args )
			if( (ret = zip( zos , v.toString() )) != 0 ) 
					return ret ;
		return 0;
		
		
	}


	private int zip(ZipOutputStream zos, String fname) throws IOException 
	{
		int ret ;
		File file = getFile(fname);
		if( file.isDirectory() ){
			String[] files = file.list();
			for( String f : files ){
				if( ( ret = zip(zos, fname + "/" + f )) != 0 )
					return ret ;
				
			}
			return 0;
		}
		
		
		ZipEntry entry = new ZipEntry(fname);
		entry.setTime(file.lastModified());
		zos.putNextEntry(entry);
		
		FileInputStream fis = new FileInputStream( file );
		Util.copyStream(fis, zos);
		fis.close();
		zos.closeEntry();
		
		return 0;
	}





}

//
// 
//Copyright (C) 2008,2009,2010,2011 , David A. Lee.
// 
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
