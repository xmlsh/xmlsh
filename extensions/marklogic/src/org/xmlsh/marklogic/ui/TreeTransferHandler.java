/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
final class TreeTransferHandler extends TransferHandler {
	/**
	 * 
	 */
	private final ExplorerShell mExplorerShell;

	/**
	 * @param explorerShell
	 */
	TreeTransferHandler(ExplorerShell explorerShell) {
		mExplorerShell = explorerShell;
	}

	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor ) ||
				!support.isDrop()) {
			return false;
		}

		JTree.DropLocation dropLocation = (JTree.DropLocation)support.getDropLocation();

		TreePath path = dropLocation.getPath();
		if( path == null )
			return true ;
		LazyTreeNode parentNode = (LazyTreeNode)(path.getLastPathComponent());

		if( parentNode.isDirectory() )
			return true ;

		return false ;
	}

	public boolean importData(TransferHandler.TransferSupport support) {
		JTree.DropLocation dropLocation =
				(JTree.DropLocation)support.getDropLocation();

		TreePath path = dropLocation.getPath();

		Transferable transferable = support.getTransferable();
		try {
			@SuppressWarnings("unchecked")
			List<File> files = (List<File>)  transferable.getTransferData(DataFlavor.javaFileListFlavor);
			if( path == null ){
			
				  mExplorerShell.doLoad( null , files );
				
				
				
			} else {
			   LazyTreeNode parentNode = (LazyTreeNode)(path.getLastPathComponent());

			   mExplorerShell.doLoad( parentNode , files );
			}
			return true ;
		} catch (Exception e) {
			mExplorerShell.printError( "Exception getting drop transfer data",e);
			return false ;
		}

	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#exportAsDrag(javax.swing.JComponent, java.awt.event.InputEvent, int)
	 */
	@Override
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		// TODO Auto-generated method stub
		super.exportAsDrag(comp, e, action);
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent, java.awt.datatransfer.Clipboard, int)
	 */
	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		super.exportToClipboard(comp, clip, action);
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		return 
		new Transferable() {
	        public DataFlavor[] getTransferDataFlavors() {
	            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
	        }
	        public boolean isDataFlavorSupported(DataFlavor flavor)
	        {
	            return flavor == DataFlavor.javaFileListFlavor;
	        }
	        public Object getTransferData(DataFlavor flavor)
	            throws UnsupportedFlavorException
	        {
	            if( flavor == DataFlavor.javaFileListFlavor ){
	            	   
				   TreePath selectedPath = TreeTransferHandler.this.mExplorerShell.mDirectoryTree.getSelectionPath();
				   if( selectedPath == null || selectedPath.getPathCount() < 1 )
					   return null ;
				   LazyTreeNode node = (LazyTreeNode)(selectedPath.getLastPathComponent());
				   String url = node.getUrl();
					   
					try {
					   return mExplorerShell.doStore(url);
					} catch (Exception e) {
						mExplorerShell.printError("Exception trying to store: " + url , e);
					}
	            }
	            return null ;
	        }
			
			
	    };
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
	 */
	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		// TODO: Delete temp files 
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