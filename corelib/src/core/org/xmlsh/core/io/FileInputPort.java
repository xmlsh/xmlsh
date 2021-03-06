/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.FileUtils;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.databind.JsonNode;

public class FileInputPort extends InputPort
{


	private		static 	Logger	mLogger = LogManager.getLogger( FileOutputPort.class );				

	private		StreamInputPort		mStreamPort = null ;
	private		File				mFile  = null ;




	private FileInputPort( File file , String sysid ) throws IOException
	{
		mFile = file;
		super.setSystemId(sysid);
		if( FileUtils.isNullFile(file))
			return ;

		if( ! (mFile.exists() && mFile.isFile() && mFile.canRead()) )
			throw new FileNotFoundException("File does not exist or is not a file or not readable: " +  mFile.getAbsolutePath());
	}


	public	FileInputPort( File file  ) throws IOException
	{
		this(file, file.toURI().toString() );
	}




	/**
	 * @param systemId
	 * @see org.xmlsh.core.io.AbstractPort#setSystemId(java.lang.String)
	 */
	@Override
	public void setSystemId(String systemId) {
		super.setSystemId(systemId);
		if( mStreamPort != null )
			mStreamPort.setSystemId(systemId);
	}



	private StreamInputPort getStreamPort() throws CoreException  {
		if( mStreamPort == null )
			try {
				FileInputStream fileStream = new FileInputStream(mFile);
				mStreamPort = new StreamInputPort( fileStream , getSystemId() );
			} catch (FileNotFoundException e) {
				Util.wrapCoreException( "Error opening file for input: " + mFile.getAbsolutePath() , e );
			} 
		return mStreamPort;
	}




	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.io.StreamInputPort#asInputStream(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public InputStream asInputStream(SerializeOpts opts) throws CoreException {
		return getStreamPort().asInputStream(opts);
	}





	/**
	 * @throws IOException 
	 * @see org.xmlsh.core.io.StreamInputPort#close()
	 */
	@Override
	public void close() throws IOException {
		if( mStreamPort != null )
			mStreamPort.close();
	}





	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.io.StreamInputPort#asSource(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public Source asSource(SerializeOpts opts) throws CoreException {
		return getStreamPort().asSource(opts);
	}





	/**
	 * @return
	 * @see org.xmlsh.core.InputPort#isFile()
	 */
	@Override
	public boolean isFile() {
		return mFile != null  ;
	}


	/**
	 * @return
	 * @throws UnimplementedException
	 * @see org.xmlsh.core.InputPort#getFile()
	 */
	@Override
	public File getFile() throws UnimplementedException {
		return mFile ;
	}





	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.io.StreamInputPort#asInputSource(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public InputSource asInputSource(SerializeOpts opts) throws CoreException {
		return getStreamPort().asInputSource(opts);
	}





	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @throws IOException 
	 * @see org.xmlsh.core.io.StreamInputPort#asXdmNode(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XdmNode asXdmNode(SerializeOpts opts) throws CoreException, IOException {
		return getStreamPort().asXdmNode(opts);
	}




	/**
	 * @param out
	 * @param opts
	 * @throws CoreException
	 * @throws IOException
	 * @see org.xmlsh.core.io.StreamInputPort#copyTo(java.io.OutputStream, org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public void copyTo(OutputStream out, SerializeOpts opts) throws CoreException, IOException {
		getStreamPort().copyTo(out, opts);
	}





	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.io.StreamInputPort#asXMLEventReader(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XMLEventReader asXMLEventReader(SerializeOpts opts) throws CoreException {
		return getStreamPort().asXMLEventReader(opts);
	}





	/**
	 * @param opts
	 * @return
	 * @throws CoreException
	 * @see org.xmlsh.core.io.StreamInputPort#asXMLStreamReader(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XMLStreamReader asXMLStreamReader(SerializeOpts opts) throws CoreException {
		return getStreamPort().asXMLStreamReader(opts);
	}




	/**
	 * @param serializeOpts
	 * @return
	 * @throws CoreException
	 * @throws IOException 
	 * @see org.xmlsh.core.io.StreamInputPort#asXdmItem(org.xmlsh.sh.shell.SerializeOpts)
	 */
	@Override
	public XdmItem asXdmItem(SerializeOpts serializeOpts) throws CoreException, IOException {
		return getStreamPort().asXdmItem(serializeOpts);
	}


	@Override
	public JsonNode asJson(SerializeOpts serializeOpts) throws IOException, CoreException {
		return JSONUtils.readJsonNode( asInputStream(serializeOpts));


	}





}



//
//
//Copyright (C) 2008-2014 David A. Lee.
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
