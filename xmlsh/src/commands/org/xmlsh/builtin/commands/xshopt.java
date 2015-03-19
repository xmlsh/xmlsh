package org.xmlsh.builtin.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.core.BuiltinCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.io.OutputPort;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.sh.shell.ShellOpts;
import org.xmlsh.types.xtypes.XValueProperties;
import org.xmlsh.types.xtypes.XValueProperty;


public class xshopt extends BuiltinCommand {

    static final String sDocRoot = "options";
    @Override
    public int run(   List<XValue> args ) throws Exception {




        Options opts = new Options( "" , SerializeOpts.getOptionDefs()  );
        opts.parse(args);
        setSerializeOpts(opts);
        args=opts.getRemainingArgs();

        printOpts( opts  );

        return 0;

    }
    private void printOpts(Options opts) throws XMLStreamException, IOException, CoreException, SaxonApiException
    {
        OutputPort stdout = getStdout();
        XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());

        try {
            writer.writeStartDocument();
            writer.writeStartElement( sDocRoot );


            XValueProperties props = mShell.getOpts().getOptionsAsProperties() ;
            writeOptions(writer, props);

            writer.writeEndElement();
            writer.writeEndDocument();

        } finally {
            writer.close();
            stdout.writeSequenceTerminator(getSerializeOpts());
        }
    }
    private void writeOptions(XMLStreamWriter writer, XValueProperties props)
            throws XMLStreamException, InvalidArgumentException,
            UnexpectedException {
        for( XValueProperty prop : props.asPropertyList()  ){
            writer.writeStartElement( "option");
            writer.writeAttribute( "name", prop.getKey());
            XValue value = prop.getValue();
            if( ! value.isNull() ){
                if( value.isInstanceOf(XValueProperties.class) )
                    writeOptions( writer , value.asInstanceOf(XValueProperties.class));
                else

                    if( value.isInstanceOf( Boolean.class) )
                        writer.writeCharacters( value.toBoolean() ?"on" : "off");
                    else
                        writer.writeCharacters( value.toString());
            }
            writer.writeEndElement();
        }
    }

}