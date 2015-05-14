package org.xmlsh.aws;

import java.io.IOException;
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
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;

public class ddbGetItem extends AWSDDBCommand {

	/**
	 * @param args
	 * @throws IOException
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = getOptions("table=table-name:,key-name:+,key-value:+,key:+,c=consistant,projection-expression:");
		opts.parse(args);

		args = opts.getRemainingArgs();


        setSerializeOpts(this.getSerializeOpts(opts));
		try {
			getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage(e.getLocalizedMessage());
			return 1;

		}

		int ret = -1;
		ret = getItem(opts);
		return ret;
	}

    private int getItem( Options opts ) throws XMLStreamException, SaxonApiException, IOException, CoreException{
		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

        String tableName = opts.getOptStringRequired("table");
        Map<String, AttributeValue> keys = parseKeyOptions(opts);
        boolean bConsistantRead = opts.hasOpt("consistant");
        
		startDocument();
		startElement(getName());

		GetItemRequest getItemRequest = new GetItemRequest().
		        withTableName(tableName).withKey(keys).
				withConsistentRead(bConsistantRead);

		if( opts.hasOpt("projection-expression"))
		    getItemRequest.setProjectionExpression( 
		            opts.getOptStringRequired("projection-expression")); 

		else {
		    if( opts.hasRemainingArgs() )
		        getItemRequest.setProjectionExpression( 
		                Util.stringJoin(Util.toStringList( opts.getRemainingArgs()),","));
		}
		    
		traceCall("getItem");

		GetItemResult result = mAmazon.getItem(getItemRequest);
		if (result.getItem() != null)
			writeItem(result.getItem());

		writeMetric( new RequestMetrics( result.getConsumedCapacity(),null) );
		endElement();
		endDocument();

		closeWriter();
		stdout.writeSequenceTerminator(getSerializeOpts());
		stdout.release();
		return 0;

	}
}
