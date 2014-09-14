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

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.EvalFlag;
import org.xmlsh.core.VariableOutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.ByteFilterOutputStream;
import org.xmlsh.util.NullInputStream;
import org.xmlsh.util.Util;

/*
 * A Value that evaulates to a "cmd_word" which is either a simple string,
 * or a subprocess expression 
 * 
 */
public class CommandNameWord extends Word {
	String		mType;	// $( $(< $<( $<(<  `
	CommandExpr		mCommand;
static Logger mLogger = LogManager.getLogger();
	public CommandNameWord( Token ttype , CommandExpr c){
		super(ttype);
		mType = ttype.toString();
		mCommand =  c;
	}
	
	@Override
	public void print( PrintWriter out )
	{
		out.print( mType );
		mCommand.print(out,false);
		out.print(")");
	}


	private String expandSubproc(Shell parentShell , CommandExpr c ) throws CoreException, IOException
	{

		
		mLogger.entry(parentShell, c);
		
		try ( Shell shell = parentShell.clone() ;
				ByteArrayOutputStream out  = new ByteArrayOutputStream()  ){

			OutputStream commandOut = shell.getSerializeOpts().isIgncr() ? 	new ByteFilterOutputStream( out , '\r' ) : out  ;
			shell.getEnv().setStdout( commandOut  );
			shell.getEnv().setStdin( new NullInputStream() );
			int ret = shell.exec(c);
			parentShell.setStatus(ret);
			
			commandOut.flush();
			out.flush();
			String s = out.toString( shell.getSerializeOpts().getInput_text_encoding());
			
			// remove trailing newlines
			s = Util.removeTrailingNewlines(s, shell.getSerializeOpts().isIgncr() );
			return mLogger.exit(s);

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



	private XValue parseXCmd(Shell parentShell , CommandExpr cmd) throws IOException, CoreException
	{

		
		mLogger.entry(parentShell, cmd);

		XVariable var = XVariable.anonymousInstance(TypeFamily.XDM);

		try ( VariableOutputPort port = new VariableOutputPort( var )	 ){

			try ( Shell shell = parentShell.clone() ){
	
				shell.getEnv().setStdout( port );
				shell.getEnv().setStdin( new NullInputStream() );
				int ret= shell.exec(cmd);
				parentShell.setStatus(ret);
	
			} 

			XValue value = var.getValue();
			port.flush();
			/*
			 * If port was written to as a text stream then need to reparse it as a document
			 */
			if( value != null && port.isAsText() ){
				String sDoc = value.toString();
	
	
				Source source = new StreamSource( new StringReader(sDoc));
				DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
				XdmNode node;
				try {
					node = builder.build(source);
				} catch (SaxonApiException e) {
					throw new CoreException("Exception parsing as XML Document",e);
				}
				return mLogger.exit(XValue.newXValue(node)) ;
			}
			return mLogger.exit(value );
		}



	}



	@Override
	public boolean isEmpty() {
		return mType == null || mCommand == null ;
	}

	@Override
	public String toString()
	{
		return mType +  mCommand.toString(false) + ")";
	}

	@Override
	public String getSimpleName()
	{
		return  mCommand.getName();
	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws CoreException, IOException 
	{

		assert( result != null );
		if( mType.equals("$(") || mType.equals("`") ){
			// http://www.gnu.org/software/bash/manual/bashref.html#Command-Substitution
			/*
			 * Bash performs the expansion by executing command and replacing the command substitution with the standard output of the command, with any trailing newlines deleted. Embedded newlines are not deleted, but they may be removed during word splitting. 
			 * The command substitution $(cat file) can be replaced by the equivalent but faster $(< file).
			 */

			String 	svalue = expandSubproc( shell, mCommand);
			XValue value = EvalUtils.splitStringToValue(shell, svalue, evalEnv( env ));
			result =  
					EvalUtils.expandValueToResult(shell, value , env.withFlagOff(EvalFlag.EXPAND_VAR), loc, result) ;


		} else 
			if( mType.equals("$<(")){

				XValue value = parseXCmd( shell , mCommand );
				result.add( value , true );

			}
			else 
				throw new CoreException("Unexpected CommandWord: " + mCommand );

		return result ;
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
