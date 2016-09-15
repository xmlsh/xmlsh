package org.xmlsh.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;

public interface IPathTreeVisitor {

	FileVisitResult enterDirectory( Path root , Path directory, UnifiedFileAttributes attrs );
	FileVisitResult exitDirectory( Path root , Path directory, UnifiedFileAttributes attrs  );
	FileVisitResult visitDirectory( Path root , Path directory, UnifiedFileAttributes attrs ) throws IOException;
	FileVisitResult visitFile( Path root , Path file, UnifiedFileAttributes uattrs ) throws IOException;
	
	
}
