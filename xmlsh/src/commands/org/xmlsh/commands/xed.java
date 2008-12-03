/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands;

import java.util.List;

import net.sf.saxon.FeatureKeys;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.tree.DocumentImpl;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.util.Util;


public class xed extends XCommand {

	private DocumentBuilder mBuilder;
	private XPathCompiler mCompiler;
	private Processor mProcessor;

	private void setupBuilders()
	{
		
		mProcessor = new Processor(false);
		mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL, net.sf.saxon.event.Builder.LINKED_TREE);
		mCompiler = mProcessor.newXPathCompiler();
		mBuilder = mProcessor.newDocumentBuilder();
		
	}
	
	
	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		boolean 	opt_delete	= false ;
		XValue		opt_add 	= null;
		XValue		opt_replace = null;
		
		
		Options opts = new Options( "f:,i:,n,v,r:,a:,d" , args );
		opts.parse();
		
		setupBuilders();

		XdmNode	context = null;

		
		// boolean bReadStdin = false ;
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");

			
			// If -i argument is an XML expression take the first node as the context
			if( ov != null  && ov.getValue().isXExpr() ){
				XdmItem item = ov.getValue().asXdmValue().itemAt(0);
				if( item instanceof XdmNode )
				//   context = (XdmNode) item ; // builder.build(((XdmNode)item).asSource());
				 // context = (XdmNode) ov.getValue().toXdmValue();
				context = importNode( (XdmNode)item);

			}
			if( context == null )
			{
	
				if( ov != null && ! ov.getValue().toString().equals("-"))
					context = mBuilder.build( getFile(ov.getValue()));
				else {
					context = mBuilder.build(getStdin().asSource());
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
		

		
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				mCompiler.declareVariable(new QName(name));			
			}
		}
		

		
		opt_add		= opts.getOptValue("a");
		opt_replace = opts.getOptValue("r");
		opt_delete  = opts.hasOpt("d");
		
		

		XPathExecutable expr = mCompiler.compile( xpath );
		
		XPathSelector eval = expr.load();
		if( context != null )
			eval.setContextItem(context);
		
		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				XValue value = xvargs.get(i*2+1);
				eval.setVariable( new QName(name),  value.asXdmValue() );	
			}
		}
		
		
		
	
		for( XdmItem item : eval ){
			Object obj = item.getUnderlyingValue();
			if( obj instanceof MutableNodeInfo ){
				MutableNodeInfo node = (MutableNodeInfo) obj;
				if( opt_replace != null )
					replace(node, opt_replace);
				if( opt_add != null )
					add( node , opt_add );
				if( opt_delete )
					delete( node );
				
				// else
				// if( opt_add != null )
				//	add( builder , )
			}
			
			
		}
		
	
		mProcessor.writeXdmValue(context, getStdout().asDestination() );

		
		return 0;

	}

	private void delete(MutableNodeInfo node) {
		node.delete();
	}



	private void add(MutableNodeInfo node, XValue add) throws IndexOutOfBoundsException, SaxonApiUncheckedException, SaxonApiException {
		if( ! add.isAtomic() ){
			XdmNode xnode = (XdmNode) add.asXdmValue();
			if( xnode.getNodeKind() == 	XdmNodeKind.ATTRIBUTE ) {
				NodeInfo anode = xnode.getUnderlyingNode();
				NamePool pool = node.getNamePool();
				int nameCode  = pool.allocate( anode.getURI() , anode.getPrefix(),anode.getLocalPart() );
				node.putAttribute(nameCode,  StandardNames.XS_UNTYPED_ATOMIC, anode.getStringValueCS(), 0);
			} else {
				node.insertChildren( new NodeInfo[]  { getNodeInfo(xnode) } , true ,true );
			}
		} else
			node.replaceStringValue(node.getStringValue() + add.toString() );
		
	}



	private void replace(MutableNodeInfo node, XValue replace)
			throws IndexOutOfBoundsException, SaxonApiUncheckedException, SaxonApiException {
		if(  ! replace.isAtomic() ){
			XdmNode xnode = (XdmNode) replace.asXdmValue();
			if( xnode.getNodeKind() == 	XdmNodeKind.ATTRIBUTE ) {
				NodeInfo anode = xnode.getUnderlyingNode();
				
			
				NamePool pool = node.getNamePool();
				int nameCode  = pool.allocate( anode.getURI() , anode.getPrefix(),anode.getLocalPart() );
				
				node.putAttribute(nameCode,  StandardNames.XS_UNTYPED_ATOMIC, anode.getStringValueCS(), 0);
				
				
			} else 
				node.replace( new NodeInfo[]  { getNodeInfo(xnode) } , true );
				
			
		}
		else
			node.replaceStringValue( replace.toString() );
	}

	/*
	 * Import the node using the builder into this object model
	 */
	private XdmNode importNode( XdmNode node ) throws SaxonApiException
	{
		return mBuilder.build(node.asSource());
	}

	
	private NodeInfo getNodeInfo( XdmNode node) throws IndexOutOfBoundsException,
		SaxonApiUncheckedException, SaxonApiException {
		
		XdmNode xnode = importNode(node);
		
		return ((DocumentImpl) xnode.getUnderlyingNode().getDocumentRoot()).getDocumentElement();
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
