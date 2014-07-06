/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.ByteFilterOutputStream;
import org.xmlsh.util.MutableInteger;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.Util;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public class CommandWord extends Word {
	String		mType;	// $( $(< $<( $<(<  `
	Command		mCommand;
	
	public CommandWord( String type , Command c){
		mType = type;
		mCommand =  c;
	}
	
	public void print( PrintWriter out )
	{
		out.print( mType );
		mCommand.print(out,false);
		out.print(")");
	}
	

	private String expandSubproc(Shell shell , Command c , MutableInteger retValue ) throws CoreException, IOException
	{
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteFilterOutputStream filterOut = null ;
		shell = shell.clone();
		try {
			
			OutputStream commandOut = shell.getSerializeOpts().isIgncr() ? 
					 filterOut = new ByteFilterOutputStream( out , '\r' ) : out;
			shell.getEnv().setStdout( commandOut  );
			shell.getEnv().setStdin( new NullInputStream() );
			int ret = shell.exec(c);
			if( retValue != null )
					retValue.setValue(ret);
			

			commandOut.flush();
			out.flush();
			String s = out.toString( shell.getSerializeOpts().getInput_text_encoding());

			// remove trailing newlines
			s = Util.removeTrailingNewlines(s, shell.getSerializeOpts().isIgncr() );
			return s;
			
		} 
		finally {

			Util.safeClose(filterOut);
			shell.close();

			
		}

		
	}


	

	/*
	 * Parse an XValue subprocess expression like
	 *     $<( command )
	 *     
	 * Create a temporary output variable and use VariableOutputPort
	 * run the command to the output then extract the value from the variable
	 * 
	 */
	
	

	private XdmValue parseXCmd(Shell shell , Command cmd, MutableInteger retValue ) throws IOException, CoreException
	{

		
		XVariable var = new XVariable("__temp",null);
		VariableOutputPort port = new VariableOutputPort( var );
		
		

		shell = shell.clone();
		try {
		
			shell.getEnv().setStdout( port );
			shell.getEnv().setStdin( new NullInputStream() );
			int ret= shell.exec(cmd);
			if( retValue != null ){
				
				
			
				retValue.setValue(ret);
			}
		
			
		} 
		finally {
			shell.close();

			
		}
		
		port.close();
		
	
		
		
		XValue value = var.getValue();
		/*
		 * If port was written to as a text stream then need to reparse it as a document
		 */
		if( value != null && port.isAsText() ){
			String sDoc = value.toString();
			
			
			DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
			Source source = new StreamSource( new StringReader(sDoc));
			XdmNode node;
			try {
				node = builder.build(source);
			} catch (SaxonApiException e) {
				throw new CoreException("Exception parsing as XML Document",e);
			}
			return node ;
			
			
			
			
		}
		
		
		return value == null ? null : value.asXdmValue();
		
		
		
	}
		
	@Override
	public XValue expand(Shell shell,boolean bExpandWild , boolean bExpandWords, boolean bTongs , MutableInteger retValue, SourceLocation loc ) throws IOException, CoreException {
		
		
		if( mType.equals("$(") || mType.equals("`") ){
			// http://www.gnu.org/software/bash/manual/bashref.html#Command-Substitution
			/*
			 * Bash performs the expansion by executing command and replacing the command substitution with the standard output of the command, with any trailing newlines deleted. Embedded newlines are not deleted, but they may be removed during word splitting. 
			 * The command substitution $(cat file) can be replaced by the equivalent but faster $(< file).
			 */
			
			String 	value = expandSubproc( shell , mCommand, retValue);
			// Trailing lines are already removed
			
 
			return expandWords( shell , value , bExpandWords , bTongs );
			
			
			
		} else 
		if( mType.equals("$<(")){
			
			XdmValue v = parseXCmd( shell , mCommand , retValue );
			return new XValue(v);
		}

		else 
			return null;
		
	}

	
	public boolean isEmpty() {
		return mType == null || mCommand == null ;
	}
	
	public String toString()
	{
		return mType +  mCommand.toString(false) + ")";
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
