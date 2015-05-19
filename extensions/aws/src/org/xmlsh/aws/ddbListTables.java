package org.xmlsh.aws;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.SaxonApiException;

import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.Options;
import org.xmlsh.core.OutputPort;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.XValue;

import com.amazonaws.services.dynamodbv2.model.ListTablesRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

public class ddbListTables extends AWSDDBCommand {

    private int kLIMIT = 100;
    /**
     * @param args
     * @throws IOException
     */
    @Override
    public int run(List<XValue> args) throws Exception {

        Options opts = getOptions("limit:,exclusive-start-table=last-table:");
        parseOptions(opts, args);


        setSerializeOpts(this.getSerializeOpts(opts));
        args = opts.getRemainingArgs();

        if (args.size() != 0) {
            usage();
            return 1;
        }


        try {
            getDDBClient(opts);
        } catch (UnexpectedException e) {
            usage(e.getLocalizedMessage());
            return 1;

        }

        int ret = -1;
        ret = list(opts);

        return ret;

    }

    private int list(Options opts) throws IOException, XMLStreamException,
    SaxonApiException, CoreException {

        OutputPort stdout = getStdout();
        mWriter = stdout.asXMLStreamWriter(getSerializeOpts());

        startDocument();
        startElement(getName());

        traceCall("listTables");

        int userLimit = opts.getOptInt("limit", 0);
        ListTablesRequest listTablesRequest = new ListTablesRequest();
        if (userLimit > 0)
            listTablesRequest.setLimit(Math.min(userLimit, kLIMIT));

        String lastTable = opts.getOptString("exclusive-start-table", null);

        int nlist = 0;
        do {
            if (lastTable != null)
                listTablesRequest.setExclusiveStartTableName(lastTable);
            ListTablesResult result = mAmazon.listTables(listTablesRequest);
            int n = result.getTableNames().size();
            if (n <= 0)
                break;
            writeTables(result.getTableNames());
            nlist += n;
            if (userLimit > 0 && nlist >= userLimit)
                break;
            lastTable = result.getLastEvaluatedTableName();
        } while (lastTable != null);

        endElement();
        endDocument();

        closeWriter();
        stdout.writeSequenceTerminator(getSerializeOpts());
        stdout.release();

        return 0;

    }

    private void writeTables(List<String> tableNames) throws XMLStreamException {
        for (String t : tableNames) {
            startElement("table");
            attribute("name", t);
            endElement();
        }
    }

}
