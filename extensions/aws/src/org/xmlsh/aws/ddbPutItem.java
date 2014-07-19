package org.xmlsh.aws;

import net.sf.saxon.s9api.SaxonApiException;
import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.StringPair;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

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

		Options opts = getOptions("expected:+,q=quiet,xml,csv,header,types:,delim:,quote:,colnames:");
		opts.parse(args);

		args = opts.getRemainingArgs();
		
		setSerializeOpts(this.getSerializeOpts(opts));
		
		String delim = opts.getOptString("delim", ",");
		String quote = opts.getOptString("quote", "\"");
		boolean bHeader = opts.hasOpt("header");
		
		try {
			 getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage( e.getLocalizedMessage() );
			return 1;
			
		}
		
		if( args.size() < 1 ){
			usage(getName()+ ":" + "table item attributes ...");
			
		}
		String table = args.remove(0).toString();
		

		int ret = -1;
		Map<String, AttributeValue> itemMap = readItem(args, opts);
		Map<String, ExpectedAttributeValue> expected = parseExpected(opts);
		ret = put(table,itemMap, expected, opts.hasOpt("q"));

		
		
		return ret;
		
		
	}


	private int put(String tableName, Map<String,AttributeValue> itemMap,Map<String, ExpectedAttributeValue> expected, boolean bQuiet) throws IOException, XMLStreamException, SaxonApiException, CoreException 
	{
	
		PutItemRequest putItemRequest = new PutItemRequest().withTableName(tableName).withItem(itemMap);
		
		if( expected != null )
		    putItemRequest.setExpected(expected);
		
		
		traceCall("putItem");

		PutItemResult result = mAmazon.putItem(putItemRequest);
		
		if( ! bQuiet ){
			OutputPort stdout = this.getStdout();
			mWriter = stdout.asXMLStreamWriter(getSerializeOpts());
			emptyDocument();
			closeWriter();
			stdout.writeSequenceTerminator(getSerializeOpts());
			stdout.release();
		}	
		
		
		return 0;
		
	}


	private Map<String, AttributeValue> readItem(List<XValue> args, Options opts)
			throws IOException, UnexpectedException, XMLStreamException, CoreException {
		Map<String, AttributeValue> itemMap;
		if( opts.hasOpt("xml"))
			itemMap = readAttributesXML( mShell.getEnv().getInput(args.size() == 0 ? null : args.get(0) ).asXMLEventReader(getSerializeOpts()));
			
		else	
	       itemMap = getAttributes( args  );
		return itemMap;
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


	

	public void usage() {
		super.usage();
	}



	

}
