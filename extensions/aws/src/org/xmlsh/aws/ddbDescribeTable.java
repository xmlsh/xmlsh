package org.xmlsh.aws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public class ddbDescribeTable extends AWSDDBCommand {

	/**
	 * @param args
	 * @throws IOException
	 */
	@Override
	public int run(List<XValue> args) throws Exception {

		Options opts = getOptions("table:");
		opts.parse(args);

		args = opts.getRemainingArgs();

		if (args.size() != 0) {
			usage();
			return 1;
		}

		mSerializeOpts = this.getSerializeOpts(opts);

		try {
			getDDBClient(opts);
		} catch (UnexpectedException e) {
			usage(e.getLocalizedMessage());
			return 1;

		}

		int ret = -1;
		ret = describe(  opts);

		return ret;

	}

	private int describe(Options opts) throws IOException,
			XMLStreamException, SaxonApiException, CoreException {

	    String tableName = opts.getOptStringRequired("table");
	    
		OutputPort stdout = this.getStdout();
		mWriter = stdout.asXMLStreamWriter(mSerializeOpts);

		startDocument();
		startElement(getName());


		DescribeTableRequest describeTableRequest = new DescribeTableRequest()
				.withTableName(tableName);


		traceCall("describeTable");

		DescribeTableResult result = mAmazon.describeTable(describeTableRequest);
		writeTableDescription( result.getTable() );

		endElement();
		endDocument();

		closeWriter();
		stdout.writeSequenceTerminator(mSerializeOpts);
		stdout.release();

		return 0;

	}

}
