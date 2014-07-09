/**
 * $Id: $
 * $Date: $
 *
 */

/** 
 * Note: This class is in the net.sf.saxon.s9api package so that it can have access 
 * to package private method XdmNode.wrap() which is otherwise unavailable
 * 
 * @TODO: When s9api is changed to export XdmNode.wrap this class can go away
 * 
 */

package org.xmlsh.util;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.type.Type;

import org.apache.log4j.Logger;

import org.xmlsh.commands.xs.element;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

import java.util.ArrayList;
import java.util.List;


/*
 * S9Util extends from XdmNode so that we can gain access to protected methods
 * 
 */
public class S9Util extends XdmNode {
	private static Logger mLogger = Logger.getLogger(element.class);
	
	protected S9Util(NodeInfo node) {
		super(node);
		// TODO Auto-generated constructor stub
	}

	public static XdmValue	wrapNode( NodeInfo node)
	{
		return new XdmNode(node); // New constructor in 9.3
	}
	
	public static XdmItem	wrapItem( Item item)
	{
		return XdmNode.wrapItem(item);
	}
	



	public static XValue createAttribute(Shell shell, QName name, String value) {
		Processor processor = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();

		
		NameValueMap<String> ns = shell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}


		
		XQueryExecutable expr = null;

		StringBuffer sb = new StringBuffer();
		sb.append("declare variable $name external ; \n");
		sb.append("declare variable $value external ; \n");
		sb.append("attribute { $name } { $value } ");
		
		try {
			expr = compiler.compile( sb.toString() );
			
			XQueryEvaluator eval = expr.load();

			
			eval.setExternalVariable( new QName("name" ) , new XdmAtomicValue(name) );
			eval.setExternalVariable( new QName("value"),  new XdmAtomicValue(value) );
			
			
			XdmValue result =  eval.evaluate();
			
			
			return new XValue(result) ;
			
			
			
			
		} catch (SaxonApiException e) {
			mLogger.warn("Error creating attribute"  , e );

			shell.printErr("Error expanding xml expression");
		}
		return null;
	}
	
	
	public static XValue createElement(Shell shell, QName name , List<XValue> args) {
		Processor processor = Shell.getProcessor();
		
		XQueryCompiler compiler = processor.newXQueryCompiler();

		
		NameValueMap<String> ns = shell.getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				compiler.declareNamespace(prefix, uri);
				
			}
			
		}


		
		XQueryExecutable expr = null;

		StringBuffer sb = new StringBuffer();
		sb.append("declare variable $name external ; \n");
		sb.append("declare variable $value external ; \n");
		sb.append("element { $name } { $value } ");
		
		try {
			expr = compiler.compile( sb.toString() );
			
			XQueryEvaluator eval = expr.load();

			
			List<XdmItem> items = new ArrayList<XdmItem>();
			for( XValue arg : args ){
				for (XdmItem item : arg.asXdmValue())
					items.add(item);
				
			}
			XdmValue value = new XdmValue(items);
			
			
			
			
			
			eval.setExternalVariable( new QName("name" ) , new XdmAtomicValue(name) );
			eval.setExternalVariable( new QName("value") , value  );
			
			
			XdmValue result =  eval.evaluate();
			
			
			return new XValue(result) ;
			
			
			
			
		} catch (SaxonApiException e) {
			mLogger.warn("Error creating attribute"  , e );

			shell.printErr("Error expanding xml expression");
		}
		return null;
	}

	
	public static XdmNode wrapDocument(XdmNode node) throws CoreException  {
		if( node.getUnderlyingNode().getNodeKind() == Type.DOCUMENT  )
			return node ;
		return wrapDocument( node.getUnderlyingNode() );
		
		
	}
		
	
	public static XdmNode wrapDocument(NodeInfo nodeInfo) throws CoreException  {
		

		// If is document already 
		if( nodeInfo.getNodeKind() == Type.DOCUMENT )
				return (XdmNode) XdmNode.wrap( nodeInfo );
		
			
		DocumentBuilder builder = Shell.getProcessor().newDocumentBuilder();
		XdmNode xnode;
		try {
			xnode = builder.build(nodeInfo);
		} catch (SaxonApiException e) {
			throw new CoreException("Exception creating document from node", e );
		}
		return xnode;
		
		
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
