package org.xmlsh.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;


public class ddbPutItem	 extends  AWSDDBCommand {

	

	/**
	 * @param args
	 * @throws IOException 
	 */
	@Override
	public int run(List<XValue> args) throws Exception {
		Options opts = getOptions("update:,exists:,+r=replace,q=quiet:");

	//	Options opts = getOptions("expected:+,q=quiet");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		mSerializeOpts = this.getSerializeOpts(opts);
		
		String updateName = opts.getOptString("update", null);
		String updateExists = opts.getOptString("exists",null);
		boolean bReplace = opts.getOptFlag("replace",true);
	
		try {
			 getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		if( args.size() < 3 ){
			usage(getName()+ ":" + "table attributes ...");
			
		}
		String table = args.remove(0).toString();
		

		int ret = -1;
		ret = put(table,args,opts, opts.hasOpt("q"));

		
		
		return ret;
		
		
	}


	private int put(String tableName, List<XValue > args,Options opts,boolean bQuiet) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
		
		
	    Map<String,AttributeValue> itemMap = parseAttributeValues( args  );
		PutItemRequest putItemRequest = new PutItemRequest().
				withTableName(tableName).withItem(itemMap);
		
		Map<String, ExpectedAttributeValue> expected = parseExpected(opts);
		if( expected != null )
		    putItemRequest.setExpected(expected);
		
		
		
		traceCall("putItem");

		PutItemResult result = mAmazon.putItem(putItemRequest);
		
		
		if( ! bQuiet ){
			OutputPort stdout = this.getStdout();
			mWriter = stdout.asXMLStreamWriter(mSerializeOpts);
			emptyDocument();
			closeWriter();
			stdout.writeSequenceTerminator(mSerializeOpts);
			stdout.release();
		}	
		
		
		return 0;
		
	}


	private Map<String, ExpectedAttributeValue> parseExpected(Options opts) throws InvalidArgumentException, IOException {
		
		List<XValue> list = opts.getOptValues("expected");
		if( list == null )
		   return null ;
		
		
		Map<String, ExpectedAttributeValue> result = new  HashMap<String, ExpectedAttributeValue>();
		for( XValue xv : list ){
			StringPair namevalue=new StringPair( xv.toString() , '=');
			if(! namevalue.hasRight())
				result.put( namevalue.getLeft() , new ExpectedAttributeValue(true));
			
			else {
				StringPair typename = new StringPair( namevalue.getLeft(), ':');
				String type = typename.getLeft();
				String value = namevalue.getRight();
				
				AttributeValue  av= new AttributeValue();
				
				
				
				if( type == "N" )
					av.setN( value );
				else
				if( type == "NS")
					av.setNS( parseSS( new XValue(value )) );
				else
				if( type == "S" )
					av.setS( value );
				else
				if( type == "SS" )
				    av.setSS( parseSS( new XValue(value)));
				else
			    if( type == "B" )
			    	av.setB( parseBinary(value) );
			    else
			    if( type == "BS" ) 
			    	av.setBS( parseBS( new XValue(xv) ));
				
					
				result.put( namevalue.getRight() , new ExpectedAttributeValue(av));
					
				
			}
			
		}
		return result;
			
		
	}

	
	// pairs for 
	//  type:name value
	// if type is empty then == "S"

	public void usage() {
		super.usage();
	}



	

}
