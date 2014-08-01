package org.xmlsh.core;

import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xml.sax.InputSource;
import org.xmlsh.sh.shell.SerializeOpts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;

import com.fasterxml.jackson.databind.JsonNode;

public interface IInputPort extends IPort {

	public   <T extends AbstractPort> ReferenceCountedHandle<T> newReference();
	public  InputStream asInputStream(SerializeOpts opts)
			throws CoreException;

	public  Source asSource(SerializeOpts opts) throws CoreException;

	public  InputSource asInputSource(SerializeOpts opts)
			throws CoreException;

	public  XdmNode asXdmNode(SerializeOpts opts) throws CoreException;

	public  void copyTo(OutputStream out, SerializeOpts opts)
			throws CoreException, IOException;

	public  XMLEventReader asXMLEventReader(SerializeOpts opts)
			throws CoreException;

	public  XMLStreamReader asXMLStreamReader(SerializeOpts opts)
			throws CoreException;

	public  XdmItem asXdmItem(SerializeOpts serializeOpts)
			throws CoreException;

	public  Reader asReader(SerializeOpts serializeOpts)
			throws UnsupportedEncodingException, CoreException;

	// Default implementation uses a singleton as the input stream
	public  IXdmItemInputStream asXdmItemInputStream(
			SerializeOpts serializeOpts) throws CoreException;

	public  JsonNode asJson(SerializeOpts serializeOpts)
			throws IOException, CoreException;


}