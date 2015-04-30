package org.xmlsh.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.aws.util.AWSSDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;

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

		Options opts = getOptions("c=consistant");
		opts.parse(args);

		args = opts.getRemainingArgs();

		boolean bConsistant = opts.hasOpt("consistant");

		mSerializeOpts = this.getSerializeOpts(opts);

		try {
			getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage(e.getLocalizedMessage());
			return 1;

		}

		if (args.size() < 3) {
			usage(getName() + " table name/value [name/value]");
			return 1;

		}
		String table = args.remove(0).toString();
		int ret = -1;
		ret = getItem(table, parseKeys(args), bConsistant);
		return ret;

	}

	private Map<String, AttributeValue> parseKeys(List<XValue> args)
			throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {

		if (args.isEmpty())
			usage("table hash_key [range-key]");
		return parseAttributeValues(args);
	}

	private int getItem(String domainName, Map<String, AttributeValue> key,
			boolean bConsistantRead) throws IOException, XMLStreamException,
			SaxonApiException, CoreException {

		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);

		startDocument();
		startElement(getName());

		GetItemRequest getItemRequest = new GetItemRequest().withTableName(domainName).withKey(key)
				.withConsistentRead(bConsistantRead);

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

	private void writeItem(Map<String, AttributeValue> result)
			throws XMLStreamException, IOException {
		startElement("item");

		for (Entry<String, AttributeValue> a : result.entrySet())
			super.writeAttribute(a.getKey(), a.getValue());
		endElement();
	}

	public void usage() {
		super.usage();
	}

}
