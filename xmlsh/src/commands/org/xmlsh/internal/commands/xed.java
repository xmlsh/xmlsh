/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.internal.commands;

import net.sf.saxon.event.Builder;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.Axis;
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
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Type;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Namespaces;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.types.XDMTypeFamily;
import org.xmlsh.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;


public class xed extends XCommand {

	private DocumentBuilder mBuilder;
	private XPathCompiler mCompiler;
	private Processor mProcessor;

	private void setupBuilders()
	{
		/*		
		mProcessor = new Processor(false);
		mProcessor.setConfigurationProperty(FeatureKeys.TREE_MODEL, net.sf.saxon.event.Builder.LINKED_TREE);
		 */
		mProcessor = Shell.getProcessor();


		mCompiler = mProcessor.newXPathCompiler();
		mBuilder = mProcessor.newDocumentBuilder();
		mBuilder.setTreeModel(TreeModel.LINKED_TREE);

		Namespaces ns = getEnv().getNamespaces();
		if( ns != null ){
			for( String prefix : ns.keySet() ){
				String uri = ns.get(prefix);
				mCompiler.declareNamespace(prefix, uri);

			}

		}


	}



	@Override
	public int run( List<XValue> args )
			throws Exception 
			{
		boolean 	opt_delete	= false ;
		XValue		opt_add 	= null;
		XValue		opt_replace = null;
		String		opt_matches = null ;
		String		opt_xpath	= null ;
		String		opt_replacex = null ;
		String		opt_rename = null;




		// backwards compatible -r == -r/text
		Options opts = new Options( "i=input:,e=xpath:,n,v,r=replace:,a=add:,d=delete,m=matches:,rx=replacex:,ren=rename:" ,  
				SerializeOpts.getOptionDefs()  );
		opts.parse(args);

		setupBuilders();

		XdmNode	context = null;


		// boolean bReadStdin = false ;
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		if( ! opts.hasOpt("n" ) ){ // Has XML data input
			OptionValue ov = opts.getOpt("i");


			// If -i argument is an XML expression take the first node as the context
			if( ov != null  && ov.getValue().isXdmItem()  ){
				XdmItem item = ov.getValue().asXdmItem();
				if( item instanceof XdmNode )
					//   context = (XdmNode) item ; // builder.build(((XdmNode)item).asSource());
					// context = (XdmNode) ov.getValue().toXdmValue();
					context = importNode( (XdmNode)item);

			}
			if( context == null )
			{
				Source src = null ;
				InputPort insrc = null ;
				if( ov != null && ! ov.getValue().toString().equals("-"))
					insrc = getInput(ov.getValue());
				else {
					insrc = getStdin();
				}	
				context = build(insrc.asSource(serializeOpts));
			}
		}


		List<XValue> xvargs = opts.getRemainingArgs();





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
		opt_replacex = opts.getOptString("rx", null );
		opt_rename = opts.getOptString("ren",null);


		opt_matches = opts.getOptString("matches",null);
		opt_xpath 	= opts.getOptString("xpath",null);
		if( opt_matches == null && opt_xpath == null )
			throw new InvalidArgumentException("option xpath or matches must be specified");


		XPathExecutable expr;
		if( opt_matches == null )
			expr = mCompiler.compile( opt_xpath );
		else 
			expr = mCompiler.compilePattern( opt_matches );


		XPathSelector eval = expr.load();

		if( opts.hasOpt("v")){
			// Read pairs from args to set
			for( int i = 0 ; i < xvargs.size()/2 ; i++ ){
				String name = xvargs.get(i*2).toString();
				XValue value = xvargs.get(i*2+1);
				eval.setVariable( new QName(name),  value.toXdmValue() );	
			}
		}

		XPathSelector replacex = null ;
		if( opt_replacex != null ){
			XPathExecutable xe = mCompiler.compile(opt_replacex);
			replacex = xe.load(); 

		}




		if( opt_replace != null || opt_delete  || opt_add  != null || opt_replacex != null || opt_rename != null  ){


			Iterable<XdmItem>  results = getResults( eval ,  context , opt_matches != null );
			for( XdmItem item : results ){
				Object obj = item.getUnderlyingValue();
				if( obj instanceof MutableNodeInfo ){
					MutableNodeInfo node = (MutableNodeInfo) obj;
					if( opt_replace != null )
						replace(node, opt_replace,true);
					if( replacex != null )
						replace( item ,  node , replacex );
					if( opt_add != null )
						add( node , opt_add );
					if( opt_delete )
						delete( node );
					if( opt_rename != null )
						rename( node , opt_rename );

				}
			}
		}




		OutputPort stdout = getStdout();
		Util.writeXdmValue( context , stdout.asDestination(serializeOpts) );
		stdout.writeSequenceTerminator(serializeOpts);

		return 0;

			}


	private Iterable<XdmItem> getResults(XPathSelector eval, XdmNode root , boolean opt_matches) throws SaxonApiException {

		if( ! opt_matches ){
			if( root != null )
				eval.setContextItem(root);
			return eval ;
		}
		ArrayList<XdmItem>	results = new ArrayList<XdmItem>();
		if( root == null )
			return results;

		XdmSequenceIterator iter = root.axisIterator(Axis.DESCENDANT_OR_SELF);
		while( iter.hasNext() ){
			XdmItem item = iter.next();
			eval.setContextItem(item);
			if( eval.effectiveBooleanValue())
				results.add(item);
			if( item instanceof XdmNode ){
				XdmSequenceIterator aiter = ((XdmNode)item).axisIterator(Axis.ATTRIBUTE);
				while( aiter.hasNext() ){
					XdmItem item2 = aiter.next();
					eval.setContextItem(item2);
					if( eval.effectiveBooleanValue())
						results.add(item2);
				}

			}
		}
		return results ;
	}



	private void delete(MutableNodeInfo node) {
		node.delete();
	}



	private void add(MutableNodeInfo node, XValue add) throws IndexOutOfBoundsException, SaxonApiUncheckedException, SaxonApiException {
		if( ! add.isAtomic() ){
			XdmNode xnode = add.asXdmNode();
			if( xnode.getNodeKind() == 	XdmNodeKind.ATTRIBUTE ) {
				NodeInfo anode = xnode.getUnderlyingNode();
				addAttribute(node, anode.getPrefix() , anode.getURI() , anode.getLocalPart(), anode.getStringValue() );
			} else {
				node.insertChildren( new NodeInfo[]  { getNodeInfo(xnode) } , true ,true );
			}
		} else
			node.replaceStringValue(node.getStringValue() + add.toString() );

	}



	private void addAttribute(MutableNodeInfo node, String prefix, String uri , String local , String value ) {

		NamePool pool = node.getNamePool();
		int nameCode  = pool.allocate(prefix , uri , local  );

		CodedName name = new CodedName(nameCode,pool);
		node.addAttribute(name,  BuiltInAtomicType.UNTYPED_ATOMIC, value , 0);
		if( !Util.isEmpty(prefix) ){

			node.addNamespace( name.getNamespaceBinding() , false);
		}
	}



	private void replace(MutableNodeInfo node, XValue replace, boolean compat_mode )
			throws IndexOutOfBoundsException, SaxonApiUncheckedException, SaxonApiException, XPathException {
		if(  ! replace.isAtomic() ){
			XdmNode xnode = (XdmNode) replace.asXdmNode();
			if( xnode.getNodeKind() == 	XdmNodeKind.ATTRIBUTE ) {
				NodeInfo anode = xnode.getUnderlyingNode();

				NodeInfo existsAttr = this.findAttribute(node , anode );
				if( existsAttr  != null )
					node.removeAttribute(existsAttr);


				addAttribute(node, anode.getPrefix() , anode.getURI() , anode.getLocalPart(), anode.getStringValue() );			
			} else 
				node.replace( new NodeInfo[]  { getNodeInfo(xnode) } , true );
		}
		else
		if( compat_mode || node.getNodeKind() ==  Type.ATTRIBUTE )
			node.replaceStringValue( replace.toString() );
		else 
		  node.replace( new NodeInfo[]  {createTextNode( node , replace.toXdmValue()) } , true );
	}



	private void rename(MutableNodeInfo node, String opt_rename) throws IndexOutOfBoundsException,
	SaxonApiUncheckedException, SaxonApiException {



		NamePool pool = node.getNamePool();
		int newNameCode = pool.allocateClarkName( opt_rename );

		CodedName name = new CodedName(newNameCode,pool);
		node.rename(  name );

	}



	private void replace(XdmItem item , MutableNodeInfo node, XPathSelector replacex)
			throws IndexOutOfBoundsException, SaxonApiUncheckedException, SaxonApiException, XPathException {

		replacex.setContextItem(item);

		// Convert to string and turn into an XdmItem
		XValue xreplace = XValue.asXValue( TypeFamily.XDM ,  replacex.evaluate()  );
		replace( node , xreplace , false );


	}





	private NodeInfo createTextNode(MutableNodeInfo parent , XdmValue value ) throws XPathException {
		/*
		net.sf.saxon.om.Orphan textNode = new net.sf.saxon.om.Orphan(mProcessor.getUnderlyingConfiguration());
		textNode.setNodeKind( Type.TEXT );
		textNode.setStringValue( replace );
		 */

		/*
			net.sf.saxon.tree.TextImpl textNode = new net.sf.saxon.tree.TextImpl( null , replace);
		 */

		/*
		try {
			Class<?> cls = Class.forName("net.sf.saxon.tree.TextImpl");
			Class<?> parentClass = Class.forName("net.sf.saxon.tree.ParentNodeImpl");
			Constructor<?> cons = cls.getConstructor(parentClass , String.class );
			NodeInfo text = (NodeInfo) cons.newInstance(null , replace );
			return text ;



		} catch( Exception e ) 
		{
			this.printErr("Exception loading textImpl", e);
			return null;
		}
		 */
		// return SaxonUtil.createTextNode(replace);

		/*
		 * TOTAL HACK BECAUSE WE CANT CREATE A TEXT NODE !!!!
		 */
		// Make the children a text node 
	  
	   
	    Builder builder = parent.newBuilder() ;
	    builder.open();
	    builder.startDocument(0);
	    builder.characters(value.toString(), 0, 0);
	    builder.endDocument();
	    builder.close();
	    return builder.getCurrentRoot().iterateAxis(net.sf.saxon.om.Axis.CHILD).next();
      
	    
	    /*
		parent.replaceStringValue(replace);

		Item item  = parent.iterateAxis( net.sf.saxon.om.Axis.CHILD ).next();
		return (NodeInfo) item ;

*/


	}



	/*
	 * Import the node using the builder into this object model
	 */
	private XdmNode importNode( XdmNode node ) throws SaxonApiException
	{
		Source src = node.asSource();
		return build(src);
	}


	private NodeInfo getNodeInfo( XdmNode node) throws IndexOutOfBoundsException,
	SaxonApiUncheckedException, SaxonApiException {

		XdmNode xnode = importNode(node);

		return ((DocumentImpl) xnode.getUnderlyingNode().getDocumentRoot()).getDocumentElement();
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
			src = (((DocumentInfo)src).iterateAxis(net.sf.saxon.om.Axis.CHILD).next());
		return mBuilder.build(src);

	}


	/*
	 * Find a matching attribute to a passed in one
	 * Compare URI and local part
	 */

	private NodeInfo findAttribute( NodeInfo node , NodeInfo attr )
	{

		// Write attributes
		AxisIterator iter = node.iterateAxis(net.sf.saxon.om.Axis.ATTRIBUTE);
		Item item;
		while( ( item = iter.next() ) != null ){
			NodeInfo a = (NodeInfo) item;
			if( a.getURI().equals(attr.getURI()) &&
					a.getLocalPart().equals( attr.getLocalPart()) )

				return a;

		}
		return null;
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
