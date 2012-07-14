/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.twitter;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;


import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.twitter.util.TwitterCommand;
import org.xmlsh.twitter.util.TwitterWriter;
import org.xmlsh.util.Util;
import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.json.DataObjectFactory;



// implemets: https://dev.twitter.com/docs/api/1/get/search

public class stream extends TwitterCommand {

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(sCOMMON_OPTS + ",p=port:,track:,sample,json",SerializeOpts.getOptionDefs());
		opts.parse(args);
		mSerializeOpts = this.getSerializeOpts(opts);
		final boolean bJson = opts.hasOpt("json");
		
		
		args = opts.getRemainingArgs();
		
    	final OutputPort port = mShell.getEnv().getOutputPort(opts.getOptStringRequired("port"), true);
    	
    	

	    StatusListener listener = new StatusListener(  ) {

	    	
	        public void onStatus(Status status) {
	            try {
	            	if( bJson ) {
	            		String json = DataObjectFactory.getRawJSON(status);
	            	
		            		PrintWriter writer = port.asPrintWriter(mSerializeOpts);
		            		writer.println(json);
		            		writer.close();
		        	
	            		
	            		
	            	} else {
		        	    TwitterWriter mWriter = new  TwitterWriter( port.asXMLStreamWriter(mSerializeOpts));
		            	mWriter.startDocument();
		            	mWriter.startElement(TwitterWriter.kTWITTER_NS,"twitter");
		    			mWriter.writeDefaultNamespace();
		            	mWriter.write("status",status);
		            	mWriter.endElement();
		            	mWriter.endDocument();
		            	mWriter.closeWriter();
		            	port.writeSequenceTerminator(mSerializeOpts);
	            	}
	            	
				} catch (Exception e) {
					onException(e);
				}

	        }

	      
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	           // System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	        }

	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	           // System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
	        }

	        public void onScrubGeo(long userId, long upToStatusId) {
	           // System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	        }

	        public void onException(Exception ex) {
	           // ex.printStackTrace();
	        }



	    };
	    
		try {
			
			
			

	
			
			

		    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		    twitterStream.addListener(listener);
		  
		    // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
		    
		    
		    
		    
		    // FilterQuery filter = new FilterQuery().track(Util.toStringArray(args));
		    
		    
		    
			// twitterStream.filter(filter);
		    if( opts.hasOpt("sample"))
		    	twitterStream.sample();
		    else
		    	twitterStream.filter(new FilterQuery().track(opts.getOptStringRequired("track").split(",")));
		    	
		    	

	
		} finally {
			
		
		}
		return 0;
		
		
		
	}
}



//
//
//Copyright (C) 2008,2009,2010,2011,2012 David A. Lee.
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
