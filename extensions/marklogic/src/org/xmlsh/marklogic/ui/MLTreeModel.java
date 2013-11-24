/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.xmlsh.marklogic.util.MLUtil;
import org.xmlsh.util.Util;

import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.types.XdmVariable;

@SuppressWarnings("serial")
public class MLTreeModel extends LazyTreeModel {
	ExplorerShell mShell ;
	
	
	class LoadNodesQuery extends MLQueryRequest 
	{
		int start ;
		int end ;
		private LazyTreeNode mNode ;
		LoadNodesQuery(LazyTreeNode node , int start , int end ) throws Exception  
		{
		  super(     
			"Loading Nodes ...",
			QueryCache.getInstance().getQuery( Util.isEmpty(node.getUrl()) ? "listRootDirectory.xquery" : "listDirectory.xquery" ),
		    new XdmVariable[] { 
			Util.isEmpty(node.getUrl()) ? null : 
					MLUtil.newVariable("root", node.getUrl() ) ,
			MLUtil.newVariable("start" , start ) , 
			MLUtil.newVariable("end" , end ) ,
			MLUtil.newVariable("urimatch", mShell.getOptions().mQuery )
			},
						
			    null );
		  this.start = start ;
		  this.end = end ;
		  mNode = node ;
		}

		@Override
		void onComplete(ResultSequence rs) throws Exception {

			final String[] dirs = rs.asStrings() ;
			
			int len = dirs.length ;

			LazyTreeNode children[] = new LazyTreeNode[ len ];

			int i = 0;
			for( String s : dirs )
				children[i++] = new LazyTreeNode( s , s.substring( mNode.getUrl().length() ) , s.endsWith("/") , i + start , i + start  );

		
			
			if( len >= mShell.getOptions().mMaxRows )
				mNode.setLength( start , end  );
			else
				mNode.setLength(-1,-1);
			
			postSetChildren(mNode , children );

		}
	}
	
	
	

	public MLTreeModel(TreeNode root, JTree tree, ExplorerShell shell) {
		super(root, tree);
		mShell = shell ;
	}

	@Override
	void loadNodes(final LazyTreeNode node ) {

		try {
			int start = node.getStart();
			int end   = node.getEnd();
			if( start == end  ){
				start = 1 ;
				end = start + mShell.getOptions().mMaxRows - 1;
			}
			mShell.putCommand( new LoadNodesQuery( node , start , end ) );
		} catch ( Exception e ) {
			mShell.printError("Exception loading nodes from query", e);
		}

	}

	public void reset() {
		postReset( (LazyTreeNode) getRoot() );
		
	}




}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */