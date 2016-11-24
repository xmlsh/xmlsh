package org.xmlsh.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import org.xmlsh.aws.util.AWSDDBCommand;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InputPort;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.commands.CSVParser;
import org.xmlsh.util.commands.CSVRecord;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.Capacity;
import com.amazonaws.services.dynamodbv2.model.ConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import net.sf.saxon.s9api.SaxonApiException;

public class ddbBatchWrite extends AWSDDBCommand {

  private String mDelim;
  private String mQuote;
  private boolean mBHeader;
  private String mListSep;
  private boolean bCSV;
  private boolean bXML;
  private XValue mColNames;
  private CSVRecord mHeader;
  private CSVParser mParser;
  private BufferedReader mReader;
  private XMLEventReader mXmlReader;
  private CSVRecord mColTypes;
  private int mMaxBatch;
  private String mTable;
  private ThreadPoolExecutor mPool;
  private int maxThreads = 10;

  private int entries(Map<String, List<WriteRequest>> items) {
    int n = 0;
    for(List<WriteRequest> list : items.values()) {
      n += list.size();

    }
    return n;
  }

  private class PutBatch implements Runnable {
    Map<String, List<WriteRequest>> requestItems;

    public PutBatch(Map<String, List<WriteRequest>> items) {

      requestItems = items;
    }

    @Override
    public void run() {
      int total = entries(requestItems);
      long start = System.currentTimeMillis();
      int n = total;
      while(n > 0) {

        System.out.println("Thread: "
            + Thread.currentThread().getName() + " Writing " + n);
        BatchWriteItemRequest request = new BatchWriteItemRequest();
        request.setRequestItems(requestItems);
        BatchWriteItemResult result = getAWSClient().batchWriteItem(request);
        requestItems = result.getUnprocessedItems();
        n = entries(requestItems);

      }

      System.out.println("Thread: " + Thread.currentThread().getName()
          + " elapsed: " + (System.currentTimeMillis() - start));
    }

  }

  /**
   * @param args
   * @throws IOException
   */
  @Override
  public int run(List<XValue> args) throws Exception {

    Options opts = getOptions(
        "expected:+,q=quiet,xml,csv,header,types:,delim:,quote:,colnames:,listSep,coltypes:,maxthreads:");
    opts.parse(args);

    args = opts.getRemainingArgs();

    setSerializeOpts(opts);

    bCSV = opts.hasOpt("csv");
    bXML = opts.hasOpt("xml");
    mMaxBatch = opts.getOptInt("batch", 25);
    maxThreads = opts.getOptInt("maxthreads", maxThreads);

    try {
      getDDBClient(opts);
    } catch (UnexpectedException e) {
      usage(e.getLocalizedMessage());
      return 1;

    }

    if(args.size() < 1) {
      usage(getName() + ":" + "table item attributes ...");

    }
    mTable = args.remove(0).toString();
    InputPort port = mShell.getEnv().getInput(
        args.size() == 0 ? null : args.get(0));

    if(bCSV) {
      mDelim = opts.getOptString("delim", ",");
      mQuote = opts.getOptString("quote", "\"");
      mBHeader = opts.hasOpt("header");
      mListSep = opts.getOptString("listSep", ":");
      if(opts.hasOpt("colnames")) {
        mColNames = opts.getOptValue("colnames");

      }
      mReader = new BufferedReader(port.asReader(getSerializeOpts()));

      mParser = new CSVParser(mReader, mDelim.charAt(0), mQuote.charAt(0));

      if(mBHeader)
        mHeader = mParser.parseLine();

      // Even if bHeader override the colnames
      if(mColNames != null)
        mHeader = parseCols(mColNames);
      mColTypes = parseCols(opts.getOptValue("coltypes"));
    }
    else if(bXML) {

    }
    else
      throw new UnexpectedException("Required -csv or -xml");

    int ret = -1;

    mPool = new ThreadPoolExecutor(maxThreads, maxThreads, 0L,
        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(
            maxThreads * 2),
        new ThreadPoolExecutor.CallerRunsPolicy());

    ret = batchWrite(mTable, port, opts.hasOpt("q"));

    return ret;

  }

  private CSVRecord parseCols(XValue mColNames2) throws UnimplementedException {
    throw new UnimplementedException("Not Implemented");
  }

  private int batchWrite(String tableName, InputPort port, boolean bQuiet)
      throws IOException, XMLStreamException, SaxonApiException,
      CoreException, InterruptedException {

    traceCall("batchWrite");

    Map<String, List<WriteRequest>> requestItems = readRequests(mMaxBatch);
    while(requestItems != null) {
      mPool.execute(new PutBatch(requestItems));
      requestItems = readRequests(mMaxBatch);
    }

    System.out.println("Shutting down and waiting to finish");
    mPool.shutdown();
    mPool.awaitTermination(1, TimeUnit.HOURS);
    return 0;

  }

  protected void writeConsumedCapacity(List<ConsumedCapacity> listCapacity)
      throws XMLStreamException {
    startElement("consumed-capacity");
    for(ConsumedCapacity cap : listCapacity)
      writeConsumedCapacity(cap);
    endElement();
  }

  protected void writeConsumedCapacity(ConsumedCapacity cap)
      throws XMLStreamException {
    startElement("capacity");

    attribute("units", Double.toString(cap.getCapacityUnits()));
    if(cap.getTable() != null)
      writeCapacity("table", cap.getTableName(), cap.getTable());
    if(cap.getGlobalSecondaryIndexes() != null)
      writeIndexCapacity("global", cap.getGlobalSecondaryIndexes());
    if(cap.getLocalSecondaryIndexes() != null)
      writeIndexCapacity("local", cap.getLocalSecondaryIndexes());
    endElement();

  }

  private void writeIndexCapacity(String string, Map<String, Capacity> indexes)
      throws XMLStreamException {
    startElement("index-capacity");
    attribute("type", string);
    for(String index : indexes.keySet()) {
      writeCapacity("index", index, indexes.get(index));
    }
    endElement();

  }

  private void writeCapacity(String type, String name, Capacity cap)
      throws XMLStreamException {
    startElement(type);
    attribute("name", name);
    attribute("capacity", Double.toHexString(cap.getCapacityUnits()));
    endElement();

  }

  private Map<String, List<WriteRequest>> readRequests(int maxBatch)
      throws IOException, UnexpectedException, XMLStreamException,
      UnimplementedException {

    Map<String, List<WriteRequest>> items = null;

    if(bXML) {
      items = readRequestsXML(maxBatch);
    }

    if(bCSV) {
      items = readRequestsCSV(maxBatch);
    }

    return items;
  }

  private Map<String, List<WriteRequest>> readRequestsCSV(int maxBatch)
      throws IOException, UnexpectedException, XMLStreamException,
      UnimplementedException {

    Map<String, List<WriteRequest>> result = new HashMap<String, List<WriteRequest>>();

    List<WriteRequest> requests = new ArrayList<WriteRequest>();
    while(maxBatch-- > 0) {
      WriteRequest w = readWriteRequestCSV();
      if(w == null)
        break;
      requests.add(w);

    }
    if(requests.isEmpty())
      return null;
    result.put(mTable, requests);

    return result;

  }

  private WriteRequest readWriteRequestCSV() throws UnexpectedException,
      IOException, XMLStreamException, UnimplementedException {

    Map<String, AttributeValue> item = readItemCSV(mReader, mParser,
        mHeader, mListSep, mColTypes);
    if(item == null)
      return null;
    return new WriteRequest(new PutRequest(item));
  }

  private Map<String, AttributeValue> readItemCSV(BufferedReader mReader2,
      CSVParser mParser2, CSVRecord mHeader2, String mListSep2,
      CSVRecord mColTypes2) throws UnimplementedException {
    throw new UnimplementedException("Not Implemented");
  }

  private Map<String, List<WriteRequest>> readRequestsXML(int maxBatch)
      throws UnimplementedException {
    // TODO Auto-generated method stub
    throw new UnimplementedException("Not Implemented");
  }

  @Override
  public void usage() {
    super.usage();
  }

}
