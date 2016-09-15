package org.xmlsh.json;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import org.xmlsh.util.INamingStrategy;

public class XMLRewritingStreamReader extends StreamReaderDelegate {

    private INamingStrategy mNamingStrategy = INamingStrategy.DefaultNamingStrategy;

    public XMLRewritingStreamReader(XMLStreamReader arg0) {
        super(arg0);
    }

    @Override
    public String getLocalName() {
        return mNamingStrategy.fromXmlName(  super.getName() );
    }

    @Override
    public QName getName() {
        return super.getName();
    }

    @Override
    public String getNamespaceURI() {
        return  super.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return  super.getPrefix();
    }
    
    
    
    

}
