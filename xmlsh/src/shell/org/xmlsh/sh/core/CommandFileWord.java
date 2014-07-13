/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.ByteFilterInputStream;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

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
	
	
	private String expandFile( Shell shell , String cmd , SourceLocation loc) throws IOException, CoreException
	{



		XValue 	files = shell.expandToValue( cmd , true,true ,false, loc );
		String file;
		if( files.isAtomic() )
			file = files.toString();
		else 
			throw new InvalidArgumentException("Invalid expansion for redirection");
		
		SerializeOpts sopts = shell.getSerializeOpts();
		InputPort ip = shell.getInputPort(file);
		InputStream is = ip.asInputStream(sopts);
		ByteFilterInputStream filterIn = null ;

		try {

			InputStream commandIn = sopts.isIgncr() ? (filterIn= new ByteFilterInputStream(is,'\r')) : is;
			String s =  Util.readString( commandIn , sopts.getInputTextEncoding());
			s = Util.removeTrailingNewlines(s, shell.getSerializeOpts().isIgncr() );
			return s;

		} finally {
			Util.safeClose(filterIn);
			is.close();
			ip.close();
		}
			
	}
	XdmNode	expandXFile( Shell shell , String xfile, SourceLocation loc ) throws IOException, CoreException{
		XValue 	files = shell.expandToValue( xfile , true,true ,false, loc );
		String file;
		if( files.isAtomic() )
			file = files.toString();
		else 
			throw new InvalidArgumentException("Invalid expansion for redirection");
		
		InputPort ip = shell.getInputPort(file);
		try {

			XdmNode node = ip.asXdmNode( shell.getSerializeOpts());
			return node;
		} catch( Exception e ){
			throw new XMLException("Exception parsing XML document: " + file , e );
		} finally
		{
			ip.close();
		}
	}


	@Override
	public XValue expand(Shell shell,boolean bExpandWild , boolean bExpandWords, boolean bTongs , SourceLocation loc ) throws IOException, CoreException {

		if(mType.equals("$(<")){
			
			String value = expandFile( shell , mFile,loc );
			return expandWords( shell , value , bExpandWords , bTongs );

		} else
		if( mType.equals("$<(<")){
			XdmNode node = expandXFile(shell , mFile,loc);
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
