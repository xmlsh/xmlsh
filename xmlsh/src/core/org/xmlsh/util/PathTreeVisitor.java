package org.xmlsh.util;

 
/*
 * A Breadth first sortable FileVisitor  like FileVisitor
 * 
 */
public abstract class PathTreeVisitor    
{
	
	/*
	static Logger mLogger = LogManager.getLogger();
	
	protected void error( String s , Exception e ) {
		mLogger.error(s,e);
	}
	protected abstract void visitFile( Path root , Path dir, BasicFileAttributes attrs)throws IOException;
	protected abstract void visitDirectory( Path root , Path dir, BasicFileAttributes attrs)throws IOException;

	protected PathTreeVisitor(Path root,  PathMatchOptions options ) {
		
		mLogger.entry(root, options );
		mRoot = root;
		mOptions = options;
	}

	
	

	public final FileVisitResult preVisitDirectory(Path dir,
			BasicFileAttributes attrs) throws IOException {
		
		mLogger.entry(dir, mCurrentDepth , attrs);
		boolean isRoot = mCurrentDepth == 0;
 

		boolean bVisit = isRoot || mOptions.doVisit(  dir  );
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

		mCurrentDepth++;
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
		
		mLogger.entry(file, mCurrentDepth, exc);
		if( exc != null && exc instanceof java.nio.file.AccessDeniedException )
			mLogger.info("Access denied accessing {} " , file , exc );
		else
		   error("Exception attempting to list file: " + file.toString(),	exc);
		return mLogger.exit( FileVisitResult.CONTINUE);
	}

	@Override
	final  public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		
		mLogger.entry(dir, mCurrentDepth , exc);
		assert( mCurrentDepth > 0 );
		mLogger.entry(dir, exc);
	    boolean isRoot = --mCurrentDepth == 0;
				  exitDirectory( isRoot , dir );
				 return mLogger.exit( FileVisitResult.CONTINUE);
	}
	
	*/

}