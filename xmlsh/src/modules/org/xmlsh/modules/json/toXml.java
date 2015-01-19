/**
 * $Id: $
 * $Date: $
 * 
 */

package org.xmlsh.modules.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.BuildingStreamWriter;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.SafeXMLStreamWriter;
import org.xmlsh.core.XValue;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.json.JsonRenamingParserDelegate;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.types.TypeFamily;
import org.xmlsh.util.XMLUtils;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

public class toXml extends AbstractBuiltinFunction {

    public toXml()
    {
        super("to-xml");
    }
    
    protected XmlMapper getXmlMapper() {
        XmlMapper xmlMapper = JSONUtils.newXmlMapper();
        xmlMapper
                .setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        return xmlMapper;
    }
    @Override
    public XValue run(Shell shell, List<XValue> args)
            throws InvalidArgumentException, SaxonApiException, IOException,
            XMLStreamException {

        Processor proc = Shell.getProcessor();


        BuildingStreamWriter bw = proc.newDocumentBuilder()
                .newBuildingStreamWriter();

        XMLStreamWriter xd = new SafeXMLStreamWriter(bw);
        XmlMapper xmlMapper = getXmlMapper();
        ObjectMapper mapper = JSONUtils.getJsonObjectMapper();
        
         ToXmlGenerator mGenerator = xmlMapper.getFactory().createGenerator(xd);

        bw.writeStartDocument();
        for (XValue arg : args) {
                

            try (
            JsonParserDelegate parser = new JsonRenamingParserDelegate(
                    new TreeTraversingParser(arg.toJson(), mapper ), true) ){
              TreeNode tree = parser.readValueAsTree();
            
              mGenerator.writeTree(tree);
            }
            
        }

        bw.writeEndDocument();
        return XValue.newXValue(TypeFamily.XDM, bw.getDocumentNode());

    }

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 * 
 * The Original Code is: all this file.
 * 
 * The Initial Developer of the Original Code is David A. Lee
 * 
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 * 
 * Contributor(s): David A. Lee
 */