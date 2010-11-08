/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.s9api.QName;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xdelattribute extends XCommand {


	
	
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( "a=attribute:+,v,e=element:+" , SerializeOpts.getOptionDefs() );
		opts.parse(args);
		args = opts.getRemainingArgs();
		
		List<QName>	attrs 		=	getQNames( opts.getOptValuesRequired("a"));
		boolean bExcept 		= opts.hasOpt("v");
		List<QName>	elements 	= getQNames( opts.getOptValues("e"));
		

		XMLEventFactory	mFactory = XMLEventFactory.newInstance();
		
		
		
		InputPort stdin = null;
		if( args.size() > 0 )
			stdin = getInput( args.get(0));
		else
			stdin = getStdin();
		if( stdin == null )
			throw new InvalidArgumentException("Cannot open input");
		try {
			
			SerializeOpts sopts = getSerializeOpts(opts);
			
			XMLEventReader	reader = stdin.asXMLEventReader(sopts);
			OutputPort stdout = getStdout();
			XMLEventWriter  writer = stdout.asXMLEventWriter(sopts);
			
			stdout.setSystemId(stdin.getSystemId());
			XMLEvent e;
			
			while( reader.hasNext() ){
				e = (XMLEvent) reader.next();
				if( e.isStartElement()){
					StartElement se = e.asStartElement();
					
					// Only look at elements in list, or all elements if null 
					if( elements == null || matches( se.getName() , elements , false)){
						
						// If matching (or excluding) attributes delete them 
						Iterator<Attribute> iter = (Iterator<Attribute>) se.getAttributes();
						
						boolean bMatches  = false ;
						while( iter.hasNext() ){
							Attribute attr = iter.next();
							if( matches(attr.getName() , attrs , bExcept )){
								bMatches = true ;
								break ;
							}
							
							
							
						}
						
						// If any match then synthesize a new start element 
						if( bMatches ){
							Iterator	namespaces = se.getNamespaces();
							List<Attribute>  newAttrs = new ArrayList<Attribute>();
							iter = (Iterator<Attribute>) se.getAttributes();
							while( iter.hasNext() ){
								Attribute attr = iter.next();
								if( ! matches( attr.getName() , attrs , bExcept ) )
									newAttrs.add(attr);
							}
							e = mFactory.createStartElement(se.getName(), newAttrs.iterator() , namespaces);
							
							
						}
						
					}
						
					
					
					
				}
				
				
				writer.add(e);
			}
			// writer.add(reader);
			reader.close();
			writer.close();
		} 
		finally {
			
			stdin.close();
		}
		return 0;
		
		
	}
	
	/*
	 * Returns true if name matches (or does not match) any name in list of names 
	 * 
	 */
	
	
	private boolean matches(javax.xml.namespace.QName name, List<QName> names, boolean bExcept) 
	{
		for( QName qname : names ){
			if( Util.isEqual(name.getNamespaceURI(), qname.getNamespaceURI() ) &&
				Util.isEqual(name.getLocalPart(), qname.getLocalName() ) ) 
					return bExcept ? false : true ;
			
		}
		return bExcept ? true : false ;
		
		
		
		
	}

	private List<QName> getQNames(List<XValue> opts) {
		if( opts == null || opts.size() == 0 )
			return null ;
		List<QName>		names = new ArrayList<QName>();
		
		for( XValue v : opts ){
			names.add( v.asQName() );
			
		}
		
		
		return names ;
	}

}



//
//
//Copyright (C) 2008,2009,2010 , David A. Lee.
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
