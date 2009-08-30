/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

public class xsql extends XCommand {


	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = new Options( "cp=classpath:,d=driver:,u=user:,p=password:,root:,row:,attr,c=connect:,q=query:" , args );
		opts.parse();
		
		String root = opts.getOptString("root", "root");
		String row = opts.getOptString("row", "row");
		
		String connect = opts.getOptStringRequired("c");
		String query = opts.getOptString("q", null);
		String driver = opts.getOptStringRequired("driver");
		String user = opts.getOptString("user","");
		String password = opts.getOptString("password","");

		boolean bAttr = opts.hasOpt("attr");
		
		ClassLoader classloader = getClassLoader(opts.getOptValue("cp"));
		

		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		if( query == null   &&  ! xvargs.isEmpty() )
			query = xvargs.get(0).toString();
		
		Connection conn = getConnection(driver, classloader, connect ,user,password);
		PreparedStatement pStmt  = null ;
		ResultSet rs = null ;
		try {
			pStmt = conn.prepareStatement(query);
			
			OutputPort stdout = getStdout();
			XMLStreamWriter writer = stdout.asXMLStreamWriter(getSerializeOpts());
			
			writer.writeStartDocument();		
			writer.writeStartElement(root);
	
			
			rs = pStmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			
			while( rs.next()  ){
				
				addElement( writer , rs , row ,  bAttr , meta );
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			
			stdout.writeSequenceTerminator();
			
		
		} finally {
			try {
				if( rs != null ) rs.close();
				if( pStmt != null ) pStmt.close();
				if( conn != null ) conn.close();
			} catch( Exception e )
			{}
			
		}
	
		return 0;
		
		
		
	}
	
	private Connection getConnection(String driver, ClassLoader classloader, String connect, String user, String password) throws SQLException, SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Class cls = loadDriver(driver,classloader);
	
		
	     java.util.Properties info = new java.util.Properties();

	
		if (user != null) {
		    info.put("user", user);
		}
		if (password != null) 
		    info.put("password", password);
		 
		
		 Method mConn = cls.getMethod("connect" , String.class , info.getClass() );
		 Object obj = cls.newInstance();
		 Connection c = (Connection) mConn.invoke(obj, connect , info );
		    
		 return c;

		
		
	}

	 //--------------------------------
	  // Load JDBC-driver
	  // params  none
	  // returns error string if error occurs by loaded driver
	  //         if no errors - returns empty string
	  //--------------------------------
	  private  Class loadDriver (String driver,ClassLoader classloader) throws SQLException {



	    try {
	      return  Class.forName(driver,true,classloader);
	    }
	    catch (Exception e) {
	      throw new SQLException( e.toString() );
	    }
	    
	  }
	private void addElement(
			XMLStreamWriter writer, 
			ResultSet rs ,
			String row, 
			boolean battr,
			ResultSetMetaData meta) throws  XMLStreamException, SQLException 
		{
			
			writer.writeStartElement(row);
			// Attribute normal format
			if( battr ){
				
				
				for( int i = 0 ; i < meta.getColumnCount() ; i++ ){
					String name = getAttrName( i , meta );
					writer.writeAttribute(name,Util.blankIfNull(rs.getString(i+1)));
				}
				
				
			} else {


				for( int i = 0 ; i < meta.getColumnCount() ; i++ ){
					String name = getColName( i ,  meta );
					writer.writeStartElement(name);
					writer.writeCharacters(Util.blankIfNull(rs.getString(i+1)));

					writer.writeEndElement();
					
				}
				
			}
			writer.writeEndElement();
			
		}
	
	
	// Get an attribute name 
	private String getAttrName(int i, ResultSetMetaData meta) throws SQLException {
		
			return toXmlName( meta.getColumnName(i+1));

		
	}



	private String getColName(int i, ResultSetMetaData meta) throws SQLException {

			return toXmlName( meta.getColumnName(i+1));
		
	}

	private String toXmlName(String field) {
		return field.replaceAll("[^a-zA-Z0-9_]","-");
	}

	

}



//
//
//Copyright (C) 2008,2009 David A. Lee.
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
