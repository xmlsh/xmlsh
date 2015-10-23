package org.xmlsh.util.commands;
import java.io.IOException;
import java.io.Writer;

import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.xmlsh.util.Util;
/**
 * 
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class CSVFormatter implements AutoCloseable
{
	CsvListWriter csvWriter;

	private char mDelim = ','; // csv
	private char mQuote = '"';

	public CSVFormatter(Writer w) {
		csvWriter = new CsvListWriter(w, CsvPreference.STANDARD_PREFERENCE );
	}

	public CSVFormatter( Writer w , char delim , char quote )
	{
		csvWriter = new CsvListWriter( w , 
				new CsvPreference.Builder(
						quote,delim,Util.getNewlineString()).build());
	}



	/**
	 * Helper method to write out an array of  strings as a CSV "record" onto the writer
	 * @throws IOException 
	 */

	@Deprecated
	public void encodeRow(String... csv) throws IOException
	{
		csvWriter.write(csv);
	}

	@Deprecated
	public  void encodeRow(CSVRecord rec) throws IOException
	{
		csvWriter.write( rec.getFields() );
	}
	
	public  void writeRow(CSVRecord rec) throws IOException
	{
		csvWriter.write( rec.getFields() );
	}
	
	public void writeRow(String... csv) throws IOException
	{
		csvWriter.write(csv);
	}
	public void writeHeader(String... csv) throws IOException{
		csvWriter.writeHeader(csv);
	}

	public void writeHeader(CSVRecord rec) throws IOException {
		csvWriter.writeHeader(rec.getFields());
		
	}

	@Override
	public void close() throws Exception {
		if( csvWriter != null ){
			csvWriter.close();
			csvWriter = null ;
		}
		
	}


}

//
//
//Copyright (C) 2008-2014    David A. Lee.
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
