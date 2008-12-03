package org.xmlsh.util;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

/**
 * @author DLEE
 *
 */
public class XMLSerializer {

    private Transformer mTransformer = null;
    
    public XMLSerializer() throws XMLException  
    {
		try
		{
			mTransformer = TransformerFactory.newInstance().newTransformer();
			//mTransformer.setOutputProperty( OutputKeys.DOCTYPE_PUBLIC , "publicID" );
			//mTransformer.setOutputProperty( OutputKeys.DOCTYPE_SYSTEM , "systemID" );
			mTransformer.setOutputProperty( OutputKeys.INDENT , "yes" );
			mTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION , "yes" );
			//mTransformer.setOutputProperty( OutputKeys.STANDALONE , "no" );
			mTransformer.setOutputProperty( OutputKeys.ENCODING , "UTF-8");
			        
		}
		catch (Exception e)
		{
            throw new XMLException("Exception creating XML serializer",e);
		}
    }
    public void setOmitXML(boolean bOmit)
    {
        mTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION , bOmit ? "yes" : "no");
    }
    
    /*
     * Specifies what elements need to be encoded in CDATA sections
     */
    public void setCDATA(String elements) {
    	mTransformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS , elements);
    }
    /**
     * Write Node to an OutputStream
     */
    
    public void write( Node node , OutputStream out ) throws XMLException 
    {
        DOMSource source = new DOMSource( node );
        StreamResult result = new StreamResult( out );
        
        try
		{
			mTransformer.transform( source , result );
		}
		catch (TransformerException e)
		{
		  throw new XMLException("Exception transforming XML document",e);
		}
    }                
    
    /**
     * Write node to a string and return it
     * @throws UnsupportedEncodingException 
     */
    
    public String write( Node node  ) throws XMLException
    {
        
        
        DOMSource source = new DOMSource( node );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamResult result = new StreamResult( out );

        try
		{
			mTransformer.transform( source , result );
		}
		catch (TransformerException e)
		{
            throw new XMLException("Exception transforming XML document",e);

		}
        try {
			return out.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new XMLException("Exception encoding XML document from UTF-8",e);
		}
    }
	
}
