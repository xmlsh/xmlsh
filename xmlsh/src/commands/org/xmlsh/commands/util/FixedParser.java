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
    		 
    		 // Line shorter then start position
    		 if( start >= line.length() )
    			 break ;
    		 
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

