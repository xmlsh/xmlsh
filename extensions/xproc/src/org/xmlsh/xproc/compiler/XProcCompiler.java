/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.xproc.compiler;

import java.io.PrintWriter;
import java.net.URI;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;


public class XProcCompiler {


	private		DeclareStep 	mPipeline;	// Root step
	private		Environment		mEnvironment ;
	




	/*
	 * Parsing phase
	 * Load xproc docment 
	 */

	public void parse(XdmNode root) {
		
		if( root.getNodeKind() == XdmNodeKind.DOCUMENT )
			root = (XdmNode) root.axisIterator(Axis.CHILD).next();

		
		QName name = root.getNodeName();
		
		
		
		if( name.equals( Names.kPIPELINE ))
			mPipeline = pipeline(root);
		else
		if( name.equals( Names.kDECLARE_STEP))
			mPipeline = declareStep(root);
		else
		if( name.equals( Names.kLIBRARY ))
			library(root);
		
		
		
	}
	
	private void library(XdmNode root) 
	{
		
		
	}

	private DeclareStep declareStep(XdmNode root) {
			return 	DeclareStep.create(root);

	}

	private DeclareStep pipeline(XdmNode root) {
			return Pipeline.create(root);
		
	}

	public void serialize(OutputContext c) throws Exception
	{

		mPipeline.serialize( c );

		
	}
	
	
	
	
	
	
	
	
}



//
//
//Copyright (C) 2008,2009 , David A. Lee.
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
