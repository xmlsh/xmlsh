/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.type.Type;

/*
 
 
<p:declare-step
name? = NCName
type? = QName
psvi-required? = boolean
xpath-version? = string
exclude-inline-prefixes? = prefix list>
  (p:input |
   p:output |
   p:option |
   p:log |
   p:serialization)*,
  ((p:declare-step |
    p:import)*,
   subpipeline)?
</p:declare-step>

*/




class DeclareStep {
	String				name;
	QName				type;
	boolean 			psvi_required;
	String 				xpath_version;
	String[]	 		exclude_inline_prefixes;
	
	// Input/out/options
	InputList	 		inputs 	= new InputList();
	OutputList	 		outputs = new OutputList();
	List<Option>		options = new ArrayList<Option>();
	List<Log>	  		logs 	= new ArrayList<Log>();
	List<Serialization>	serializations =  new ArrayList<Serialization>();
	
	// Step declarations including libraries
	List<DeclareStep>	steps = new ArrayList<DeclareStep>();
	List<Import>		imports = new ArrayList<Import>();
	SubPipeline			subpipeline = new SubPipeline();
	
	List<Namespace>		namespaces = new ArrayList<Namespace>();
	
	static DeclareStep create( XdmNode node )
	{
		DeclareStep step = new DeclareStep();
		step.parse(node);
		return step;
	}
	
	
	void parse(XdmNode node)
	{
		name 			= XProcUtil.getAttrString(node, "name");
		type  			= XProcUtil.getAttrQName(node,"type");
		psvi_required 	= XProcUtil.getAttrBool(node,"psvi-required",false);
		xpath_version 	= XProcUtil.getAttrString(node, "xpath-version");
		exclude_inline_prefixes = XProcUtil.getAttrList( node , "exclude-inline-prefixes");
		parseNamespaces(node);
		parseChildren( node );
		
	}

	protected void parseChildren(XdmNode parent) {
		XdmSequenceIterator children = parent.axisIterator(Axis.CHILD);
		while( children.hasNext() ){
			XdmItem item=children.next();
			if( item instanceof XdmNode ){
				XdmNode child = (XdmNode) item ;
				if( child.getNodeKind() != XdmNodeKind.ELEMENT )
					continue ;
				
				
				QName name = child.getNodeName();
				
				if( name.equals(Names.kINPUT))
					inputs.add( Input.create(child));
				else
				if( name.equals(Names.kOUTPUT))
					outputs.add( Output.create(child));
				else
				if( name.equals(Names.kOPTION))
					options.add( Option.create(child));
				else
				if( name.equals(Names.kLOG))
					logs.add( Log.create(child));
				else
				if( name.equals(Names.kSERIALIZATION))
					serializations.add( Serialization.create(child));
				else
				if( name.equals(Names.kDECLARE_STEP))
					steps.add( DeclareStep.create(child));
				else
				if( name.equals( Names.kIMPORT))
					imports.add( Import.create(child));
				else
					subpipeline.parse( child );
				
				
			}
			
			
			
		}
		
	}


	private void parseNamespaces(XdmNode node) {

		int [] ns = node.getUnderlyingNode().getDeclaredNamespaces(null);
		if( ns == null )
			return  ;
		

		
		for( int code : ns ){
			namespaces.add(new Namespace(node.getUnderlyingNode(),code));
		}
		

	}



	void serialize(OutputContext c) {
		
		Input saveIn = c.getPrimaryInput();
		for( Namespace ns : namespaces ){
			c.addPreambleLine("declare namespace " + ns.getPrefix() + "=\"" + ns.getUri() + "\" ");
		}
		Input in =	inputs.getPrimary();
		c.setPrimaryInput(in);
//		if( in != null )
//			c.addBodyLine("xread " + in.getPortVariable() );
		c.addBody(" ( ");
		c = c.push();
		subpipeline.serialize(c);
		c = c.pop();
		c.addBody(" )" );
		
		if( in != null ){
			// c.addBody("<{" + in.getPortVariable() +"}");

			in.serialize(c);
			
		}
		
		c.setPrimaryInput(saveIn);
			
		
	}
	
	
};


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
