/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.twitter;

import java.util.List;


import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.twitter.util.TwitterCommand;
import org.xmlsh.twitter.util.TwitterWriter;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;



// implemets: https://dev.twitter.com/docs/api/1/get/search

public class search extends TwitterCommand {
	
	private TwitterWriter mWriter;

	@Override
	public int run(List<XValue> args) throws Exception {
		
		Options opts = new Options(sCOMMON_OPTS + ",q=query:,geo=geocode:,lang:,locale:,page:,t=result_type:,rpp:,until:,since_id:,max_id:,include_entities:,sanitize",SerializeOpts.getOptionDefs());
		opts.parse(args);
		mSerializeOpts = this.getSerializeOpts(opts);
		final boolean bSanitize = opts.hasOpt("sanitize");

		
		args = opts.getRemainingArgs();
		
		try {
			
			
			 Twitter twitter = new TwitterFactory().getInstance();
			 Query query = new Query();
			 



			
			if( opts.hasOpt("query") )
				query.setQuery(opts.getOptStringRequired("query"));
	
			if( opts.hasOpt("lang") )
				query.setLang(opts.getOptStringRequired("lang"));
	
			if( opts.hasOpt("locale") )
				query.setLocale(opts.getOptStringRequired("locale"));
	
			if( opts.hasOpt("page") )
				query.setPage( opts.getOptValue("page").toInt());
	
			if( opts.hasOpt("result_type") )
				query.setResultType(opts.getOptStringRequired("result_type"));
	
			if( opts.hasOpt("rpp") )
				query.setRpp(opts.getOptValue("rpp").toInt());
	
			
			if( opts.hasOpt("until") )
				query.setUntil(opts.getOptStringRequired("until"));
	
			if( opts.hasOpt("since_id") )
				query.setSinceId(opts.getOptValue("since_id").toLong());
			if( opts.hasOpt("since") )
				query.setUntil(opts.getOptStringRequired("since"));
			
			if( opts.hasOpt("max_id") )
				query.setSinceId(opts.getOptValue("max_id").toLong());			
			
			
            QueryResult result = twitter.search(query);
            List<Tweet> tweets = result.getTweets();
            
			
			

			OutputPort out = this.getStdout();
			mWriter = new TwitterWriter( out.asXMLStreamWriter( mSerializeOpts  ),bSanitize);
			
			mWriter.startDocument();
			mWriter.startElement("twitter");
			mWriter.writeDefaultNamespace();

			for( Tweet t : tweets )
				mWriter.write( t );

			
			
			mWriter.endElement();
			mWriter.endDocument();
			mWriter.closeWriter();
	
				
			out.release();
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
