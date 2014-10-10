package org.xmlsh.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.nio.file.FileVisitResult.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.util.UnifiedFileAttributes.PathMatchOptions;


/* 
 * Similar to FileTreeWalker but implements breadth-first walking then sorting then depth-first visiting
 * 
 */
public class PathTreeWalker {
	
	private Path mRoot ;
	private Comparator<UnifiedFileAttributes> mSort ;
	private LinkOption[] linkOptions = FileUtils._pathNoFollowLinks;
	private PathMatchOptions  mMatchOptions;
	private boolean mRecursive;

	
	static Logger mLogger = LogManager.getLogger();
	public PathTreeWalker( Path root , boolean recursive , PathMatchOptions options ,  Comparator<UnifiedFileAttributes> sort   ){
		mRoot = root ;
		mMatchOptions = options ;
		mRecursive = recursive ;
		mSort = sort ;
		
	}
	public PathTreeWalker( Path root,   boolean recursive , PathMatchOptions options ){
		this( root , recursive  , options , new Comparator<UnifiedFileAttributes>() {
			final Comparator<Path> pathComparator = FileUtils.alphaPathComparator();
			@Override
			public int compare(UnifiedFileAttributes o1, UnifiedFileAttributes o2) {
				return pathComparator.compare(o1.getPath() , o2.getPath() );
			} 
			
		}); 
	}
    
	
    protected <V extends IPathTreeVisitor> FileVisitResult walk(Path start, V visitor, int depth ) throws IOException {

    	try {
    		
    		BasicFileAttributes attrs = FileUtils.getBasicFileAttributes(start, false);
    		if( attrs == null )
    			return FileVisitResult.CONTINUE;
       	   
    	    UnifiedFileAttributes uattrs = FileUtils.getUnifiedFileAttributes(start, attrs , false );
       	    
    	    // Start is a file not a directory 
    	    if( depth == 0 &&  ! uattrs.isDirectory()){
    	    	// Show exact matches on root reguardless of the options
       	    	    return visitor.visitFile(mRoot, start , uattrs );
       	    }
       	    
    		List<UnifiedFileAttributes>  paths = new ArrayList<UnifiedFileAttributes>();
    		
    		try ( DirectoryStream<Path>  ds =  Files.newDirectoryStream(start) ){
			    for (Path path : ds) {
		    	    UnifiedFileAttributes ua = FileUtils.getUnifiedFileAttributes(path, false );
			    	if( mMatchOptions.doVisit(path, ua ))
			    		paths.add( ua );
			    }
    		}     		

    		Collections.sort(paths, mSort);
			FileVisitResult result = CONTINUE  ;
			
    		// NOW visit the sorted lists
    		for( UnifiedFileAttributes info :    paths ){

				if( info.isDirectory()){
    				result = visitor.enterDirectory(mRoot, info.getPath() , info);
				    if( result == CONTINUE ){
				    	result = visitor.visitDirectory(mRoot, info.getPath(), info);
				    	if(  result == CONTINUE && mRecursive ){
				    		result = walk(info.getPath() , visitor , depth + 1 );
				    	}
				    	result = visitor.exitDirectory(mRoot, info.getPath(), info);
				    } else
					if( result == SKIP_SUBTREE )
						break ;
				} else {
	       	    	if( mMatchOptions.doVisit(info.getPath(), info))
					   result = visitor.visitFile(mRoot, info.getPath(), info);
				}
				if( result == TERMINATE)
					return result ;
				if( result == SKIP_SIBLINGS )
					break ;

    		}
       	    return result ;
        }
    	catch (IOException x ) {
    		mLogger.catching(x);
    		return FileVisitResult.CONTINUE;
    	
    	}
    	catch (SecurityException x) {
    		return FileVisitResult.CONTINUE;
    	}
      
    	
    	
    }
    
    
	public <V extends IPathTreeVisitor> FileVisitResult walk( V visitor ) throws IOException {
		return walk( mRoot , visitor , 0 );
	
	}
	

}
