/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import com.marklogic.xcc.Content;
import com.marklogic.xcc.Session;

public abstract class MLPutRequest extends MLRequest {

	private Content[] contents;
	

	public MLPutRequest( Content content) throws InterruptedException {
		this( new Content[] { content } );
		
	}
	
	public MLPutRequest( Content[] contents  ) throws InterruptedException {
		super(contents.length > 1 ? "Putting files ..." : "Putting File ...");
		this.contents = contents;

	}


	@Override
	void run(MLRequestThread reqThread) throws Exception {
	    Session mSession = reqThread.getConnection().newSession();
	    try {
		    mSession.insertContent(contents);
		    mSession.close();
		    onComplete(true);
	    } catch(Exception e){
	    	onComplete( false );
	    	throw e ;
	    }
	    
	    finally {
	    	mSession.close();
	    }

	}

	abstract void onComplete(boolean bSuccess);
	


}



/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */