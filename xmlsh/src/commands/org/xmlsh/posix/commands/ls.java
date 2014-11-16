/**
 * $Id: ls.java 346 2009-12-03 13:14:51Z daldei $
 * $Date: 2009-12-03 08:14:51 -0500 (Thu, 03 Dec 2009) $
 * 
 */

package org.xmlsh.posix.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import static java.nio.file.attribute.PosixFilePermission.*;
import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.internal.commands.xls.ListVisitor;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.IPathTreeVisitor;
import org.xmlsh.util.PathMatchOptions;
import org.xmlsh.util.PathTreeVisitor;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.UnifiedFileAttributes;

import static org.xmlsh.util.UnifiedFileAttributes.MatchFlag.*;

import org.xmlsh.util.Util;
import org.xmlsh.util.XFile;


/**
 * Posix command ls
 * Rewritten from xquery to native java so that it can stream large lists efficiently
 * 
 * @author David A. Lee
 */

public class ls extends XCommand {

	private boolean opt_a = false;
	private boolean opt_R = false;
	private boolean opt_l = false;
	private boolean opt_s = false ; // system

	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts = new Options("a=all,l=long,R=recurse,r,s=system", SerializeOpts
				.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();

		OutputPort stdout = getStdout();
		SerializeOpts serializeOpts = getSerializeOpts(opts);
		PrintWriter writer = stdout.asPrintWriter(serializeOpts);

		if (args == null)
			args = new ArrayList<XValue>();
		if (args.size() == 0)
			args.add(XValue.newXValue(""));

		opt_l = opts.hasOpt("l");
		opt_a = opts.hasOpt("a");
		opt_R = opts.hasOpt("R") || opts.hasOpt("r");
		opt_s = opts.hasOpt("s");

		int ret = 0;
		for (XValue arg : args) {

			// Must go to Shell API to get raw files
			String sArg = arg.toString();
			File dir = getEnv().getShell().getFile(sArg);
			if (dir == null || !dir.exists()) {
				printErr("ls: cannot access " + sArg
						+ " : No such file or directory");
				ret++;
				continue;
			}
			
			Path pdir = FileUtils.asValidPath(dir);
			if( pdir == null ){
				printErr("ls: cannot access " + sArg
						+ " : No such file or directory");
				ret++;
				continue;
			}
			
			FileUtils.walkPathTree(pdir,
					opt_R , 
					new ListVisitor(writer),
					(new PathMatchOptions()).
					   withFlagsHidden( opt_a ? null  : HIDDEN_SYS , 
							   opt_a ? null  : HIDDEN_NAME ,
							   opt_s ? null : SYSTEM )

					
					);

		}
		// writer.write(serializeOpts.getSequence_term());
		writer.close();

		return ret;
	}

	public  class ListVisitor implements IPathTreeVisitor {

		PrintWriter writer;

		public ListVisitor( PrintWriter writer) {
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
				UnifiedFileAttributes attrs) throws IOException {
			if( root.equals(directory))
				return FileVisitResult.CONTINUE;
			if (opt_l)
				writeFlags(directory, attrs);
			writer.write(FileUtils.toJavaPath(root.relativize(directory).toString()));
			writer.write(getSerializeOpts().getSequence_sep());
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path root, Path path,
				UnifiedFileAttributes uattrs) throws IOException {
			if (opt_l)
				writeFlags(path, uattrs);
			if( path.equals(root))
			   writer.write(FileUtils.toJavaPath(path.getFileName().toString()));
			else
				writer.write(FileUtils.toJavaPath(root.relativize(path).toString()));

			
			writer.write(getSerializeOpts().getSequence_sep());
			return FileVisitResult.CONTINUE ;
		}


		private void writeFlags(Path path, UnifiedFileAttributes uattrs)
				throws IOException {
			StringBuffer flags = new StringBuffer();
			Set<PosixFilePermission> perms = FileUtils.getPosixFilePermissions(path,LinkOption.NOFOLLOW_LINKS);
			

			flags.append(uattrs.isDirectory() ? "d" : "-");
			flags.append( PosixFilePermissions.toString( perms ) );
			flags.append(" ");

			long len = uattrs.size();
			String slen = String.valueOf(len);
			slen = String.format("%1$10s", slen);
			flags.append(slen);
			flags.append(" ");

			String sDate = String.format("%1$tF %1$tT", new Date(Files
					.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS)
					.toMillis()));
			flags.append(sDate);
			flags.append(" ");
			writer.write(flags.toString());

		}

		
		public void error(String s, Exception e) {
			printErr( s  , e);
			
		}



	}

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the "License");
// you may not use this file except in compliance with the License. You may obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
// Contributor(s): none.
//
