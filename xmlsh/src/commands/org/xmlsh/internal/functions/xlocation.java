/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.functions;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.Options;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.io.VariableOutputPort;
import org.xmlsh.sh.core.SourceLocation;
import org.xmlsh.sh.core.SourceLocator;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;

public class xlocation extends AbstractBuiltinFunction {

	public static final String XLOC_OPTS = 
	        "d=depth:,f=function,n=name,s=source,start=start-line,end=end-line,scol=start-column,ecol=end-column,r=relpath,p=path,f=file";

	public xlocation()
	{
		super("xlocation");
	}


	@Override
	public XValue run(Shell shell, List<XValue> args) throws Exception
	{

		Options opts = new Options(XLOC_OPTS,SerializeOpts.getOptionDefs());
		opts.parse(args);
		return run( shell , opts , -1 );
	}

	// for xstacktrace

	public XValue run(Shell shell, Options opts, int depth ) throws Exception
	{

		List<XValue> xv = new ArrayList<XValue>();

		depth = opts.getOptInt("depth", depth );
		SourceLocation loc = shell.getLocation(depth);

		if( loc == null )
			return null ;
		if( opts.hasOpt("name") && loc.hasName() )
			xv.add( XValue.newXValue( describeName(depth,loc)));
		if( opts.hasOpt("s") || opts.hasOpt("f") || opts.hasOpt("p")  ) 
			xv.add( XValue.newXValue(loc.getSource(opts.hasOpt("r") || (opts.hasOpt("f")&&!opts.hasOpt("p")) )) );
		if( opts.hasOpt("start") ) 
			xv.add( XValue.newXValue(loc.getStartline()));
		if( opts.hasOpt("end") ) 
			xv.add( XValue.newXValue(loc.getEndLine()));
		if( opts.hasOpt("scol") ) 
			xv.add( XValue.newXValue(loc.getStartColumn()));
		if( opts.hasOpt("ecol") ) 
			xv.add( XValue.newXValue(loc.getEndColumn()));

		if( xv.isEmpty() )
			return describe(shell,loc) ;

		return XValue.newXValue(xv);
	}



	private XValue describe(Shell shell , SourceLocation loc) throws Exception 
	{

		XVariable xv = XVariable.anonymousInstance(TypeFamily.XDM);

		try ( VariableOutputPort port = new VariableOutputPort( xv ) ){
			XMLStreamWriter writer = port.asXMLStreamWriter(shell.getSerializeOpts());

			writer.writeStartDocument();
			writer.writeStartElement(getName());
			writer.writeAttribute("name", loc.getName() );
			writer.writeAttribute("scope" , loc.hasName() ? "function" : "");
			writer.writeAttribute("source", loc.getSource(false) );
			writer.writeAttribute("file-name", loc.getSource(true) );
			writer.writeAttribute("end-column", String.valueOf(	loc.getEndColumn()));
			writer.writeAttribute("start-column",String.valueOf(loc.getStartColumn()));
			writer.writeAttribute("end-line", String.valueOf(	loc.getEndLine()));
			writer.writeAttribute("start-line", String.valueOf(	loc.getStartLine()));
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			port.flush();
			return xv.getValue();
		}



	}

	private String describeName(int depth, SourceLocation loc) {
		if( depth < 0 || ! loc.hasName())
			return loc.getName();
		else
			return "function " + loc.getName() + "()";
	}


}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
