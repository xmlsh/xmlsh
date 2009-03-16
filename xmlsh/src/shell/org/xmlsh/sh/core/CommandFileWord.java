/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLException;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public class CommandFileWord extends Word {
	private 	String		mType;	// String value
	private		String		mFile;
	public CommandFileWord( String type , String file ){
		mType = type;
		mFile = file;
	}
	
	public void print( PrintWriter out )
	{
		out.print(mType);
		out.print(mFile);
		out.print(")");
	}
	
	
	private String expandFile( Shell shell , String cmd ) throws IOException, CoreException
	{


		XValue 	files = shell.expand( cmd , true,true );
		String file;
		if( files.isAtomic() )
			file = files.toString();
		else 
			throw new InvalidArgumentException("Invalid expansion for redirection");
		InputStream is = shell.getInputStream(file);
		try {
			return Util.readString( is).trim();
		} finally {
			is.close();
		}
			
	}
	XdmNode	expandXFile( Shell shell , String xfile ) throws IOException, CoreException{
		XValue 	files = shell.expand( xfile , true,true );
		String file;
		if( files.isAtomic() )
			file = files.toString();
		else 
			throw new InvalidArgumentException("Invalid expansion for redirection");
		InputStream is = null;
		try {
			DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
			is = shell.getInputStream(file);
			XdmNode node = builder.build(new StreamSource(is));
			return node;
		} catch( Exception e ){
			throw new XMLException("Exception parsing XML document: " + file , e );
		} finally {
			if( is != null )
				is.close();
		}
	}



	public XValue expand(Shell shell,boolean bExpandWild , boolean bExpandWords ) throws IOException, CoreException {

		if(mType.equals("$(<")){
			
			String s = expandFile( shell , mFile );
			return new XValue(s);
		} else
		if( mType.equals("$<(<")){
			XdmNode node = expandXFile(shell , mFile);
			return new XValue(node);
			
		}
		else
			return null;
	
	}



	public boolean isEmpty() {
		return mFile == null ||  mFile.isEmpty();
	}
	
	public String toString()
	{
		return mType + mFile + ")";
	}
}



//
//
//Copyright (C) 2008, David A. Lee.
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
