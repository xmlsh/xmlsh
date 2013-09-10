/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.sh.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.s9api.XdmEmptySequence;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

@SuppressWarnings("serial")
public class WordList extends ArrayList<Word> {

	public void print(PrintWriter out) {
		for( Word s : this ){
			s.print( out );
			out.print( " ");
		}
		
	}

	public XValue expand(Shell shell, boolean bExpandWild, boolean bExpandWords, boolean bTongs, SourceLocation loc) throws IOException, CoreException {
		if( this.size() == 0 )
			return new XValue(XdmEmptySequence.getInstance());
		if( this.size() == 1 )
			return this.get(0).expand(shell, bExpandWild, bExpandWords,bTongs ,loc);
		
		
		List<XValue>  list = new ArrayList<XValue>( this.size() );
		
		for( Word w : this )
			list.add(w.expand(shell,bExpandWild,bExpandWords,bTongs,loc) );
		return new XValue( list );
		
	}
	
}



//
//
//Copyright (C) 2008-2013    David A. Lee.
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
