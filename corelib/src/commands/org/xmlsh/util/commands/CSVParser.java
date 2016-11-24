package org.xmlsh.util.commands;

import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.xmlsh.util.Util;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 * 
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class CSVParser {

  private CsvListReader csvReader;

  public CSVParser(Reader r, CsvPreference p ) {
    csvReader = new org.supercsv.io.CsvListReader(r,p);
  }

  public CSVParser(Reader r) {
    this(r, CsvPreference.STANDARD_PREFERENCE);
  }

  public CSVParser(Reader r, char delim, char quote) {
    csvReader = new CsvListReader(r,
        new CsvPreference.Builder(quote, delim, Util.getNewlineString())
            .build());
  }

  public CSVParser(Reader r, char delim, char quote, int max) {
    csvReader = new CsvListReader(r,
        new CsvPreference.Builder(quote, delim, Util.getNewlineString())
            .build());
  }

  /**
   * Parse a single line into String[] each string is 1 csv field
   * If combine
   * 
   * @throws IOException
   */

  public CSVRecord parseLine() throws IOException {
    List<String> row = csvReader.read();
    if(row == null)
      return null;
    return new CSVRecord(row);
  }

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
