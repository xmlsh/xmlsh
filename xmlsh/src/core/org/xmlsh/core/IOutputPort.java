package org.xmlsh.core;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

import org.xml.sax.ContentHandler;
import org.xmlsh.sh.shell.SerializeOpts;

public interface IOutputPort extends IPort , Flushable {

	public  OutputStream asOutputStream(SerializeOpts opts)
			throws CoreException;

	public  PrintStream asPrintStream(SerializeOpts opts)
			throws CoreException;

	public  Destination asDestination(SerializeOpts opts)
			throws CoreException;

	public  PrintWriter asPrintWriter(SerializeOpts opts)
			throws UnsupportedEncodingException, CoreException;

	// These 2 shouldnt really go on the port 
	public  void writeSequenceSeperator(SerializeOpts serializeOpts)
			throws IOException, InvalidArgumentException, CoreException,
			SaxonApiException;

	public  void writeSequenceTerminator(SerializeOpts serializeOpts)
			throws IOException, CoreException, SaxonApiException;

	public  XMLStreamWriter asXMLStreamWriter(SerializeOpts opts)
			throws InvalidArgumentException, XMLStreamException,
			SaxonApiException, CoreException;

	public  XMLEventWriter asXMLEventWriter(SerializeOpts opts)
			throws InvalidArgumentException, XMLStreamException,
			SaxonApiException, IOException, CoreException;

	public  IXdmItemOutputStream asXdmItemOutputStream(
			SerializeOpts opts) throws CoreException;

	public  ContentHandler asContentHandler(SerializeOpts opts)
			throws XPathException, SaxonApiException, CoreException;

	@Override
	public  boolean isFile();

	@Override
	public  File getFile() throws UnimplementedException;

	public  boolean isNull();

}