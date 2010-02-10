package org.xmlsh.commands.util;

import java.util.List;

/**
 * A single 'record' of CVS.
 * <br>
 * Implemented as String[]
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class CSVRecord
{
    private String[]    mFields;
    
    CSVRecord( String[] fields )
    {
        mFields = fields;
    }
    
    
    public CSVRecord(List<String> fields) {
		this( fields.toArray(new String[fields.size()]));
	}


	public String getField( int id )
    {
        return id < mFields.length ? mFields[id] : "" ; //$NON-NLS-1$
    }    

    public int getNumFields() {
        return mFields.length;
    }

    String[] getFields() { return mFields; }


}
