/**
 * $Id: xfile.java 356 2010-01-01 15:46:31Z daldei $
 * $Date: 2010-01-01 10:46:31 -0500 (Fri, 01 Jan 2010) $
 * 
 */

package org.xmlsh.internal.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.annotations.Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.FileInputPort;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.internal.commands.xls.ListVisitor;
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
import org.xmlsh.util.commands.Checksum;

@Command
public class xmd5sum extends XCommand {

	private static final String sFile = "file";
	private static final String sName = "name";
	private static final String sMd5 = "md5";
	private static final String sLen = "length";
	private static final String sPath = "path";
	private static String sDocRoot = "xmd5";
	private boolean opt_a;
	private boolean opt_s;
	private boolean opt_r;

	@Override
	public int run(List<XValue> args) throws Exception {
		
		mLogger.entry(args);
		Options opts = new Options("b=binary,x=xml,a=all,s=system,r=relative", SerializeOpts
				.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();

		XMLStreamWriter out = null;
		OutputPort stdout = mShell.getEnv().getStdout();

		setSerializeOpts(opts);
		opt_a = opts.hasOpt("a");
		opt_s = opts.hasOpt("s");
		opt_r = opts.hasOpt("r");
		
		
		out = stdout.asXMLStreamWriter(getSerializeOpts());
		out.writeStartDocument();
		out.writeStartElement(sDocRoot);
		

		if (args.isEmpty())
			args.add(XValue.newXValue("-"));
		//else
		//  out.writeAttribute("pwd",FileUtils.toJavaPath(curDir.toString()));

		for (XValue arg : args) {

			String sArg = arg.toString();
			if( sArg.equals("-") ||  Util.tryURL(sArg) != null )
				  writeMD5( getInput(arg), sArg, null,  out );


			else {

				Path path = getEnv().getShell().getPath(sArg);
				if( path == null ||  ! Files.exists(path, LinkOption.NOFOLLOW_LINKS) ){
					this.printErr("xmd5sum: cannot access " + sArg + " : No such file or directory" );
					continue;
				}
			
				

				FileUtils.walkPathTree(path,  
						true , 
						new ListVisitor(out),
						(new PathMatchOptions()).
						   withFlagsHidden( opt_a ? null  : HIDDEN_SYS , 
								   opt_a ? null  : HIDDEN_NAME ,
								   opt_s ? null : SYSTEM )
						
						);
			
			} 
			
			 

		}
		out.writeEndElement();
		out.writeEndDocument();
		out.flush();
		out.close();

		stdout.writeSequenceTerminator(getSerializeOpts());

		return mLogger.exit(0);

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
				return FileVisitResult.CONTINUE;
		}

		
		@Override
		public FileVisitResult visitFile(Path root, Path path,
				UnifiedFileAttributes attrs) throws IOException {
			mLogger.entry(root, path, attrs);
			
			if( ! attrs.isRegularFile() ){

				printErr("Not a regular file: " + path.toString());
				return FileVisitResult.CONTINUE ; 
			}
			if( ! Files.isReadable( path )){
				
				printErr("File not readable:  " + path.toString());
				return FileVisitResult.CONTINUE;
			}
						
			
			try ( InputPort port = new FileInputPort( path.toFile() ) ){
				XFile xf = new XFile(path,root,attrs);
				
			   writeMD5(  port , xf.getName(), xf , writer );
			} catch (CoreException | XMLStreamException e) {
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
			super(root, new PathMatchOptions(true, opt_a, opt_s, false));
			this.writer = writer;
		}

		@Override
		public void visitFile( boolean root,Path path, BasicFileAttributes attrs)throws IOException {
			
			mLogger.entry(root, path, attrs);
			if( ! attrs.isRegularFile() ){

				printErr("Not a regular file: " + path.toString());
				return ; 
			}
			if( ! Files.isReadable( path )){
				
				printErr("File not readable:  " + path.toString());
				return ; 
			}
						
			
			try ( InputPort port = new FileInputPort( path.toFile() ) ){
				XFile xf = new XFile(path,mRoot);
				
			   writeMD5(  port , xf.getName(), xf , writer );
			} catch (CoreException | XMLStreamException e) {
				Util.wrapIOException(e);
			}
		}
		@Override
		public void enterDirectory(boolean root,Path path, BasicFileAttributes attrs)throws IOException {

		}
		@Override
		public void exitDirectory(boolean root,Path dir )throws IOException {
		}



		@Override
		public void error(String s, Exception e) {
			printErr( s  , e);
			
		}



	}

		*/

		
	private void writeMD5(InputPort inp ,String name , XFile xf ,   XMLStreamWriter out ) throws CoreException, IOException,
			XMLStreamException {

		mLogger.entry(inp, xf,  out);
		try (InputStream in = inp.asInputStream(getSerializeOpts())) {
			Checksum cs = Checksum.calcChecksum(in);
			out.writeStartElement(sFile);
			out.writeAttribute(sName, name);
			if( opt_r){
			  if (xf != null && !Util.isBlank(xf.getPwdRelativeName()))
				 out.writeAttribute(sPath,xf.getPwdRelativeName());
			} else
			{
				  if (xf != null && !Util.isBlank( xf.getPath()))
						 out.writeAttribute(sPath, xf.getPath());
			
			}
			
			out.writeAttribute(sMd5, cs.getMD5());
			out.writeAttribute(sLen, String.valueOf(cs.getLength()));
			out.writeEndElement();

		}
		
		mLogger.exit();

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
