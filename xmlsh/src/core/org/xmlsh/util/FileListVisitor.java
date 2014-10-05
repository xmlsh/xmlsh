package org.xmlsh.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class FileListVisitor extends SimpleFileVisitor<Path> 
{
	protected Path mRoot;
	protected PathMatchOptions mOptions;
	static Logger mLogger = LogManager.getLogger();
	public abstract void error( String s , Exception e );
	public abstract void visitFile( boolean root,Path dir, BasicFileAttributes attrs)throws IOException;
	public abstract void enterDirectory(boolean root,Path dir, BasicFileAttributes attrs)throws IOException;
	public abstract void exitDirectory(boolean root,Path dir )throws IOException;

	public FileListVisitor(Path root, PathMatchOptions options ) {
		
		mLogger.entry(root, options );
		mRoot = root;
	 
		mOptions = options;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) throws IOException {
		
		mLogger.entry(dir, attrs);
		boolean isRoot = dir.equals(mRoot);
 

		boolean bVisit = mOptions.doVisit(  dir  );
		boolean bExit = false ;
		if( bVisit ){
			try {
				bExit = true;
				enterDirectory(isRoot , dir, attrs);
				if( ! isRoot && ! mOptions.mRecursive )
					return mLogger.exit( FileVisitResult.SKIP_SUBTREE);
				bExit = false ;

			} catch (Exception e) {
				mLogger.catching(e);
				error("Exception attempting to list file: "
						+ dir.toString(), e);
			
		   } finally {

			   if( bExit )
				try {
					exitDirectory( isRoot  , dir );
				} catch (Exception e) {
					mLogger.catching(e);
					error("Exception attempting to list file: "
							+ dir.toString(), e);
				}
		   }
		} else
			return mLogger.exit(FileVisitResult.SKIP_SUBTREE);

		return mLogger.exit(FileVisitResult.CONTINUE);
	}


	@Override
	public
	final FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {

		mLogger.entry(file, attrs);		
		boolean isRoot = file.equals(mRoot);
		boolean bVisit = mOptions.doVisit(  file  );

			if( bVisit )
			  visitFile(isRoot ,  file, attrs);
			return mLogger.exit(FileVisitResult.CONTINUE);
	}

	@Override
	final public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException {
		
		mLogger.entry(file, exc);
		if( exc != null && exc instanceof java.nio.file.AccessDeniedException )
			mLogger.info("Access denied accessing {} " , file , exc );
		else
		   error("Exception attempting to list file: " + file.toString(),	exc);
		return mLogger.exit( FileVisitResult.CONTINUE);
	}

	@Override
	final  public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		
		mLogger.entry(dir, exc);
	     	boolean isRoot = dir.equals(mRoot);
				  exitDirectory( isRoot , dir );
				 return mLogger.exit( FileVisitResult.CONTINUE);
	}

}