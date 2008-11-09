/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands;

import java.util.List;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tree.DocumentImpl;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XEnvironment;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.util.Util;
import org.xmlsh.util.XMLException;


public class xed extends XCommand {

	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		
		Options opts = new Options( "f:,i:,n,v,r:" , args );
		opts.parse();
		
		// Use a seperate processor !
		Processor  processor  = new Processor(false);
		processor.setConfigurationProperty(FeatureKeys.TREE_MODEL, net.sf.saxon.event.Builder.LINKED_TREE);
//		Processor processor = env.getShell().getProcessor();
		
		XPathCompiler compiler = processor.newXPathCompiler();
		XdmNode	context = null;

		DocumentBuilder builder = processor.newDocumentBuilder();
		
		boolean bReadStdin = false ;
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");

			
			// If -i argument is an XML expression take the first node as the context
			if( ov != null  && ov.getValue().isXExpr() ){
				XdmItem item = ov.getValue().toXdmValue().itemAt(0);
				if( item instanceof XdmNode )
				//   context = (XdmNode) item ; // builder.build(((XdmNode)item).asSource());
					context = builder.build(((XdmNode)item).asSource());
				 // context = (XdmNode) ov.getValue().toXdmValue();
			}
			if( context == null )
			{
	
				if( ov != null && ! ov.getValue().toString().equals("-"))
					context = builder.build( getFile(ov.getValue()));
				else {
					bReadStdin = true ;
					context = builder.build( new StreamSource( getStdin()));
				}	
			}
		}
		

		List<XValue> xvargs = opts.getRemainingArgs();
		

		
		OptionValue ov = opts.getOpt("f");
		String xpath = null;
		if( ov != null )
			xpath = Util.readString( getFile(ov.getValue().toString()) ) ;
		else 
			xpath = xvargs.remove(0).toString();
		

		ov = opts.getOpt("r");
		if( ov == null )
			throwInvalidArg("No replacement [-r] specified");
		
		XValue replace = ov.getValue();
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();

				compiler.declareVariable(new QName(name));			
				
			}
				
			
		}
		
		
		

		XPathExecutable expr = compiler.compile( xpath );
		
		XPathSelector eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				XValue value = xvargs.get(i*2+1);
				
				
				eval.setVariable( new QName(name),  value.toXdmValue() );	
					
				
			}
				
			
		}
		
		
		
/*
		Serializer dest = new Serializer();
		dest.setOutputProperty( Serializer.Property.OMIT_XML_DECLARATION, "yes");
		dest.setOutputStream(env.getStdout());
*/		
	
		for( XdmItem item : eval ){
			Object obj = item.getUnderlyingValue();
			if( obj instanceof MutableNodeInfo ){
				MutableNodeInfo node = (MutableNodeInfo) obj;
				if( replace.isXExpr() && ! replace.isAtomic() )
					node.replace( new NodeInfo[]  { getNodeInfo(builder,replace) } , true );
				
				else
					node.replaceStringValue( replace.toString() );
			}
			
			
		}
		
		Serializer ser = new Serializer();
		ser.setOutputStream( getStdout());
		
		processor.writeXdmValue(context, ser );

		
		return 0;

	}

	private NodeInfo getNodeInfo(DocumentBuilder builder , XValue replace) throws IndexOutOfBoundsException,
		SaxonApiUncheckedException, SaxonApiException {
		
		XdmNode node = builder.build(((XdmNode) replace.toXdmValue()).asSource());
		
		return ((DocumentImpl) node.getUnderlyingNode().getDocumentRoot()).getDocumentElement();
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
