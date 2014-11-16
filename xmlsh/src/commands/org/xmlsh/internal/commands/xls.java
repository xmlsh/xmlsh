/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.internal.commands;



import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.HIDDEN_NAME;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.HIDDEN_SYS;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.SYSTEM;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.posix.commands.ls.ListVisitor;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.IPathTreeVisitor;
import org.xmlsh.util.PathMatchOptions;
import org.xmlsh.util.PathTreeVisitor;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.UnifiedFileAttributes;

import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.*;

import org.xmlsh.util.Util;
import org.xmlsh.util.XFile;

public class xls extends XCommand {


	private boolean opt_a = false ;
	private boolean opt_R = false ;
	private boolean opt_r = false ;
	private boolean opt_l = false ;
	private boolean opt_s = false ; // system


	@Override
	public int run(  List<XValue> args  )	throws Exception
	{
		Options opts = new Options("a=all,l=long,R=recurse,r=relative,s=system", SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();

		Path curDir = mShell.getCurPath();


		OutputPort stdout = getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
		writer.writeStartDocument();

		String sDocRoot = "dir";
		writer.writeStartElement(sDocRoot);
		writer.writeAttribute("pwd", FileUtils.toJavaPath(curDir.toString()));


		if( args == null )
			args = new ArrayList<XValue>();
		if( args.size() == 0 )
			args.add(XValue.newXValue(""));

		opt_l = opts.hasOpt("l");
		opt_a = opts.hasOpt("a");
		opt_R = opts.hasOpt("R");
		opt_r = opts.hasOpt("r");  // output relative to pwd path as path
		opt_s = opts.hasOpt("s");
		int ret = 0;
		for( XValue arg : args ){

			// Must go to Shell API to get raw files
			String sArg = arg.toString();
			Path path = getEnv().getShell().getPath(sArg);
			if( path == null ||  ! Files.exists(path, LinkOption.NOFOLLOW_LINKS) ){
				this.printErr("ls: cannot access " + sArg + " : No such file or directory" );
				ret++;
				continue;
			}

			FileUtils.walkPathTree(path,  
					opt_R , 
					new ListVisitor(writer),
					(new PathMatchOptions()).
					   withFlagsHidden( opt_a ? null  : HIDDEN_SYS , 
							   opt_a ? null  : HIDDEN_NAME ,
							   opt_s ? null : SYSTEM )
					
					);
			
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
		stdout.writeSequenceTerminator(serializeOpts);


		return ret;
	}


	public  class ListVisitor implements IPathTreeVisitor {

		XMLStreamWriter writer;

		public ListVisitor( XMLStreamWriter writer) {
			this.writer = writer;
		}


		@Override
		public FileVisitResult enterDirectory(Path root, Path directory,
				UnifiedFileAttributes attrs) {
			
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult exitDirectory(Path root, Path directory,
				UnifiedFileAttributes attrs) {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitDirectory(Path root, Path directory,
				UnifiedFileAttributes uattrs) throws IOException {
			mLogger.entry(root, directory, uattrs);
			if( root.equals(directory))
				return FileVisitResult.CONTINUE;
			try {
				(new XFile(directory,uattrs)).serialize(writer,opt_l, true, opt_r);
			} catch (XMLStreamException e) {
				Util.wrapIOException(e);
			}
			return FileVisitResult.CONTINUE;
		}

		
		@Override
		public FileVisitResult visitFile(Path root, Path path,
				UnifiedFileAttributes uattrs) throws IOException {
			mLogger.entry(root, path, uattrs);
			try {
				(new XFile(path,uattrs)).serialize(writer,opt_l, true, opt_r);
			} catch (XMLStreamException e) {
				Util.wrapIOException(e);
			}
			
			return FileVisitResult.CONTINUE ;
		}



		
		public void error(String s, Exception e) {
			printErr( s  , e);
			
		}



	}
/*
	class ListVisitor extends PathTreeVisitor {

		XMLStreamWriter writer;

		public ListVisitor(Path root, XMLStreamWriter  writer) {
			super(root, new PathMatchOptions(opt_R, opt_a, opt_s, false)  );
			this.writer = writer;
		}

		@Override
		public void visitFile( boolean root,Path path, BasicFileAttributes attrs) throws IOException{
			
			mLogger.entry(root, path, attrs);
			try {
				(new XFile(path,mRoot)).serialize(writer,opt_l, true, opt_r);
			} catch (XMLStreamException e) {
				Util.wrapIOException(e);
			}
		}
		@Override
		public void enterDirectory(boolean root,Path path, BasicFileAttributes attrs)throws IOException {
			
			mLogger.entry(root, path, attrs);
			try {
				if( ! root )
				(new XFile(path,mRoot)).serialize(writer,opt_l, false, opt_r );
			} catch (XMLStreamException e) {
				Util.wrapIOException(e);
			}

		}
		@Override
		public void exitDirectory(boolean root,Path dir )throws IOException {
			
			mLogger.entry(root, dir);
			try {
				if( ! root )
				writer.writeEndElement();
			} catch (XMLStreamException e) {
				Util.wrapIOException(e);
			}
		}



		@Override
		public void error(String s, Exception e) {
			printErr( s  , e);
			
		}






	}
*/
}

//
//
//Copyright (C) 2008-2014    David A. Lee.
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
