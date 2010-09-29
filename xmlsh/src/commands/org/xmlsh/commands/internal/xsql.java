/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.Util;

public class xsql extends XCommand {

		
	private static abstract class IDriver 
	{
		abstract Connection	getConnection() throws Exception;
		abstract void 		releaseConnection(Connection c) throws Exception;
		//--------------------------------
		  // Load JDBC-driver
		  // params  none
		  // returns error string if error occurs by loaded driver
		  //         if no errors - returns empty string
		  //--------------------------------
		  protected  Class<?> loadDriver (String driver,ClassLoader classloader) throws SQLException {



		    try {
		      return  Class.forName(driver,true,classloader);
		    }
		    catch (Exception e) {
		      throw new SQLException( e.toString() );
		    }
		    
		  }

	}
	
	private static class JDBCDriver extends IDriver
	{
		
		
		String mConnect;
		String mUser;
		String mPassword;
		Properties mOptions;
		Class<?>	mClass ;
		
		JDBCDriver( String driver, ClassLoader classloader, String connect, String user, String password, Properties options ) throws SQLException
		{
			mConnect = connect ; 
			mUser = user ; 
			mPassword = password ; 
			mOptions = options ;
			
			mClass = loadDriver(driver,classloader);
			
		}

		@Override
		public
		Connection	getConnection() throws Exception 
		{
					
			
				
		    java.util.Properties info = new java.util.Properties();
		    if( mOptions != null )
		    	info.putAll(mOptions);
	
		
			if (mUser != null) {
			    info.put("user", mUser);
			}
			if (mPassword != null) 
			    info.put("password", mPassword);
			 
			
			 Method mConn = mClass.getMethod("connect" , String.class , info.getClass() );
			 Object obj = mClass.newInstance();
			 Connection c = (Connection) mConn.invoke(obj, mConnect , info );
		    
			 return c;
		
		}

		 
		@Override
		public void releaseConnection(Connection c ) throws Exception  
		{
			try {
				c.close();
			} 
			finally {} 
			
		}
		  
	}

	private static class PoolDriver extends IDriver
	{
		
		Properties mOptions;
		Class<?>	mClass ;
		
		PoolDriver( String driver, ClassLoader classloader, Properties options ) throws SQLException
		{
			mOptions = options ;
			
			mClass = loadDriver(driver,classloader);
			
		}
		
		
		
		@Override
		public Connection getConnection() throws Exception {
			Method method = mClass.getMethod("getConnection");
			if( method != null )
				return (Connection) method.invoke(null);
			else
				return null;
			
		}

		@Override
		public void releaseConnection(Connection c) throws Exception {
			Method method = mClass.getMethod("releaseConnection", Connection.class );
			if( method != null )
				method.invoke(null, c );
			
			
		}
		
	}
	
	
	
	private boolean bAttr = false ;
	private	 SerializeOpts mSerializeOpts;

	@Override
	public int run(List<XValue> args) throws Exception {

		String fieldXPath  = "name()";
		String tableNameXPath = "name()";


		
		Properties options = null;
		
		Options opts = new Options( "cp=classpath:,pool=pooldriver:,d=driver:,u=user:,p=password:,root:,row:,attr,c=connect:,q=query:,o=option:+,insert,update=execute,tableAttr:,fieldAttr:,fetch:,table:" ,SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		String root = opts.getOptString("root", "root");
		String row = opts.getOptString("row", "row");
		
		String connect = opts.getOptStringRequired("c");
		String query = opts.getOptString("q", null);
		String driver = opts.getOptString("driver",null);
		String user = opts.getOptString("user",null);
		String password = opts.getOptString("password",null);
		String pooldriver = opts.getOptString("pool", null);
		boolean bInsert = opts.hasOpt("insert");
		boolean bUpdate = opts.hasOpt("update");
		String tableAttr = opts.getOptString("tableAttr", null);
		String fetch = opts.getOptString("fetch", null );
		String tableName = opts.getOptString("table", null );
		
		String fieldAttr = opts.getOptString("fieldAttr",null);
			

		if( pooldriver == null && driver == null ){
			usage("Expected -pool or -driver");
			return 1;
		}
		
		boolean bAttr = opts.hasOpt("attr");
		
		
		
		ClassLoader classloader = getClassLoader(opts.getOptValue("cp"));
		
		/*
		 * Optional properties
		 */
		if (opts.hasOpt("o")) {
			options = new Properties();


			// Add custom name spaces
			for (XValue v : opts.getOpt("o").getValues()){
				String vs[] = v.toString().split("=",2);
				options.put(vs[0], vs[1]);

			}
		}
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		if( query == null   &&  ! xvargs.isEmpty() )
			query = xvargs.get(0).toString();
		
		IDriver dbdriver = null;
		if( driver != null ) 
			dbdriver = new JDBCDriver(driver,  classloader, connect ,user,password, options );
		else
			dbdriver = new PoolDriver( pooldriver , classloader , options  );
		
		Connection conn = dbdriver.getConnection();
		
		if( conn == null ){
			
			printErr("Cannot establish connection");
			return(1);
		}
		
		try {
			if( bUpdate )
				runUpdate(conn,  getSerializeOpts(opts), root, row, query, bAttr );
			
			else
			if( bInsert ){
				InputPort  in = getStdin();

				runInsert( conn ,  in, bAttr , tableName , tableAttr , fieldAttr) ;
		
			}
			else
				runQuery(conn,  getSerializeOpts(opts), root, row, query, bAttr , fetch );
		}
		finally {

			dbdriver.releaseConnection(conn);
			
		}
		
		return 0;
		
		
		
	}
	

	/*
	 * Insert rows into a single table
	 * If table is not null use the table name 
	 * [or] If tableAttr is not null then use the root attribute "tableAttr" as the table name otherwise use the element name
	 * If fieldAttr is not null then use each row's attribute "fieldAttr" as the field name otherwise use the element name
	 * 
	 */
	
	
	
	private void runInsert(Connection conn,  InputPort input , boolean bAttr , String tableName , String tableNameAttr, String fieldNameAttr) throws SaxonApiException, SQLException, XMLStreamException, IOException, CoreException {
		

		OutputPort stdout = getStdout();
	
		XMLStreamWriter writer = stdout.asXMLStreamWriter(mSerializeOpts);
		
		writer.writeStartDocument();		
		writer.writeStartElement("results");
		
		
		XMLEventReader reader = input.asXMLEventReader(mSerializeOpts);
		
		try {


			/*
			 * Read the root element and get the table name
			 */

			StartElement 	rootElement = readStartElement( reader );
			if( rootElement == null )
				throw new CoreException("No root element");
			


			if( tableName == null ){
				if( tableNameAttr != null ){
					Attribute attr = rootElement.getAttributeByName(new QName(tableNameAttr));
					if( attr != null )
						tableName = attr.getValue();
				}
				else
					tableName = rootElement.getName().getLocalPart();
			}

			if( Util.isBlank(tableName))
				throw new CoreException("Cannot determine table name for insert");


			/*
			 * Now for each row ...
			 */

			StartElement	rowElement ;
			int allRows = 0;
			while( (rowElement = readStartElement( reader ) ) != null ){

				/*
				 * Process rows as insrt
				 */

				Map<String,String>	namevalues = readNameValues( reader , rowElement , bAttr , fieldNameAttr );

				boolean bFirst = true ;
				StringBuffer sNames = new StringBuffer();
				StringBuffer sQuestions = new StringBuffer();
				List<String>	values = new ArrayList<String>( namevalues.size() );

				for( Entry<String,String> nv : namevalues.entrySet() ){

					String name = nv.getKey();
					String value = nv.getValue();

					if( ! bFirst )
						sNames.append(",");
					sNames.append("`" + name + "`");


					if( ! bFirst )
						sQuestions.append(",");
					sQuestions.append("?");

					values.add(value);
					bFirst = false ;
				}

				String sql = "INSERT INTO `" + tableName + "` (" + sNames.toString() + ") VALUES(" +sQuestions.toString() + ")" ;
				// printErr(sql);

				PreparedStatement pStmt = conn.prepareStatement(sql);
				int i = 1;
				for( String v : values )
					pStmt.setString(i++, v);


				int ret = pStmt.executeUpdate();
				allRows += ret ;



				/*
				 * 					
	
					Integer itype = tableDef.get( name );
					int type = itype == null ? java.sql.Types.CHAR : itype.intValue();
					switch( type ){
						case BIT		:
						case TINYINT 	:
						case SMALLINT	:
						case INTEGER 	:
						case BIGINT 	:
						case FLOAT 		:
						case REAL 		:
						case DOUBLE 	:
						case NUMERIC 	:
						case DECIMAL	:
						case CHAR		:
						case VARCHAR 	:
						case LONGVARCHAR :
						case DATE 		:
						case TIME 		:
						case TIMESTAMP 	:
						case BINARY		:
						case VARBINARY 	:
						case LONGVARBINARY 	:
						case NULL		:
						case OTHER		:
				        case JAVA_OBJECT:
				        case DISTINCT   :
				        case STRUCT     :
				        case ARRAY      :
				        case BLOB       :
				        case CLOB       :
				        case REF        :
					    case DATALINK	:
					    case BOOLEAN 	 :
					    case ROWID 		:
					    case NCHAR 		:
					    case NVARCHAR 	:
					    case LONGNVARCHAR :
					    case NCLOB  	:
					    case SQLXML  	:


				 */					


			}




			writer.writeStartElement("table");
			writer.writeAttribute("name", tableName );
			writer.writeAttribute("rows", String.valueOf(allRows) );
			writer.writeEndElement();
		} finally {
			if( reader != null )
				reader.close();
		}

			
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		
		
	}

	/*
	 * Read name/value pairs from a row record
	 * if bAttr is true then all row attributes are name/value pairs
	 * else read all child elements
	 * 	if fieldNamaAttr != null then use the named attribute as the field name
	 *  else use the element name as the field name 
	 *  Use text as the value
	 */

	private Map<String, String> readNameValues(XMLEventReader reader, StartElement rowElement,boolean bAttr, String fieldNameAttr) throws XMLStreamException, CoreException {
		
		Map<String,String> namevalues = new HashMap<String,String>();
		if( bAttr ){
			Iterator iter = rowElement.getAttributes();
			while( iter.hasNext() ){
				Attribute attr = (Attribute) iter.next();
				namevalues.put( attr.getName().getLocalPart() , attr.getValue() );
				
				
			}
			// Read the end element for row
			readEndElement(reader);

		}
		else {
			StartElement elem;
			while((elem = readStartElement(reader)) != null ){
				String name ;
				String value ;
				if( fieldNameAttr != null ){
					Attribute attr = elem.getAttributeByName(new QName(fieldNameAttr));
					if( attr == null){
						throw new CoreException("Failed to find field name attribute: " + fieldNameAttr );
					}
					name = attr.getValue();
				} else
					name = elem.getName().getLocalPart();
				
				
				value = readCharacters( reader ); // Includes up to end element
				namevalues.put( name , value );

			}
			// Got the EndElement for the row ... stop now
			
		}
		return namevalues ;
	}

	
	
	/*
	 * Read up to EndElement and include all charactors
	 */
	private String readCharacters(XMLEventReader reader) throws XMLStreamException {
		StringBuffer sb = new StringBuffer();
		while( reader.hasNext() ){
			XMLEvent event = reader.nextEvent();
			if( event.isCharacters() ){
				sb.append( event.asCharacters().getData() );
				
				
			}
			else
			if( event.isEndElement() || event.isEndDocument() )
				break ;
			
			
			
		}
		
		return sb.toString();
		
	}


	/*
	 * Read ahead until we get a StartElement or return null on EOF
	 */

	private StartElement readStartElement(XMLEventReader reader) throws XMLStreamException {
		
		while( reader.hasNext() ){
			XMLEvent event = reader.nextEvent();
			if( event.isStartElement() )
				return (StartElement) event ;
			if( event.isEndElement() )
				return null ;
			if( event.isEndDocument() )
				return null ;
		}
		return null;
		
		
	}

	private void readEndElement(XMLEventReader reader) throws XMLStreamException {
		
		while( reader.hasNext() ){
			XMLEvent event = reader.nextEvent();
			if( event.isEndElement() || event.isEndDocument() )
				return ;
		}
		return ;
		
	}

	private Map<String, Integer> describeTable(DatabaseMetaData dbmeta, String tableName) throws SQLException {
		
		HashMap<String,Integer>  map = new HashMap<String,Integer>();
		
		ResultSet rs = dbmeta.getColumns(null, null, tableName , "%");
		// Should be only one result
		while( rs.next()){
			
			map.put( rs.getString( "COLUMN_NAME" ), rs.getInt("DATA_TYPE"));
			
			
		}
		return map ;
	}


	private void runQuery(Connection conn,SerializeOpts serializeOpts, String root, String row, String query,
			boolean bAttr, String fetch) throws SQLException, IOException, InvalidArgumentException,
			XMLStreamException {
		Statement pStmt  = null ;
		ResultSet rs = null ;
		try {
			
			pStmt = conn.createStatement();
			if( fetch != null )
				pStmt.setFetchSize( Util.parseInt(fetch, 1));
			
			
			OutputPort stdout = getStdout();
			XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
			
			writer.writeStartDocument();		
			writer.writeStartElement(root);
			
			
			
			rs = pStmt.executeQuery(query);
			ResultSetMetaData meta = rs.getMetaData();
			
			while( rs.next()  ){
				
				addElement( writer , rs , row ,  bAttr , meta );
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			
			stdout.writeSequenceTerminator(serializeOpts);
			
			writer.close();
			
		
		} finally {
			try {
				if( rs != null ) rs.close();
				if( pStmt != null ) pStmt.close();
				if( conn != null ) conn.close();
			} catch( Exception e )
			{}
			
		}
	}
	


	private void runUpdate(Connection conn,SerializeOpts serializeOpts, String root, String row, String query,
			boolean bAttr) throws SQLException, IOException, InvalidArgumentException,
			XMLStreamException {
		Statement pStmt  = null ;
		ResultSet rs = null ;
		try {
			
			pStmt = conn.createStatement();
			
			OutputPort stdout = getStdout();
			XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
			
			writer.writeStartDocument();		
			writer.writeStartElement(root);
	
			
			int rows = pStmt.executeUpdate(query);
			writer.writeAttribute("rows", String.valueOf(rows) );

			
			
			writer.writeEndElement();
			writer.writeEndDocument();
			
			stdout.writeSequenceTerminator(serializeOpts);
			
			writer.close();
			
		
		} finally {
			try {
				if( rs != null ) rs.close();
				if( pStmt != null ) pStmt.close();
				if( conn != null ) conn.close();
			} catch( Exception e )
			{}
			
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
//Copyright (C) 2008,2009,2010 David A. Lee.
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
