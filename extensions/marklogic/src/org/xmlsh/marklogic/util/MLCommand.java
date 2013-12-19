/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.marklogic.util;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.Options.OptionValue;
import org.xmlsh.core.XCommand;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.ContentbaseMetaData;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;

public abstract class MLCommand extends XCommand {

	protected	ContentSource	mContentSource = null ;
	protected 	Session 		mSession = null ;
	private    Logger mLogger = LogManager.getLogger(this.getClass());
	
	protected  PrintWriter		mOutput = null;

	protected boolean 		bVerbose = false ;
	
	public MLCommand() {
		super();
	}

	protected ContentSource getConnection(Options opts) throws Exception  {
		XValue vc = null;
		String connect;
		OptionValue ov = opts.getOpt("c");
		if (ov != null)
			vc = ov.getValue();
		else
			vc = getEnv().getVarValue("MLCONNECT");
		if (vc == null) {
			throw new InvalidArgumentException("No connection");
		}
		connect = vc.toString();

		URI serverUri = new URI(connect);
		ContentSource cs = ContentSourceFactory.newContentSource(serverUri,MLUtil.newTrustOptions(serverUri));
		return cs;
	}
	
	
	protected BigInteger[] parseForestIds(List<XValue> forests) throws RequestException {
		
		BigInteger bi[] = new BigInteger[ forests.size() ];
		
		ContentbaseMetaData meta = mSession.getContentbaseMetaData();
		int i = 0;
		for( XValue v : forests ){
			String forest = v.toString();
			BigInteger forest_id;
			if( Util.isInt(forest, false))
				forest_id = new BigInteger( forest );
			else
				forest_id = meta.getForestMap().get(forest);
			
			bi[i++] = forest_id ;
			
		}

		return bi;
		
		
		
		
		
	}

	
	

	// Dont use shell printError as these may be in non shell threads
		protected synchronized void printError( String error , Exception e )
		{		
			
			
			mOutput.println(error);
			mOutput.println(e.getMessage());
			for( Throwable t = e.getCause() ; t != null ; t = t.getCause() ){
				mOutput.println("  Caused By: " + t.getMessage());		
				
			}
			
			
			if( e != null )
				mLogger.error( error , e );
			
			mOutput.flush();
		}
	

		
		protected void print( String str )
		{
			if( bVerbose ){
				mOutput.println(str);
				mOutput.flush();
			}
				
		}

}

//
//
// Copyright (C) 2008-2014    David A. Lee.
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
