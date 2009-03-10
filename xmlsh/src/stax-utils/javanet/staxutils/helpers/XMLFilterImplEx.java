package javanet.staxutils.helpers;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Extension to XMLFilterImpl that implements LexicalHandler.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class XMLFilterImplEx extends XMLFilterImpl implements LexicalHandler {
    
    protected LexicalHandler lexicalHandler;
    
    protected boolean namespacePrefixes;
    
    public void setNamespacePrefixes(boolean v) {
        namespacePrefixes = v;
    }
    
    public boolean getNamespacePrefixes() {
        return namespacePrefixes;
    }
    
    /**
     * Set the lexical event handler.
     *
     * @param handler the new lexical handler
     */
    public void setLexicalHandler(LexicalHandler handler) {
        lexicalHandler = handler;
    }
    
    /**
     * Get the lexical event handler.
     *
     * @return The current lexical handler, or null if none was set.
     */
    public LexicalHandler getLexicalHandler() {
        return lexicalHandler;
    }


    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.ext.LexicalHandler.
    ////////////////////////////////////////////////////////////////////
    
    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicId, systemId);
        }
    }
    
    public void endDTD() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }
    
    public void startEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startEntity(name);
        }
    }
    
    public void endEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endEntity(name);
        }
    }
    
    public void startCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }
    
    public void endCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }
    
    public void comment(char ch[], int start, int length) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.comment(ch, start, length);
        }
    }    
}
