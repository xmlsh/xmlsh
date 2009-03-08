/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.transform.Source;

import net.sf.saxon.AugmentedSource;
import net.sf.saxon.event.Builder;
import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.tree.DocumentImpl;
import net.sf.saxon.type.Type;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;


public class xaddbase extends XCommand {

	private DocumentBuilder mBuilder;
	private Processor mProcessor;

	private void setupBuilders()
	{
/*		
		mProcessor = new Processor(false);
		mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL, net.sf.saxon.event.Builder.LINKED_TREE);
*/
		mProcessor = Shell.getProcessor();
		

		mBuilder = mProcessor.newDocumentBuilder();
		
	}
	
	
	
	@Override
	public int run( List<XValue> args )
	throws Exception 
	{
		boolean		opt_all		= false ;
		boolean		opt_relative = false ;
		
		
		Options opts = new Options( "i:,a=all,r=relative" , args );
		opts.parse();
		
		setupBuilders();

		XdmNode	context = null;

		
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
				context = build( getSource(ov.getValue()));
			else {
				context = build(getStdin().asSource());
			}	
		}
		
		opt_all 	= opts.hasOpt("all");
		opt_relative = opts.hasOpt("relative");
		add_base( context , opt_all , opt_relative );
			
		


		OutputPort stdout = getStdout();
		Util.writeXdmValue( context , stdout.asDestination() );
		stdout.writeSequenceTerminator();
		
		return 0;

	}
	
	


	private void add_child_base(NodeInfo parent, boolean opt_all, boolean opt_relative, String parentBaseURI ) throws URISyntaxException 
	{
		
		
		AxisIterator iter = parent.iterateAxis( Axis.CHILD );

		
		Item item;
		while( (item = iter.next()) != null ){

			if( item instanceof MutableNodeInfo  ){
				MutableNodeInfo node = (MutableNodeInfo) item;
				if( node.getNodeKind() == Type.ELEMENT ){
					String baseURI = node.getBaseURI();
					if( opt_all ||  ! Util.isEqual( baseURI , parentBaseURI ) )
						addAttribute( node , "xml" , "http://www.w3.org/XML/1998/namespace", "base" , resolve(parentBaseURI , baseURI, opt_relative) );
					else
						removeAttribute( node , "xml" , "http://www.w3.org/XML/1998/namespace", "base");
					
					add_child_base( node , opt_all , opt_relative, baseURI );
					
				}
			}
			
			
			
		}
		
	}

	
	
	
	


	private String resolve(String parentBaseURI, String baseURI, boolean opt_relative) throws URISyntaxException {
		if(! opt_relative )
			return baseURI ;
		
		URI u = new URI( parentBaseURI );
		// Remove final path component from parent 
		String path = u.getPath();
		int slash = path.lastIndexOf('/');
		if( slash >= 0 )
			path = path.substring(0,slash);
		URI parent_URI = 
			new URI(u.getScheme(),
			        u.getUserInfo(), u.getHost(), u.getPort(),
			        path, 
			        u.getQuery(),
			        u.getFragment());

		
		URI base_URI = new URI( baseURI );
		
		URI relative = parent_URI.relativize(base_URI);
		return relative.toString();
	}



	private void add_base(XdmNode xroot, boolean opt_all, boolean opt_relative ) throws URISyntaxException 
	{
	
		
		NodeInfo root = null;
		if( xroot.getNodeKind() == XdmNodeKind.DOCUMENT )
			root = ((DocumentImpl) xroot.getUnderlyingNode()).getDocumentElement();
		else
			root = xroot.getUnderlyingNode();
		
		
		MutableNodeInfo node = (MutableNodeInfo) root;
		if( node.getNodeKind() == Type.ELEMENT ){
			String baseURI = node.getBaseURI();
			addAttribute( node , "xml" , "http://www.w3.org/XML/1998/namespace", "base" , baseURI );
			add_child_base( node , opt_all , opt_relative, baseURI );
			
			
		}

		
	}


	private void addAttribute(MutableNodeInfo node, String prefix, String uri , String local , String value ) {

		NamePool pool = node.getNamePool();
		int nameCode  = pool.allocate(prefix , uri , local  );
		node.putAttribute(nameCode,  StandardNames.XS_UNTYPED_ATOMIC, value , 0);

	}


	private void removeAttribute(MutableNodeInfo node, String prefix, String uri, String local) {
		NamePool pool = node.getNamePool();
		int nameCode  = pool.allocate(prefix , uri , local  );
		node.removeAttribute(nameCode);
		
	}




	/*
	 * Import the node using the builder into this object model
	 */
	private XdmNode importNode( XdmNode node ) throws SaxonApiException
	{
		Source src = node.asSource();
		return build(src);
	}

	

	
	/*
	 * Creates/Builds a Tree (LINKED_TREE) type node from any source
	 */
	
	private XdmNode build( Source src ) throws SaxonApiException
	{
		// @TODO: To get over a bug in Saxon's build() have to use the root element
		// instead of a document node to force building of a linked tree model
		// Otherwise the source is just returned unchnaged
		
		if( src instanceof DocumentInfo  )
			src = (NodeInfo)(((DocumentInfo)src).iterateAxis(net.sf.saxon.om.Axis.CHILD).next());

		
		AugmentedSource asrc = AugmentedSource.makeAugmentedSource(src); 
		asrc.setTreeModel(Builder.LINKED_TREE); 
		return mBuilder.build(asrc);
		
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
