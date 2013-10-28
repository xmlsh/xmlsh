/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.ui;

import java.util.ArrayList;
import java.util.List;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.XdmVariable;

public abstract class MLQueryRequest extends MLRequest {

	private String mQuery;
	private XdmVariable[] mVariables = null;
	private RequestOptions mOptions ;
	


	public MLQueryRequest(String operation, String query, XdmVariable[] variables,
			RequestOptions options) throws InterruptedException {
		super(operation);
		mQuery = query;
		mVariables = variables;
		mOptions = options;
	}
	

	public MLQueryRequest(String operation, String query, XdmVariable variable,
			RequestOptions options) throws InterruptedException {
		this(operation,query, variable == null ? null : new XdmVariable[]{variable} , options );
	}
	
	

	@Override
	void run(MLRequestThread reqThread ) throws Exception {
		
	    Session mSession = reqThread.getConnection().newSession();

		AdhocQuery request = mSession.newAdhocQuery (mQuery );

		try {
			if( mVariables != null )
			  for( XdmVariable var : mVariables ) 
				  if( var != null )
				     request.setVariable(var);
					
			if(mOptions != null )
			   request.setOptions (mOptions);
			
		    ResultSequence rs = mSession.submitRequest(request);
		    onComplete(rs);
		
		    

		
		}
		finally {
			mSession.close();
		}
		
	}

	abstract void onComplete( ResultSequence rs) throws Exception;





	/**
	 * @param query the query to set
	 */
	protected void setQuery(String query) {
		mQuery = query;
	}



	/**
	 * @param variables the variables to set
	 */
	protected void setVariables(XdmVariable[] variables) {
		mVariables = variables;
	}



	/**
	 * @param options the options to set
	 */
	protected void setOptions(RequestOptions options) {
		mOptions = options;
	}


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