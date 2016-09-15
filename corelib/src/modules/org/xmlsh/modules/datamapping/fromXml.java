/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.datamapping;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.annotations.Function;
import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.core.io.XValueInputPort;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.json.XMLRewritingStreamReader;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Function(name="from-xml",names={"convert-from-xml"} )
public class fromXml	extends AbstractBuiltinFunction {

    protected XmlMapper getXmlMapper() {
        XmlMapper xmlMapper = JSONUtils.newXmlMapper();
        xmlMapper
                .setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        return xmlMapper;
    }
	@Override
	public XValue run(Shell shell, List<XValue> args) throws SaxonApiException, IOException, XMLStreamException, ClassNotFoundException, CoreException {

	    mLogger.entry( args );
	    requires(args.size() == 2, "from-xml( xml class");
	    Class<?> clss = JavaUtils.convertToClass(args.get(1), shell);
	    requires( clss != null , "Cannot convert to class");
	    
	    XMLStreamReader reader = null;
        try ( InputPort iPort = new XValueInputPort(args.get(0)) ){
             reader =  new XMLRewritingStreamReader(iPort.asXMLStreamReader(shell.getSerializeOpts()) );
             XmlMapper mapper = getXmlMapper();
             Object obj = mapper.readValue(reader, clss);
            requires( obj != null , "Cannot convert to class");
		    return XValue.newXValue( null , obj );
		} finally {
		    Util.safeClose( reader );
		    
		}
	}

}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

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
 * 
 */