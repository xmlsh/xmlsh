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
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.annotations.Command;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.FileInputPort;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.internal.commands.xls.ListVisitor;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.FileListVisitor;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.PathMatchOptions;
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
	private static final String sPwdRel = "pwd-relpath";
	private static final String sRootRel = "relpath";
	private static String sDocRoot = "xmd5";
	private Path curDir;
	private boolean opt_a;
	private boolean opt_s;

	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts = new Options("b=binary,x=xml,a=all,s=system", SerializeOpts
				.getOptionDefs());
		opts.parse(args);
		args = opts.getRemainingArgs();
		curDir = getCurdir().toPath();

		XMLStreamWriter out = null;
		OutputPort stdout = mShell.getEnv().getStdout();

		setSerializeOpts(opts);
		opt_a = opts.hasOpt("a");
		opt_s = opts.hasOpt("s");
		
		
		out = stdout.asXMLStreamWriter(getSerializeOpts());
		out.writeStartDocument();
		out.writeStartElement(sDocRoot);
		out.writeAttribute("pwd",curDir.toString());

		if (args.isEmpty())
			args.add(XValue.newXValue("-"));

		for (XValue arg : args) {

			String sArg = arg.toString();
			if( sArg.equals("-") ||  Util.tryURL(sArg) != null )
				  writeMD5( getInput(arg), sArg,  null, null , null , out );

			else {
			   File dir = getEnv().getShell().getFile(sArg);
				Files.walkFileTree(dir.toPath(), new ListVisitor(dir.toPath(),
						out));
			} 
			
			 

		}
		out.writeEndElement();
		out.writeEndDocument();
		out.flush();
		out.close();

		stdout.writeSequenceTerminator(getSerializeOpts());

		return 0;

	}

	
	



	class ListVisitor extends FileListVisitor {

		XMLStreamWriter writer;

		public ListVisitor(Path root, XMLStreamWriter  writer) {
			super(root, new PathMatchOptions(true, opt_a, opt_s, false));
			this.writer = writer;
		}

		@Override
		public void visitFile( boolean root,Path path, BasicFileAttributes attrs)throws IOException {
			if( ! attrs.isRegularFile() ){

				printErr("Not a regular file: " + path.toString());
				return ; 
			}
			if( ! Files.isReadable( path )){
				
				printErr("File not readable:  " + path.toString());
				return ; 
			}
						
			
			try ( InputPort port = new FileInputPort( path.toFile() ) ){
			   writeMD5(  port , path.getFileName().toString() ,  path.toString() , 
					   curDir.relativize(path).toString() ,
					   root ? null : mRoot.relativize(path).toString() , 
					    writer );
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

		

		
	private void writeMD5(InputPort inp , String name , String path , String pwdRel , String rootRel ,  XMLStreamWriter out ) throws CoreException, IOException,
			XMLStreamException {

		try (InputStream in = inp.asInputStream(getSerializeOpts())) {
			Checksum cs = Checksum.calcChecksum(in);
			out.writeStartElement(sFile);
			if (!Util.isBlank(name))
				out.writeAttribute(sName, FileUtils.toJavaPath(name));
			if (!Util.isBlank(sPath))
				out.writeAttribute(sPath, FileUtils.toJavaPath(path));
			if (!Util.isBlank(pwdRel))
				out.writeAttribute(sPwdRel, FileUtils.toJavaPath(pwdRel));
			if (!Util.isBlank(rootRel))
				out.writeAttribute(sRootRel, FileUtils.toJavaPath(rootRel));
			
			
			out.writeAttribute(sMd5, cs.getMD5());
			out.writeAttribute(sLen, String.valueOf(cs.getLength()));
			out.writeEndElement();

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
