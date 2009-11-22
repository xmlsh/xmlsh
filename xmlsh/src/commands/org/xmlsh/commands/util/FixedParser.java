package org.xmlsh.commands.util;

import java.util.ArrayList;

/**
 * Parses lines with fixed width format into CSVRecord format
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class FixedParser
{
	private		int			widths[];
	private		boolean	 	normalize;

	
	public FixedParser( int widths[] , boolean normalize )
	{
		this.widths = widths;
		this.normalize = normalize;
		
	}
	
	
	/**
     * Parse a single line into String[] each string is 1 csv field
     */
     
     public CSVRecord parseLine( String line ){
        
    	 String[] list = new String[ widths.length];
    	 int col = 0;
    	 int start = 0;
    	 while( col < widths.length ){
    		 int width = widths[col];
    		 int end = start + width;
    		 
    		 if( end > line.length() )
    			 end = line.length();
    		 String field = line.substring(start , end );
    		 if( normalize )
    			 field = field.trim();
    		 
    		 list[col] = field ;
    		 start += width;
    		 col++;
    		  
    	 }
    	 
        return new CSVRecord( list );
        
    }


    

}
