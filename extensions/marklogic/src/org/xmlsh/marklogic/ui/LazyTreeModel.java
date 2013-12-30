/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

@SuppressWarnings("serial")
public abstract class LazyTreeModel extends DefaultTreeModel implements TreeWillExpandListener {

	
	abstract void loadNodes( LazyTreeNode node );
	
	public LazyTreeModel(TreeNode root, JTree tree) {
		super(root);
		setAsksAllowsChildren(true);
		tree.addTreeWillExpandListener(this);
		tree.setModel(this);
	}

	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		LazyTreeNode node = (LazyTreeNode) event.getPath().getLastPathComponent();
		if (node.isLoaded()) {
			return;
		}
		setLoading(node, false);
		loadNodes(node);
	}

	public void reloadNode(String id) {
		LazyTreeNode node = findNode(id);
		if (node != null) {
			node.setLoaded(false);
			setLoading(node, true);
			loadNodes(node);
		}
	}

	public void reloadParentNode(String id) {
		LazyTreeNode node = findParent(id);
		if (node != null) {
			node.setLoaded(false);
			setLoading(node, true);
			loadNodes(node);
		}
	}

	public LazyTreeNode findParent(String id) {
		LazyTreeNode node = findNode(id);
		if (node != null && node.getParent() != null) {
			return (LazyTreeNode) node.getParent();
		}
		return null;
	}

	public void loadFirstLevel() {
		setLoading((LazyTreeNode) getRoot(), false);
		loadNodes((LazyTreeNode) getRoot());
	}

	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
	}

	protected void setChildren(LazyTreeNode parentNode, LazyTreeNode... nodes) {
		if (nodes == null) {
			return;
		}
		int childCount = parentNode.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				removeNodeFromParent((MutableTreeNode) parentNode.getChildAt(0));
			}
		}
		for (int i = 0; i < nodes.length; i++) {
			insertNodeInto(nodes[i], parentNode, i);
		}
	}

	private void setLoading2(final LazyTreeNode parentNode, final boolean reload) {
		if (reload) {
			setChildren(parentNode, createReloadingNode());
		} else {
			setChildren(parentNode, createLoadingNode());
		}
	}

	private void setLoading(final LazyTreeNode parentNode, final boolean reload) {
		if (SwingUtilities.isEventDispatchThread()) {
			setLoading2(parentNode, reload);
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						setLoading2(parentNode, reload);
					}
				});
			} catch (Exception e) {
				// LOG.error("Cannot create loading node", e);
			}
		}
	}

	private LazyTreeNode findNode(String id) {
		return findNode(id, (LazyTreeNode) getRoot());
	}

	private LazyTreeNode findNode(String url, LazyTreeNode parent) {
		int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			LazyTreeNode node = (LazyTreeNode) parent.getChildAt(i);
			if (url.equals(node.getUrl())) {
				return node;
			}
			if (node.isLoaded()) {
				node = findNode(url, node);
				if (node != null) {
					return node;
				}
			}
		}
		return null;
	}


	protected LazyTreeNode createLoadingNode() {
		return new LazyTreeNode(null, "Loading ..." );
	}

	protected LazyTreeNode createReloadingNode() {
		return new LazyTreeNode(null, "Refreshing...");
	}
	
	void postSetChildren( final LazyTreeNode parentNode , final LazyTreeNode[] treeNodes ){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				parentNode.setLoaded(true);
				setChildren(parentNode, treeNodes);
				nodeChanged( parentNode );
			}
		});	
	}
	
	void postReload( final LazyTreeNode node ){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if( node == null )
					loadFirstLevel() ;
				else {
					node.setLength(-1, -1);
					node.setLoaded(false);
					setLoading(node, true);
					loadNodes(node);
					nodeChanged( node );
				}

			}
		});	
	}
	
	void postReset( final LazyTreeNode node ){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				node.setLength(-1, -1);
				node.setLoaded(false);
				setLoading(node, true);
				
				setChildren(node, new LazyTreeNode[]{ } );

				nodeChanged( node );

			}
		});	
	}
	
	void postNext( final LazyTreeNode node ,  final int n ){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				node.setLength( node.getEnd() + 1 , node.getEnd() + n  );
				node.setLoaded(false);
				setLoading(node, true);
				
				loadNodes(node);
				nodeChanged( node );

			}
		});	
	}
	
	void postPrev( final LazyTreeNode node ,  final int n ){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int start = node.getStart()  - n ;
				if( start <1 )
					start = 1;
				
				node.setLength( start , start + n - 1  );
				node.setLoaded(false);
				setLoading(node, true);
				
				loadNodes(node);
				nodeChanged( node );

			}
		});	
	}
	
	
}
	

	/*
	 * Copyright (C) 2008-2014 David A. Lee.
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