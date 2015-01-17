/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.modules.json;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.core.AbstractBuiltinFunction;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.core.XVariable;
import org.xmlsh.core.io.VariableInputPort;
import org.xmlsh.json.JSONUtils;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

public class fromXml	extends AbstractBuiltinFunction {


    @JacksonXmlRootElement(localName = "dynaBean", namespace = "")
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "class", include = JsonTypeInfo.As.PROPERTY)
    public static class DynaBean {
        private final Map<String, String> _properties = new TreeMap<String, String>();

        public DynaBean(Map<String, String> values) {
            _properties.putAll(values);
        }

        @JsonAnyGetter
        @JacksonXmlProperty(isAttribute = false)
        public Map<String, String> getProperties() {
            return _properties;
        }
    }    
	public fromXml()
	{
		super("from-xml");
	}

	@Override
	public XValue run(Shell shell, List<XValue> args) throws SaxonApiException, IOException, XMLStreamException, ClassNotFoundException, CoreException {

		JacksonXmlModule module = new     JacksonXmlModule();
		// to default to using "unwrapped" Lists:
		module.setDefaultUseWrapper(false);
		XmlMapper xmlMapper = new XmlMapper(module);
		XmlMapper mapper = JSONUtils.getXmlMapper();
		Class<?> cls = 
				args.size() > 1 ? JavaUtils.convertToClass(args.get(1) , shell ) : null ;
		if( cls == null )
			cls = DynaBean.class ; // JSONUtils.jsonNodeClass();
		XMLStreamReader reader = null ;
		
		try ( 
			VariableInputPort iPort = new VariableInputPort(XVariable.anonymousInstance( args.get(0))) ){
		
			reader = iPort.asXMLStreamReader(shell.getSerializeOpts());
		
			Object obj = mapper.readValue( reader , cls );

		    return XValue.newXValue(null,obj);
		} finally {
			Util.safeClose(reader);
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