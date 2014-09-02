/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.grammar.Token;
import org.xmlsh.sh.shell.ParseResult;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.ByteFilterInputStream;
import org.xmlsh.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/*
 * CommandFileWord is a SubProc syntax that comes from a file 
 * $(<file)  or $<(<file) 
 */
public class CommandFileWord extends Word {
	private 	String		mType;	// String value
	private		Word		mFile;
	private static final EvalEnv mEnv = EvalEnv.fileInstance();

	public CommandFileWord( Token t,  Word file ){
		super(t);
		mType = t.toString();
		mFile = file;
	}

	@Override
	public void print( PrintWriter out )
	{
		out.print(mType);
		mFile.print(out);
		out.print(")");
	}


	private String expandFile( Shell shell , Word cmd , SourceLocation loc) throws IOException, CoreException
	{


		XValue 	files = cmd.expand(shell, mEnv, loc);
		String file;
		if( files.isAtomic() )
			file = files.toString();
		else 
			throw new InvalidArgumentException("Invalid expansion for redirection");

		SerializeOpts sopts = shell.getSerializeOpts();
		try (
				InputPort ip = shell.newInputPort(file);
				InputStream is = ip.asInputStream(sopts);
				){

			ByteFilterInputStream filterIn = null ;

			InputStream commandIn = sopts.isIgncr() ? (filterIn= new ByteFilterInputStream(is,'\r')) : is;
			String s =  Util.readString( commandIn , sopts.getInputTextEncoding());
			s = Util.removeTrailingNewlines(s, shell.getSerializeOpts().isIgncr() );
			return s;
		} 

	}
	private XdmNode	expandXFile( Shell shell , Word xfile, SourceLocation loc ) throws IOException, CoreException{
		XValue 	files = xfile.expand(shell, mEnv, loc);
		String file;
		if( files.isAtomic() )
			file = files.toString();
		else 
			throw new InvalidArgumentException("Invalid expansion for redirection");

		try (
				InputPort ip = shell.newInputPort(file);
				){

			XdmNode node = ip.asXdmNode( shell.getSerializeOpts());
			return node;
		} 
	}




	@Override
	public boolean isEmpty() {
		return mFile == null ||  mFile.isEmpty();
	}

	@Override
	public String toString()
	{
		return mType + mFile + ")";
	}

	@Override
	String getSimpleName()
	{
		return mType + mFile.getSimpleName()  + ")";
	}

	@Override
	protected ParseResult expandToResult(Shell shell, EvalEnv env, SourceLocation loc, ParseResult result) throws IOException,
	CoreException
	{

		if(mType.equals("$(<")){

			String value = expandFile( shell , mFile,loc );
			result.add( XValue.newXValue(value) , true );

		} else
			if( mType.equals("$<(<")){
				XdmNode node = expandXFile(shell , mFile,loc);
				result.add( XValue.newXValue( TypeFamily.XDM,node) , true );
			}
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
