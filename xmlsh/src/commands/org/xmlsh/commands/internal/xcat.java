/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.commands.internal;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XQueryCompiler;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.VariableInputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.NameValueMap;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class xcat extends XCommand {
	/*
	 * TODO: xcat should use the collection() instead of doc()
	 * so that ports can be used as filenames instead of just files or URI's
	 */

	private XMLEventFactory mFactory = XMLEventFactory.newInstance();
	
	
	public int run( List<XValue> args )	throws Exception
	{
		


		Options opts = new Options( "w=wrap:,r=root" ,  SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		// root node
		OptionValue ow = opts.getOpt("w");
		XValue wrapper = null;
		if( ow != null )
			wrapper = ow.getValue();
		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		
		

		Processor  processor  = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();
		NameValueMap<String> ns = getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}
			
		
		
		// hasFiles means 'has more then one file'
		// this effects if wrapping by default removes the root element.
		boolean bHasFiles = ( xvargs.size() > 0 );
		boolean bRemoveRoot = opts.hasOpt("r");
		
		
		// Use a copy of the serialize opts so we can override the method 
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		
		
		
		OutputPort stdout = getStdout();
		XMLEventWriter writer = stdout.asXMLEventWriter(serializeOpts);
		
		
		writer.add( mFactory.createStartDocument(serializeOpts.getOutputXmlEncoding()));
		
		
		
		if( wrapper != null )
			writeWrapperStart(wrapper, writer , serializeOpts );
		
		 if( !bHasFiles ){
			 InputPort in = getStdin();
			 write(in,writer,bRemoveRoot,serializeOpts);
			 
		 } else {
			 for( XValue xf : xvargs ){
				 	InputPort in = getInput(xf);
				 	try {
				 		write(in,writer,bRemoveRoot,serializeOpts);
				 	} catch (Exception e) {
						this.printErr("Skipping file: " + in.getSystemId() , e );
					}
			 }
		 }
		 if( wrapper != null )
			 writeWrapperEnd( wrapper , writer , serializeOpts);
		 
		 writer.add( mFactory.createEndDocument() );
		
		writer.flush();
		writer.close();
		stdout.writeSequenceTerminator(serializeOpts);
		
		return 0;


	}


	private void write(InputPort in, XMLEventWriter writer, boolean bRemoveRoot,SerializeOpts opts) throws CoreException, XMLStreamException {
		
		XMLEventReader reader = in.asXMLEventReader(opts);
		XMLEvent event ;
		int depth = 0;
		while( reader.hasNext() ){
			event = reader.nextEvent();
			if( event.isStartDocument() || event.isEndDocument() )
				continue;
			else
			if( event.isStartElement() ){
				if( bRemoveRoot && depth == 0 )
					;
				else
					writer.add( event );
					
				depth++;	
			}
			else
			if( event.isEndElement()){
				depth--;
				if( bRemoveRoot && depth == 0 )
					;
				else
					writer.add( event );
			}
			else
			if( depth == 1 && bRemoveRoot && event.isCharacters() && event.asCharacters().isWhiteSpace() )
				;
			else
				writer.add( event );
			
			
		}

		
	}


	private void writeWrapperStart(XValue wrapper, XMLEventWriter writer, SerializeOpts opts) throws XMLStreamException, CoreException {
		if( wrapper.isAtomic() )
			writer.add( mFactory.createStartElement(new QName(wrapper.toString()), null, null));
		
		else
		{
			XVariable var = new XVariable( "_unnamed" , wrapper );
			VariableInputPort nodep = new VariableInputPort( var );
			XMLEventReader reader = nodep.asXMLEventReader(opts);
			XMLEvent event ;
			while( reader.hasNext() ){
				event = reader.nextEvent();
				if( event.isAttribute() ||  event.isStartElement()){
					writer.add(event);
				}
				
				
				
			}
			reader.close();
			nodep.close();
			
			
		}
	}

	private void writeWrapperEnd(XValue wrapper, XMLEventWriter writer, SerializeOpts opts) throws XMLStreamException, CoreException {
		if( wrapper.isAtomic() )
			writer.add( mFactory.createEndElement(new QName(wrapper.toString()), null));
		
		else
		{
			XVariable var = new XVariable( "_unnamed" , wrapper );
			VariableInputPort nodep = new VariableInputPort( var );
			XMLEventReader reader = nodep.asXMLEventReader(opts);
			XMLEvent event ;
			while( reader.hasNext() ){
				event = reader.nextEvent();
				if( event.isEndElement() ){
					writer.add(event);
				}
				
				
				
			}
			reader.close();
			nodep.close();
			
			
		}
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
