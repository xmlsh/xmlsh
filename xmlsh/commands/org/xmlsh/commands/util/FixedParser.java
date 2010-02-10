package org.xmlsh.commands.util;

import org.xmlsh.util.Util;

/**
 * Parses lines with fixed width format into CSVRecord format
 * 
 * @author David A. Lee
 * @version $Revision$
 */
public class FixedParser
{
	private		ColSpec		mColspecs[];
	private		boolean	 	bNormalize;

	private	 static class ColSpec 
	{
		int		start ;
		int		end   ;
		
		ColSpec( int start , int end ){
			this.start = start ;
			this.end = end ;
		}
		ColSpec( String spec )
		{
			String pair[] = spec.split("-");
			start = Util.parseInt(pair[0],0);
			end = pair.length > 1 ?  
				Util.parseInt(pair[1],0) : 0 ;
			
		}
		
		
	};
	
	
	
	public FixedParser( String colspecs[] , boolean normalize )
	{
		bNormalize = normalize;
		mColspecs = new ColSpec[colspecs.length];
		int i = 0;
		for( String spec : colspecs )
			mColspecs[i++] = new ColSpec( spec );
		
		
	}
	
	
	/**
     * Parse a single line into String[] each string is 1 csv field
     */
     
     public CSVRecord parseLine( String line ){
        
    	 String[] list = new String[ mColspecs.length];
    	 int col = 0;
    	 for( ColSpec spec : mColspecs ){
    		 int start = spec.start - 1;
    		 int end   = spec.end ;
    		 
    		 if( start < 0 )
    			 start =0;
    		 
    		 if( end <= 0 || end > line.length() )
    			 end = line.length();
    		 String field = line.substring(start , end );
    		 if( bNormalize )
    			 field = field.trim();
    		 
    		 list[col++] = field ;
    		  
    	 }
    	 
        return new CSVRecord( list );
        
    }


    

}
