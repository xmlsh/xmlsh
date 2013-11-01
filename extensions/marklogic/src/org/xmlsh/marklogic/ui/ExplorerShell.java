/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreePath;

import org.xmlsh.core.Options;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ResultSequence;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class ExplorerShell {

	private JFrame mframe;
	private ExplorerOptions mOptions ;
	private BlockingQueue<MLRequest> mCommandQueue =  new ArrayBlockingQueue<MLRequest>(20, true);

	private File mCurdir;
	MLRequestThread mRequestThread;
	JTree mDirectoryTree;
	private MLTreeModel mDirectoryModel;
	SerializeOpts mSerializeOpts;
	private Shell mShell;

	private JLabel mStatus ;

	private File mTempDir;
	private JMenuItem mntmNext;
	private JMenuItem mntmPrev;
	private JButton mBtnConnect;
	private JComboBox mListDatabases;
	private boolean inConnect = false ;


	public void run( ) {
		try {
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) {
			// handle exception
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mframe.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ExplorerShell(Shell sh , Options opts) throws Exception {
		mShell = sh ;
		mCurdir = sh.getCurdir();
		mSerializeOpts = sh.getSerializeOpts(opts);

		mOptions = new ExplorerOptions( opts.getOptString("connect", sh.getEnv().getVarString("MLCONNECT")));
		QueryCache.init(this);
		initialize();
	}



	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		mframe = new JFrame();
		mframe.setTitle("MarkLogic Explorer");
		mframe.setBounds(100, 100, 559, 445);
		mframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		mframe.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOptions = new JMenuItem("Options...");
		mntmOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OptionsDialog dialog = new OptionsDialog(mOptions);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		});
		mnFile.add(mntmOptions);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);

		JPanel panel = new JPanel();
		mframe.getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{87, 286, 0};
		gbl_panel.rowHeights = new int[]{23, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		mBtnConnect = new JButton("Connect...");
		mBtnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				reconnect();
			}
		});
		mBtnConnect.setMinimumSize(new Dimension(87, 20));
		mBtnConnect.setMargin(new Insets(1, 12, 1, 12));
		mBtnConnect.setMaximumSize(new Dimension(87, 20));
		GridBagConstraints gbc_btnConnect = new GridBagConstraints();
		gbc_btnConnect.anchor = GridBagConstraints.WEST;
		gbc_btnConnect.insets = new Insets(0, 0, 0, 5);
		gbc_btnConnect.gridx = 0;
		gbc_btnConnect.gridy = 0;
		panel.add(mBtnConnect, gbc_btnConnect);
		mBtnConnect.setHorizontalAlignment(SwingConstants.LEFT);
		
		mListDatabases = new JComboBox();
		mListDatabases.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if( inConnect )
					return ;
				if (event.getStateChange() == ItemEvent.SELECTED) {
			          Object item = event.getItem();
			          if( item instanceof String && ! Util.isBlank((String)item) ){
			        	  mOptions.mDatabase = (String) item ;
			        	  reconnect();
			        	  reconnect();
			          }
			       }
			}
		});
		GridBagConstraints gbc_listDatabases = new GridBagConstraints();
		gbc_listDatabases.fill = GridBagConstraints.BOTH;
		gbc_listDatabases.gridx = 1;
		gbc_listDatabases.gridy = 0;
		panel.add(mListDatabases, gbc_listDatabases);

		JLabel lblStatus = mStatus= new JLabel("Status");
		mframe.getContentPane().add(lblStatus, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane();
		mframe.getContentPane().add(scrollPane, BorderLayout.CENTER);

		JTree tree = new JTree(  );
		tree.setDropMode(DropMode.ON);
		tree.setDragEnabled(true);
		tree.setRootVisible(false);
		tree.setTransferHandler( new TreeTransferHandler(this) );
		scrollPane.setViewportView(tree);


		mDirectoryTree = tree ;
		mDirectoryTree.setModel( mDirectoryModel = new MLTreeModel(new LazyTreeNode(), tree, this) );

		JPopupMenu popupMenu = new JPopupMenu();
		
		addPopup(tree, popupMenu);

		final JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					onOpen();
				} catch (Exception e1) {
					printError("Exception opening file",e1);
				}
			}
		});

		final JMenuItem mntmRefresh = new JMenuItem("Refresh...");
		mntmRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onRefresh();
			}
		});
		
		mntmNext = new JMenuItem("Next 1000...");
		mntmNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onNext();
			}
		});
		popupMenu.add(mntmNext);
		
		mntmPrev = new JMenuItem("Prev 1000...");
		mntmPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onPrev();
			}
		});
		popupMenu.add(mntmPrev);
		popupMenu.add(mntmRefresh);
		
		final JMenuItem mntmRename = new JMenuItem("Rename...");
		mntmRename.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		popupMenu.add(mntmRename);
		popupMenu.add(mntmOpen);

		final JMenuItem mntmLoad = new JMenuItem("Load...");
		mntmLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					onLoad();
				} catch (InterruptedException e1) {
					printError( "Exception loading documents",e1);

				}
			}
		});
		popupMenu.add(mntmLoad);

		final JMenuItem mntmSaveAs = new JMenuItem("Save As...");
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onSaveAs();
			}
		});
		
		final JMenuItem mntmDelete = new JMenuItem("Delete...");
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					onDelete();
				} catch (InterruptedException e1) {
					printError( "Exception deleting documents",e1);
				}
			}

		});
		popupMenu.add(mntmDelete);
		popupMenu.add(mntmSaveAs);
		
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				int nsel = mDirectoryTree.getSelectionCount();
				boolean isDir = false ;
				for( String u : getSelectedUrls() )
					if( u.endsWith("/"))
						isDir=true;
				
				mntmDelete.setEnabled(nsel > 0);
				mntmOpen.setEnabled(nsel == 1 && ! isDir );
				mntmRename.setEnabled(nsel == 1 && ! isDir  );
				mntmLoad.setEnabled( nsel == 0 || ( nsel == 1 && isDir ) );
				mntmSaveAs.setEnabled( nsel > 0 );
				mntmRefresh.setEnabled( nsel == 0 || isDir );
				mntmNext.setText("Next " + mOptions.mMaxRows + "...");
				mntmPrev.setText("Prev " + mOptions.mMaxRows + "...");
		
				if( nsel == 1 ){
					TreePath path = mDirectoryTree.getSelectionPath();
					LazyTreeNode node = (LazyTreeNode) path.getLastPathComponent();
					mntmNext.setEnabled( node.isDirectory() && ( node.getStart() != node.getEnd() ));
					mntmPrev.setEnabled( node.isDirectory() && node.getStart() > 1 );
					
				} else {
				mntmNext.setEnabled(false);
				mntmPrev.setEnabled(false);
				
			  }
			}
		});
	}

	protected void onRefresh() {
				
		if( mDirectoryTree.getSelectionCount() > 0 ){
			for( TreePath p : mDirectoryTree.getSelectionPaths() ){
				LazyTreeNode parentNode = (LazyTreeNode) p.getLastPathComponent();
				if( parentNode.isDirectory()){
					
				    mDirectoryModel.postReload(parentNode);
				}
			}
		} else
			mDirectoryModel.loadFirstLevel();
	}
	
	
	protected void onNext() {
		
		TreePath p = mDirectoryTree.getSelectionPath();
		if( p != null ){
			LazyTreeNode parentNode = (LazyTreeNode) p.getLastPathComponent();
			if( parentNode.isDirectory())
				mDirectoryModel.postNext( parentNode , mOptions.mMaxRows );
			
		}
	
	}
	
	protected void onPrev() {
		
		TreePath p = mDirectoryTree.getSelectionPath();
		if( p != null ){
			LazyTreeNode parentNode = (LazyTreeNode) p.getLastPathComponent();
			if( parentNode.isDirectory()){
				if( parentNode.getStart() > 1 )
				     mDirectoryModel.postPrev( parentNode , mOptions.mMaxRows );
			}
			
		}
	
	}
	
	
	
	
	
	private void onDelete() throws InterruptedException {
		if( mDirectoryTree.getSelectionCount() > 0 ){
			if( confirmYesNo("Delete Selected Files?")){
				doDelete( getSelectedUrls() );
				Set<LazyTreeNode> parents = new HashSet<LazyTreeNode>();
				
				
				for( TreePath p : mDirectoryTree.getSelectionPaths() ){
					parents.add((LazyTreeNode) ((LazyTreeNode)p.getLastPathComponent()).getParent() );
				}
				
				for( LazyTreeNode p : parents )
				   mDirectoryModel.postReload( p );
							

			}
		}
		
	}

	private List<String> getSelectedUrls() {
		List<String>  list = new ArrayList<String>();

		if( mDirectoryTree.getSelectionCount() > 0 ){
			for( TreePath p : mDirectoryTree.getSelectionPaths() ){
				list.add(  ((LazyTreeNode) p.getLastPathComponent()).getUrl() );
			}
		}			
		return list;

	}

	private boolean confirmYesNo(String string) {
		return JOptionPane.showConfirmDialog(mframe, string, "Confirm", JOptionPane.YES_NO_OPTION ) ==  JOptionPane.YES_OPTION;
	}

	List<File> doStore(final String url) throws Exception {

		
       try {
	        mframe.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		final Semaphore sem = new Semaphore(1);
		sem.acquire();

		if( url.endsWith("/")) { // Directory
			String name = url.substring( 0 , url.lastIndexOf('/'));  // strip trailing slash
			name = name.substring(name.lastIndexOf('/') + 1 );
			final File tmpDir = getTempFile(name);
			tmpDir.mkdirs();

			MLListDirectoryRequest dirRequest =  new MLListDirectoryRequest( url  ) {
				@Override
				void onComplete(ResultSequence rs) throws Exception {
					final String children[] = rs.asStrings();
						for( String child_url : children ){

							MLGetRequest getRequest;
							File outFile = new File( tmpDir , child_url.substring(url.length()));
							File outDir = outFile.getParentFile();
							try {
								if( ! outDir.exists())
									outDir.mkdirs();
								
								getRequest = new MLGetRequest(	child_url , outFile,mSerializeOpts);
								getRequest.run( mRequestThread );
							} catch (Exception e) {
								printError("Exception getting: " + child_url + " to local file: " + outFile.getAbsolutePath() , e );
								 
							}

						}
					sem.release();
				}

			} ;

			putCommand( dirRequest );
			sem.acquire();
			return Arrays.asList(tmpDir);
		}
		else {
			String name = url.substring(url.lastIndexOf('/') + 1 );
			File tmpFile = getTempFile(name);

			MLGetRequest r =  new MLGetRequest(url, tmpFile, mSerializeOpts) {
			
				void onComplete( ResultSequence rs) throws Exception {
					super.onComplete(rs);
					sem.release();
				}
			
			};
			putCommand( r );
			sem.acquire();

			return Arrays.asList(tmpFile);
		}
       } finally {
	        mframe.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

       }
	}	

	void doLoad(final LazyTreeNode parentNode, List<File> files) throws InterruptedException {
        try {
			mframe.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			
			for( File f : files ){
				List<Content>  c = getContents( parentNode.getUrl(), f  );
				if( c == null || c.size() == 0 )
					continue ;

				int start = 0;
				int end = c.size();
				int n =  mOptions.mBatchSize ;
				while( start < end ){
					if( start + n >= end )
						n = end - start  ;
					
					List<Content> batch = c.subList(start, start + n);
					
					final boolean last = (start + n ) >= end;
							
				    putCommand( new MLPutRequest( batch.toArray( new Content[batch.size()])){ 

						public void onComplete(boolean bSuccess) {

							if( last )
							   mDirectoryModel.postReload(parentNode);
						}
					});	
					
					start += n ;
					n =  mOptions.mBatchSize ;
				}
				
				
			}
		}finally {
	        mframe.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		}
	}

	private List<Content> getContents(String parentUri , File f) {
		List<Content> list = new ArrayList<Content>();
		String fileUri = parentUri + f.getName();
		if( f.isDirectory() ){
			for( File child : f.listFiles() ){
				String name = child.getName() ;
				if( name.equals(".") || name.equals(".."))
					continue ;
				list.addAll( getContents(  fileUri + "/" , child ) );
			}
		}
		else
			if( f.isFile() && f.canRead() ){
				list.add( ContentFactory.newContent(fileUri , f, getCreateOptions() ) ) ;
			} 

		return list;


	}

	private ContentCreateOptions getCreateOptions() {
		return new ContentCreateOptions();
	}

	protected void onSaveAs() {
		// TODO Auto-generated method stub

	}

	protected void onLoad() throws InterruptedException {

		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setCurrentDirectory( mCurdir );
		if( fc.showDialog(mframe, "Load") == JFileChooser.APPROVE_OPTION  ){
			File[] files = fc.getSelectedFiles();
			LazyTreeNode node = (LazyTreeNode) mDirectoryTree.getSelectionPath().getLastPathComponent() ;
			
			doLoad( node , Arrays.asList( files ) );
			
			
		}
		
		
		
		
	}

	protected void onOpen() throws Exception {
		String url = getSelectedUrls().get(0);
		List<File> files = doStore( url );
		
		Desktop.getDesktop().edit(files.get(0));
		
		

	}

	void printError(String s, Exception e) {
		mShell.printErr(s,e);

	}


	public Shell getShell() {
		return mShell;
	}


	
	private void reconnect()
	{
		if( mRequestThread != null ){
			mRequestThread.close();
			mRequestThread = null ;
			this.mBtnConnect.setText("Connect...");
			mListDatabases.removeAllItems();

			mDirectoryModel.reset();

		} else {


			mRequestThread = new MLRequestThread( this ,  mOptions  , mCommandQueue  );
			mRequestThread.start();
	
			mDirectoryModel.loadFirstLevel();
			this.mBtnConnect.setText("Disconnect...");
			
			try {
			  refreshDatabases();
			} catch( Exception e )
			{
				printError("Exception refreshing databases",e);
				
			}
			

	 
		}

	}
	private void refreshDatabases() throws InterruptedException {

		putCommand( new MLQueryRequest( "Getting Databases ..." , 
				QueryCache.getInstance().getQuery("listDatabases.xquery"),
				null 
				){  


				@Override
				void onComplete(ResultSequence rs) throws Exception {
					final String[] dbs = rs.asStrings();
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							inConnect = true ;
							mListDatabases.removeAllItems();
							

							boolean first = true ;
							String selected = null ;
							for( String db : dbs )
							{
							   if( ! first ){
							      mListDatabases.addItem( db );
							      if( db.equals( dbs[0]))
							    	  selected =db ;
							   }
							   first = false ;
							   
							}

							mListDatabases.setSelectedItem(selected);
							inConnect = false ;
							   
						}
					});	
					
					
					
				}
			});
		
		
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	public void setStatus(final String operation) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				mStatus.setText(operation);

			}
		});	
	}
	
	File getTempFile(String name) throws IOException {
		if( mTempDir == null ){
			mTempDir = File.createTempFile("mlui", null );
			mTempDir.delete();
			mTempDir.mkdir();
			mTempDir.deleteOnExit();
		}
		File f = new File( mTempDir , name );
		if( f.exists() )
			f.delete();
		f.deleteOnExit();
		return f;
	}

	private void doDelete( List<String> urls ) throws InterruptedException {
		
		List<MLRequest>  deletes = new ArrayList<MLRequest>( urls.size());
		
		for( String url : urls )
			deletes.add( new MLDeleteRequest(url) );
		
		final Semaphore sem = new Semaphore(1);
		sem.acquire();
		
        mframe.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		putCommand( new MLMultiRequest( "Deleting documents ..." , deletes ){  

				@Override
				void onComplete() {
					sem.release();
					
				}
			});
		sem.acquire();
        mframe.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}
	

	boolean putCommand(MLRequest command) {
		try {
			mCommandQueue.put(command);
		} catch (InterruptedException e) {
			printError("Interrupted putting command to command queue" , e);
			return false;
		}
		return true;
	}

	/**
	 * @return the options
	 */
	protected ExplorerOptions getOptions() {
		return mOptions;
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