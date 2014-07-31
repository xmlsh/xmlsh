package org.xmlsh.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.ValueRepresentation;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.value.AtomicValue;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.sh.shell.SerializeOpts;

public class XMLUtils
{
    public static byte[]   toBytes(XdmValue xdm , SerializeOpts opts ) throws SaxonApiException, IOException
    {
        if( xdm != null ){
            if( isAtomic(xdm) )
                return xdm.toString().getBytes(opts.getOutput_text_encoding());
            else
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                serialize( xdm ,  out , opts );
                return out.toByteArray();
            }
        }
        return null;
    }


    public static boolean isAtomic(XdmValue value) {
        if(  value == null )  // strange but null is atomic for this 
            return true ;

        ValueRepresentation<? extends Item> item = value.getUnderlyingValue();
        boolean isAtom = ( item instanceof AtomicValue ) || 
                ( item instanceof NodeInfo && ((NodeInfo)item).getNodeKind() == net.sf.saxon.type.Type.TEXT ) ;
        return isAtom;
    }
    
    public static XdmItem asXdmItem(XdmValue value)
    {
        if( value == null)
            return null  ;
        try {
            return  value.itemAt(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (SaxonApiUncheckedException e) {
            return null;
        }

    }
    
    public static XdmValue asXdmValue(Object obj) 
    {
        if( obj instanceof XdmValue)
            return (XdmValue) obj ;
        return null;
    }

    public static XdmNode asXdmNode(XdmValue xdm) throws InvalidArgumentException
    {
        XdmItem item = asXdmItem(xdm);
        if( item instanceof XdmNode )
            return (XdmNode) item ;
        else
            throw new InvalidArgumentException("Value is not a Node");
    }
    
    public static void serialize( XdmValue value , OutputStream out, SerializeOpts opt) throws SaxonApiException, IOException
    {
        Serializer ser = Util.getSerializer(opt);
        ser.setOutputStream( out );
        Util.writeXdmValue( value , ser );
        ser.close();;
        out.flush();
    }


};
