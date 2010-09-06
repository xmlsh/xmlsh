/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.commands.internal;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.Util;

public class xsql extends XCommand {

	
	private boolean bAttr = false ;
	

	@Override
	public int run(List<XValue> args) throws Exception {

		String fieldXPath  = "name()";
		String tableNameXPath = "name()";

		String tableRowXPaht 	 = "/*/*";
		
		Properties options = null;
		
		Options opts = new Options( "cp=classpath:,d=driver:,u=user:,p=password:,root:,row:,attr,c=connect:,q=query:,o=option:+,insert,update=execute,tableAttr:,fieldAttr:" ,SerializeOpts.getOptionDefs() );
		opts.parse(args);
		
		String root = opts.getOptString("root", "root");
		String row = opts.getOptString("row", "row");
		
		String connect = opts.getOptStringRequired("c");
		String query = opts.getOptString("q", null);
		String driver = opts.getOptStringRequired("driver");
		String user = opts.getOptString("user",null);
		String password = opts.getOptString("password",null);
		boolean bInsert = opts.hasOpt("insert");
		boolean bUpdate = opts.hasOpt("update");
		String tableAttr = opts.getOptString("tableAttr", null);
		if( tableAttr != null )
			tableNameXPath = "string(@" + tableAttr +")";
		
		String fieldAttr = opts.getOptString("fieldAttr",null);
		if( fieldAttr != null )
			fieldXPath = "string(@" + fieldAttr + ")";
		

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
		

		
		
		List<XValue> xvargs = opts.getRemainingArgs();
		if( query == null   &&  ! xvargs.isEmpty() )
			query = xvargs.get(0).toString();
		
		Connection conn = getConnection(driver, classloader, connect ,user,password, options );
		
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
				XdmNode	context = in.asXdmNode(this.getSerializeOpts(opts));
				runInsert( conn ,  getSerializeOpts(opts) , context , tableRowXPaht, tableNameXPath , fieldXPath  ) ;
		
			}
			else
				runQuery(conn,  getSerializeOpts(opts), root, row, query, bAttr );
		}
		finally {
			conn.close();
			
		}
		
		return 0;
		
		
		
	}

	private void runInsert(Connection conn, SerializeOpts serializeOpts, XdmNode context , String tableRowXPath , String tableNameXPath, String fieldXPath) throws SaxonApiException, SQLException, XMLStreamException, IOException, InvalidArgumentException {
		

		OutputPort stdout = getStdout();
	
		XMLStreamWriter writer = stdout.asXMLStreamWriter(serializeOpts);
		
		writer.writeStartDocument();		
		writer.writeStartElement("results");
		
		
		XQueryCompiler xqueryCompiler;
		
		
		
		Processor processor = Shell.getProcessor();
		XPathCompiler xpathCompiler = processor.newXPathCompiler();
		xqueryCompiler = processor.newXQueryCompiler();
		
		XPathExecutable tableNameExe = xpathCompiler.compile(tableNameXPath);
		XPathExecutable fieldNameExe = xpathCompiler.compile(fieldXPath);
		XPathExecutable dataRowExe = xpathCompiler.compile("*");
		XPathExecutable fieldRowExe = xpathCompiler.compile("*");
		XPathExecutable fieldValueExe = xpathCompiler.compile("string()");

		
		
		/*
		 * Create a list of all table elements
		 */
		
		XQueryExecutable tableExpr = xqueryCompiler.compile( tableRowXPath );
		XQueryEvaluator tableEval = tableExpr.load();
		if( context != null )
			tableEval.setContextItem(context);

		
		DatabaseMetaData  dbmeta = conn.getMetaData();
		
		/*
		 * For each table element
		 */
		for( XdmItem table : tableEval ){
			
			/*
			 *  Get the name of the table
			 */
			
			String tableName = evalString( tableNameExe , table );

			/*
			 * Get the table metadata
			 */

			// Map<String,Integer> tableDef = describeTable( dbmeta , tableName );
			
			
			
			
			/*
			 * For each row ...
			 */
			XPathSelector rowSel = dataRowExe.load();
			rowSel.setContextItem(table);
				
			
			int allRows = 0;
			for( XdmItem row :  rowSel.evaluate() ){
				// row is a single table row
				// Each child element is a single field
				
				XPathSelector fieldSel = fieldRowExe.load();
				fieldSel.setContextItem(row);
					
				
				
				
				boolean bFirst = true ;
				StringBuffer 	sNames = new StringBuffer();
				StringBuffer	sQuestions = new StringBuffer();
				
				
				ArrayList<String>	values = new ArrayList<String>();
				for( XdmItem field :  fieldSel.evaluate() ){
					String name = evalString( fieldNameExe , field );
					String value = evalString( fieldValueExe , field  );
					
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
					
						
			
			
			
		}
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();
		
		
		
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

	private String evalString(XPathExecutable exe, XdmItem context) throws SaxonApiException {
		
		XPathSelector sel = exe.load();
		sel.setContextItem(context);
		return new XValue(sel.evaluateSingle()).toString();
		
		
	}

	private void runQuery(Connection conn,SerializeOpts serializeOpts, String root, String row, String query,
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
	
	private Connection getConnection(String driver, ClassLoader classloader, String connect, String user, String password, Properties options) throws SQLException, SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		Class<?> cls = loadDriver(driver,classloader);
	
		
	    java.util.Properties info = new java.util.Properties();
	    if( options != null )
	    	info.putAll(options);

	
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
	  private  Class<?> loadDriver (String driver,ClassLoader classloader) throws SQLException {



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
