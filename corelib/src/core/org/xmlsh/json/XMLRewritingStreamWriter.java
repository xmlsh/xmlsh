package org.xmlsh.json;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.util.StreamWriterDelegate;
import org.xmlsh.util.INamingStrategy;
import org.xmlsh.annotations.*;

public class XMLRewritingStreamWriter extends StreamWriterDelegate {
    private INamingStrategy mNamingStrategy = INamingStrategy.DefaultNamingStrategy;

    public XMLRewritingStreamWriter(XMLStreamWriter parentWriter) {
        super(parentWriter);
    }

    @Override
    public void writeEmptyElement(String local) throws XMLStreamException {
        // TODO Auto-generated method stub
        this.writeEmptyElement(null,local,null);
    }

    @Override
    public void writeEmptyElement(String ns, String local)
            throws XMLStreamException {
        // TODO Auto-generated method stub
        this.writeEmptyElement(null,local,ns);
    }

    @Override
    public void writeEmptyElement(String prefix, String local, String ns)
            throws XMLStreamException {
        QName qn = mNamingStrategy.toXmlName(local);
        super.writeEmptyElement(qn.getPrefix(), qn.getLocalPart(), qn.getNamespaceURI());
    }

    @Override
    public void writeStartElement(String local) throws XMLStreamException {
        // TODO Auto-generated method stub
        this.writeStartElement(null,local,null);
    }

    @Override
    public void writeStartElement(String ns, String local)
            throws XMLStreamException {
        // TODO Auto-generated method stub
        this.writeStartElement(null,local,ns);
    }
    @Bug(value="Workaround for Saxon 9.5 problem in STAX writer - it will not create xmlns entries for you")
    @Override
    public void writeStartElement(String prefix, String local, String ns )
            throws XMLStreamException {
        QName qn = mNamingStrategy.toXmlName(local);
       super.writeStartElement(qn.getPrefix(), qn.getLocalPart(), qn.getNamespaceURI());
       
      
       if( qn.getPrefix() != null )
    	   super.writeNamespace(qn.getPrefix(), qn.getNamespaceURI());


    }
    
    
}