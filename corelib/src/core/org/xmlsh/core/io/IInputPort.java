package org.xmlsh.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.xml.sax.InputSource;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.ReferenceCountedHandle;
import org.xmlsh.sh.shell.SerializeOpts;

import com.fasterxml.jackson.databind.JsonNode;

public interface IInputPort extends IPort {

	public   <T extends AbstractPort> ReferenceCountedHandle<T> newReference();
	
	public  InputStream asInputStream(SerializeOpts opts)
			throws CoreException, IOException;

	public  Source asSource(SerializeOpts opts) throws CoreException;

	public  InputSource asInputSource(SerializeOpts opts)
			throws CoreException, IOException;

	public  XdmNode asXdmNode(SerializeOpts opts) throws CoreException, IOException;

	public  void copyTo(OutputStream out, SerializeOpts opts)
			throws CoreException, IOException;

	public  XMLEventReader asXMLEventReader(SerializeOpts opts)
			throws CoreException;

	public  XMLStreamReader asXMLStreamReader(SerializeOpts opts)
			throws CoreException, IOException;

	public  XdmItem asXdmItem(SerializeOpts serializeOpts)
			throws CoreException, IOException;

	public  Reader asReader(SerializeOpts serializeOpts)
			throws UnsupportedEncodingException, CoreException, IOException;

	// Default implementation uses a singleton as the input stream
	public  IXdmItemInputStream asXdmItemInputStream(
			SerializeOpts serializeOpts) throws CoreException, IOException;

	public  JsonNode asJson(SerializeOpts serializeOpts)
			throws IOException, CoreException;


}