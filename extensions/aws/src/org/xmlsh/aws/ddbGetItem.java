package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
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

		Options opts = getOptions("table=table-name:,key-name:+,key-value:+,key:+,c=consistant");
		opts.parse(args);

		String tableName = opts.getOptStringRequired("table");

		Map<String, AttributeValue> keys = parseKeyOptions(opts);

        boolean bConsistant = opts.hasOpt("consistant");
		args = opts.getRemainingArgs();
 
		List<String> attrs = Util.toStringList(args);

		mSerializeOpts = this.getSerializeOpts(opts);

		try {
			getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage(e.getLocalizedMessage());
			return 1;

		}

		int ret = -1;
		ret = getItem(tableName, keys, attrs,bConsistant);
		return ret;

	}

    private int getItem(String tableName, Map<String, AttributeValue> key,
			List<String> attrs, boolean bConsistantRead) throws IOException, XMLStreamException,
			SaxonApiException, CoreException {

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);

		startDocument();
		startElement(getName());

		GetItemRequest getItemRequest = new GetItemRequest().withTableName(tableName).withKey(key)
				.withConsistentRead(bConsistantRead);
		if( attrs != null && !attrs.isEmpty())
		  getItemRequest.setProjectionExpression(Util.stringJoin(attrs, ","));
		
		traceCall("getItem");

		GetItemResult result = mAmazon.getItem(getItemRequest);
		if (result.getItem() != null)
			writeItem(result.getItem());

		endElement();
		endDocument();

		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();
		return 0;

	}
}
