/**
 * $Id: xcat.java 388 2010-03-08 12:27:19Z daldei $
 * $Date: 2010-03-08 07:27:19 -0500 (Mon, 08 Mar 2010) $
 *
 */

package org.xmlsh.commands.internal;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class xunzip extends XCommand {
	
	public int run( List<XValue> args )	throws Exception
	{
		


		Options opts = new Options( "f=file:,l=list,d=dest:" ,  SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		boolean bList = opts.hasOpt("l");
		String dest = opts.getOptString("d", ".");
		XValue zipfile = opts.getOptValue("f");
		
		args = opts.getRemainingArgs();
		
		SerializeOpts serializeOpts = getSerializeOpts(opts);

		InputPort iport = (zipfile == null ? getStdin() : getInput(zipfile));
		InputStream is = iport.asInputStream(serializeOpts); 
		
		ZipInputStream zis = new ZipInputStream(is);
		
		try {
		
		int ret = 0;
		if( bList ){
			ret = list(zis,serializeOpts,args);
			
		}
		else
			ret = unzip( zis , getFile(dest) , args );
		
		
		// Central directory may be pesent at the end read past it to avoid a broken pipe
		while( is.read() >= 0 )
			;
		
		zis.close();
		
		return ret;
		
		} finally {
			zis.close();
			is.close();
			iport.close();
		}
		


	}

	private int unzip(ZipInputStream zis, File dest, List<XValue> args) throws IOException {
		
	
		
		ZipEntry entry ;
		while( (entry = zis.getNextEntry()) != null ){
			
			if( matches( entry.getName() , args )){
				File outf = getShell().getFile( dest , entry.getName());
				// printErr(outf.getAbsolutePath());
				if( entry.isDirectory())
					outf.mkdirs();
				else
				{
					// In matching case dir may not exist
					File dir = outf.getParentFile();
					if( ! dir.exists() )
						dir.mkdirs();
					
					
					FileOutputStream fos = new FileOutputStream(outf);
					Util.copyStream(zis, fos);
					fos.close();
					outf.setLastModified(entry.getTime());
					
				}
			
			}
			zis.closeEntry();
			
			
		}

		return 0;
	}

	private boolean matches(String name, List<XValue> args) {
		if( args == null || args.size() == 0)
			return true ; // 0 args matches all
		
		for( XValue v : args )
			if( Util.isEqual(name, v.toString()))
				return true ;
		return false ;
		
		
		
		
	}

	private int list(ZipInputStream zis,SerializeOpts serializeOpts, List<XValue> args) throws IOException, XMLStreamException, InvalidArgumentException, SaxonApiException {
		OutputPort stdout = getStdout();
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
		writer.writeStartDocument();
		
		
		writer.writeStartElement("zip");
	
		
		ZipEntry entry ;
		while( (entry = zis.getNextEntry()) != null ){
			
			if( matches( entry.getName() , args )){

				
				writer.writeStartElement("entry");
				writer.writeAttribute("name", entry.getName());
				if( entry.getComment() != null )
					writer.writeAttribute("comment", entry.getComment());
				writer.writeAttribute("size", String.valueOf(entry.getSize()));
				writer.writeAttribute("compressed_size", String.valueOf(entry.getCompressedSize()));
				writer.writeAttribute("directory", String.valueOf(entry.isDirectory()));
				
	
				
				writer.writeAttribute("time", Util.formatXSDateTime( entry.getTime()));
				writer.writeEndElement();
			}
			zis.closeEntry();
			
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		stdout.release();
		return 0;
	}



}

//
// 
//Copyright (C) 2008-2014    David A. Lee.
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
