/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.jmx;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.remote.JMXConnector;
import javax.xml.stream.XMLStreamException;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.jmx.util.JMXCommand;
import org.xmlsh.sh.shell.SerializeOpts;

public class query extends JMXCommand {

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(sCOMMON_OPTS ,SerializeOpts.getOptionDefs());
		opts.parse(args);
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		args = opts.getRemainingArgs();
		
		String connect = opts.getOptStringRequired("c");
		
		
		
		JMXConnector jmx = getConnector(opts);
		try {
			MBeanServerConnection mbean = jmx.getMBeanServerConnection();
			

			OutputPort out = this.getStdout();
			mWriter = out.asXMLStreamWriter( mSerializeOpts  );
			
			startDocument();
			mWriter.writeStartElement("","objects",kJMX_NS);
			mWriter.writeDefaultNamespace(kJMX_NS);
			attribute("date" , formatXSDateTime(new Date()));
	
	
			for( XValue arg : args ){
				
				String[]  nameAttrs = arg.toString().split(";");
				
				
				
				ObjectName name = objectName(nameAttrs[0]);
				
				MBeanInfo info = mbean.getMBeanInfo(name);
		
			
				
				writeObject(Arrays.copyOfRange(nameAttrs, 1, nameAttrs.length) , name, mbean, info);
				
			}
			endElement();
			endDocument();
			closeWriter();
	
				
			out.release();
		} finally {
			jmx.close();
		}
		return 0;
		
		
		
	}


	private void writeObject(String[] attrs, ObjectName name, MBeanServerConnection mbean,
			MBeanInfo info) throws XMLStreamException, InstanceNotFoundException,
			ReflectionException, IOException {
		startElement("object");
		attribute("classname" , info.getClassName());

		String[] attributes = getAttributeNames(info, attrs );
		
		AttributeList list = mbean.getAttributes(name, attributes);
		for( Attribute attr : list.asList() ){
			
			writeAttribute( attr );
		}
		endElement();
	}


	private String[] getAttributeNames(MBeanInfo info, String[] attrs ) {
		
		if( attrs != null && attrs.length > 0 )
			return attrs ;
		
		
		
		MBeanAttributeInfo[] infos = info.getAttributes();
		String[] attributes = new String[infos.length];
		int i = 0;
		for( MBeanAttributeInfo attrInfo : infos )
			attributes[i++] = attrInfo.getName();
		return attributes;
	}
	
	private void writeValue(String element , String name , Object value ) throws XMLStreamException {
		startElement(element);
		if( name != null )
			attribute( "name" , name);
		writeObjectValue( value );
		endElement();
	}
	
	
	
	
	private void writeAttribute(Attribute attr) throws XMLStreamException {
		writeValue("attribute" , attr.getName() , attr.getValue());
		
	}


	private void writeObjectValue(Object value) throws XMLStreamException {
		if( value == null )
			return ;
		
		if( value instanceof CompositeDataSupport )
			writeObjectValue( (CompositeDataSupport) value  );
		else
		if( value instanceof TabularDataSupport )
			writeObjectValue( (TabularDataSupport) value );
		
		else 
		if( value instanceof Object[] )
		{
			writeArray(value);
		}
		else
		if( value.getClass().isArray() ){
			writeArray( toObjectArray(value) );
			
		}
			
			
		else
		{
			attribute("type" , value.getClass().getName());
			characters( value.toString() );
			
		}
		
		
	}


	/*
	 * Convert an array of primatives to an array of objects
	 */
	private Object[] toObjectArray(Object value) {
		
		int len = Array.getLength(value);
		Object[] array = new Object[len];
		for( int i = 0 ; i < len ; i++ )
			array[i] = Array.get(value, i);
		
		return array ;
		
	}


	private void writeArray(Object value) throws XMLStreamException {
		for( Object o  :(Object[]) value ) 
			writeValue( "entry", null , o );
	}

	private void writeObjectValue(CompositeDataSupport value) throws XMLStreamException {
		CompositeType type = value.getCompositeType();
		attribute("type" , type.getTypeName() );
		
		for( String name : type.keySet() ){
			Object ov = value.get(name);
			writeValue( "entry" , name , ov );
			
			
		}
		
		
	}
	
	private void writeObjectValue(TabularDataSupport value) throws XMLStreamException {
		TabularType type = value.getTabularType();
		CompositeType ctype = type.getRowType();
		List<String> indexNames = type.getIndexNames() ;
		
		attribute("type" , type.getTypeName() );
		
		int i = 0;
		for( Object  row : value.values() ){
			startElement("row");
			// attribute("name" , indexNames.get(i++));
			writeObjectValue( row );
			endElement();
			
		}
	
	}
	
	
	String formatXSDateTime(Date date) 
	{
		if( date == null )
			date = new Date();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
		df.setTimeZone( TimeZone.getTimeZone("UTC") );
			
			
		
		return df.format(date);
	}

		

}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
