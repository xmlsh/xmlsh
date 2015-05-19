/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

import org.xmlsh.aws.util.DDBTypes.AttrType;
import org.xmlsh.aws.util.DDBTypes.IAttrNameExpr;
import org.xmlsh.aws.util.DDBTypes.IAttrObjectExpr;
import org.xmlsh.aws.util.DDBTypes.IAttrValueExpr;
import org.xmlsh.aws.util.DDBTypes.IKeyAttrValueMap;
import org.xmlsh.aws.util.DDBTypes.INameObjectMap;
import org.xmlsh.aws.util.DDBTypes.KeyAttrValueMap;
import org.xmlsh.aws.util.DDBTypes.NameMap;
import org.xmlsh.core.CoreException;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Base64;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Capacity;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.ItemCollectionMetrics;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public abstract class AWSDDBCommand extends AWSCommand {
    public static final String sATTR_NAME_EXPR_OPTIONS = "ne=attr-name-expr:+";

    public static final String sATTR_EXPR_OPTIONS = Options.joinOptions("ve=attr-value-expr:+",sATTR_NAME_EXPR_OPTIONS);
    public static final String sTABLE_OPTIONS = "t=table:";
    public static final String sKEY_OPTIONS = "k=key:,kn=key-name:+,kv=key-value:+";
    public static final String sCONDITION_OPTIONS = "c=condition:";
    public static final String sRETURN_OPTIONS = "return=return-values:";
    public static final String sDDB_COMMON_OPTS = "q=quiet:,o+:";
    public static final String sDOCUMENT_OPTS = "document,j=json";
    public static final String sCONSISTANT_OPTS = "c=consistant";
    
    protected boolean bQuiet = false ;
    
    static Object parseToJavaValue(String v) {
        if( v == null )
            return null ;
        if( v.isEmpty() )
            return v;
        Number num = JavaUtils.toNumber( v , null  );
        if( num != null )
            return num ;
        return v;
    }
    protected AmazonDynamoDB mAmazon;
    private DynamoDB mDynamo = null;
    private DynamoDBMapper mMapper = null;

    public static class RequestMetrics {
        public int count;
        public int scanCount;
        public ConsumedCapacity capacity;
        public ItemCollectionMetrics itemMetrics;

        public RequestMetrics(int count, int scanCount, ConsumedCapacity capacity) {
            super();
            this.count = count;
            this.scanCount = scanCount;
            this.capacity = capacity;
        }

        public RequestMetrics(ConsumedCapacity consumedCapacity, ItemCollectionMetrics itemCollectionMetrics) {
            count = 0;
            scanCount = 0;
            this.capacity = consumedCapacity;
            this.itemMetrics = itemCollectionMetrics;
        }
    }

    public AWSDDBCommand() {
        super();
    } 
    @Override
    protected void parseCommonOptions(Options opts) throws InvalidArgumentException {
      {
          super.parseCommonOptions(opts);
          bQuiet = opts.hasOpt("quiet");
      }
            
     }
    void binary(byte[] array) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Base64.OutputStream b64 = new Base64.OutputStream(os, Base64.ENCODE);
        b64.write(array);
        b64.close();

    }

    protected Object getClient() {
        return mAmazon;
    }

    protected void getDDBClient(Options opts) throws UnexpectedException, InvalidArgumentException {
        mAmazon = new AmazonDynamoDBClient(
                new AWSCommandCredentialsProviderChain(mShell, opts)
                );
        setDDBClientOpts(opts);
    }

    private void setDDBClientOpts(Options opts) throws InvalidArgumentException {
        setEndpoint(opts);
        setRegion(opts);
    }

    @Override
    protected void setEndpoint(Options opts) throws InvalidArgumentException {
        // TODO Auto-generated method stub
        super.setEndpoint(opts);
    }
    @Override
    protected void setRegion(Options opts) {
        if( mShell.getEnv().getVarString("AWS_DDBLOCAL") != null )
            setEndpoint( mShell.getEnv().getVarString("AWS_DDBLOCAL") );
        else
            super.setRegion(opts);

    }
    @Override
    public int run(List<XValue> args) throws Exception {
        // TODO Auto-generated method stub
        return 0;
    }
    private void setDynamoDBOpts(Options options) {
    }

    protected DynamoDB getDynamotDB(Options opts) throws UnexpectedException, InvalidArgumentException {
        if (mAmazon == null)
            getDDBClient(opts);
        assert (mAmazon != null);
        if (mDynamo == null)
            mDynamo = new DynamoDB(mAmazon);
        setDynamoDBOpts(opts);
        return mDynamo;
    }

    protected DynamoDBMapper getMapper(Options opts) throws UnexpectedException, InvalidArgumentException {
        if (mAmazon == null)
            getDDBClient(opts);
        assert (mAmazon != null);
        if (mMapper == null)
            mMapper = new DynamoDBMapper(mAmazon);
        setMapperOpts(opts);
        return mMapper;
    }

    private void setMapperOpts(Options opts) {

    }

    protected AttributeDefinition parseKeyAttribute(XValue xv)
            throws InvalidArgumentException, UnexpectedException {
        DDBTypes.NameType ant = new DDBTypes.NameType(xv);
        return new AttributeDefinition().withAttributeName(ant.getName())
                .withAttributeType(ant.getTypeName());

    }

    // Parses one global secondary index
    protected GlobalSecondaryIndex parseGlobalSecondaryIndex(XValue xv)
            throws InvalidArgumentException, UnexpectedException {
        if (!xv.isXdmNode())
            throw new InvalidArgumentException(
                    "Unexpected argument type for global secondary index: "
                            + xv.getTypeName());

        GlobalSecondaryIndex gi = new GlobalSecondaryIndex();

        gi.setIndexName(xv.xpath(getShell(), "./@name").toString());
        gi.setKeySchema(parseKeySchemaList(xv.xpath(getShell(), "./key-schema")));
        gi.setProjection(parseProjection(xv.xpath(getShell(), "./projection")));
        gi.setProvisionedThroughput(parseProvisionedThroughput(xv.xpath(
                getShell(), "./provisioned-throughput")));

        return gi;
    }

    protected KeySchemaElement parseKeySchemaElement(XValue xv) throws InvalidArgumentException {

        // Note: overload of 'type' for hash/range
        DDBTypes.NameType ant = new DDBTypes.NameType(xv);
        KeySchemaElement keyElement = new KeySchemaElement().withAttributeName(
                ant.getName()).withKeyType(
                        KeyType.valueOf(ant.getTypeName().toUpperCase()));
        return keyElement;
    }

    private Collection<KeySchemaElement> parseKeySchemaList(XValue xv)
            throws UnexpectedException {
        Collection<KeySchemaElement> list = new ArrayList<KeySchemaElement>();
        if (xv.isXdmNode()) {
            // value->sequence->item
            for (XdmItem item : xpath(xv, "key-schema/key-element-name")
                    .asXdmValue()) {
                if (item instanceof XdmNode) {
                    DDBTypes.NameType ant = new DDBTypes.NameType((XdmNode) item);
                    list.add(new KeySchemaElement(ant.getName(), ant.getTypeName()));
                }
                else
                    throw new UnexpectedException("Unexpected node type: "
                            + item.getClass().getName());
            }
        } else
            throw new UnexpectedException("Unexpected type: "
                    + xv.getTypeName());
        return list;

    }

    protected LocalSecondaryIndex parseLocalSecondaryIndex(XValue xv)
            throws InvalidArgumentException, UnexpectedException {

        if (!xv.isXdmNode())
            throw new InvalidArgumentException(
                    "Unexpected argument type for global secondary index: "
                            + xv.getTypeName());
        XdmNode node = xv.asXdmNode();
        if (!node.getNodeName().getLocalName().equals("local-secondary-index"))
            throw new InvalidArgumentException("Unexpected element: "
                    + node.getNodeName().toString()
                    + " expected: local-secondary-index");

        LocalSecondaryIndex li = new LocalSecondaryIndex().withIndexName(node
                .getAttributeValue(new QName("index-name")));

        li.setKeySchema(parseKeySchemaList(xpath(xv, "./key-schema")));
        return li;

    }

    private Projection parseProjection(XValue xv) throws UnexpectedException,
    InvalidArgumentException {
        if (xv.isXdmNode()) {
            XdmNode node = xv.asXdmNode();
            if (!node.getNodeName().getLocalName().equals("projection"))
                throw new UnexpectedException("unexpected element name: "
                        + node.getNodeName().toString());

            Projection p = new Projection().withProjectionType(
                    node.getAttributeValue(new QName("projection-type")))
                    .withNonKeyAttributes(
                            xpath(xv, "./non-key-attribute/string()")
                            .asStringList());
            return p;
        } else
            throw new UnexpectedException("Unexpected type: "
                    + xv.getTypeName());

    }

    private ProvisionedThroughput parseProvisionedThroughput(XValue xpath) {
        return null;
    }

    @Override
    public void setEndpoint(String endpoint) {
        mAmazon.setEndpoint(endpoint);
        super.setEndpointSet(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
     */
    @Override
    public void setRegion(String region) {
        if( Util.isBlank(region) ||   super.hasSetEndpoint())
            return ;
        if( region.equals("local"))
            setEndpoint("http://localhost:8000");
        else
          mAmazon.setRegion(RegionUtils.getRegion(region));

    }

    protected void writeAttribute(String key, AttributeValue avalue)
            throws XMLStreamException, IOException {

        startElement("attribute");
        attribute("name", key);
        writeAttributeValue(avalue);

        endElement();
    }

    protected void writeAttributeValue(AttributeValue avalue)
            throws XMLStreamException, IOException {
        if (avalue.getS() != null) {
            attribute(AttrType.S);
            characters(avalue.getS());
        }
        else if (avalue.getN() != null) {
            attribute(AttrType.N);
            characters(avalue.getN());
        }
        else

            if (avalue.getB() != null) {
                attribute(AttrType.B);
                binary(avalue.getB().array());
            }
            else if (avalue.getSS() != null) {
                attribute(AttrType.SS);

                for (String s : avalue.getSS()) {
                    startElement("value");
                    characters(s);
                    endElement();
                }

            }
            else if (avalue.getNS() != null) {
                attribute(AttrType.NS);

                for (String s : avalue.getNS()) {
                    startElement("value");
                    characters(s);
                    endElement();
                }
            }
            else if (avalue.getBS() != null) {
                attribute(AttrType.BS);

                for (ByteBuffer s : avalue.getBS()) {
                    startElement("value");
                    binary(s.array());
                    endElement();
                }
            }
            else if (avalue.getL() != null) {
                attribute(AttrType.L);
                for (AttributeValue av : avalue.getL()) {
                    startElement("value");
                    writeAttributeValue(av);
                    endElement();
                }
            } else if (avalue.getM() != null) {
                attribute(AttrType.M);
                for (Entry<String, AttributeValue> e : avalue.getM().entrySet()) {
                    writeAttribute(e.getKey(), e.getValue());
                }
            } else if (avalue.isBOOL() != null) {
                attribute(AttrType.BOOL);
                characters(avalue.getBOOL().booleanValue() ? "true" : "false");
            } else if (avalue.isNULL() != null) {
                attribute(AttrType.NULL);
                characters(avalue.getNULL().booleanValue() ? "true" : "false");
            }

    }

    protected void attribute(AttrType t) throws XMLStreamException {
        attribute("type", t.name());
    }

    private void writeAttributeDefinition(AttributeDefinition def)
            throws XMLStreamException {
        startElement("attribute-definition");
        attribute("name", def.getAttributeName());
        attribute("type", def.getAttributeType());
        endElement();
    }

    private void writeAttributeDefinitions(
            List<AttributeDefinition> attributeDefinitions)
                    throws XMLStreamException {
        startElement("attribute-definitions");
        for (AttributeDefinition def : attributeDefinitions)
            writeAttributeDefinition(def);
        endElement();

    }

    private void writeGlobalSecondaryIndex(GlobalSecondaryIndexDescription index)
            throws XMLStreamException {

        startElement("global-secondary-index");
        attribute("index-name", index.getIndexName());
        attribute("index-size", index.getIndexSizeBytes());
        attribute("index-status", index.getIndexStatus());
        attribute("item-count", index.getItemCount());
        writeKeySchemaList(index.getKeySchema());
        writeProjection(index.getProjection());
        writeProvisionedThroughput(index.getProvisionedThroughput());
        endElement();

    }

    private void writeGlobalSecondaryIndexes(
            List<GlobalSecondaryIndexDescription> globalSecondaryIndexes)
                    throws XMLStreamException {
        startElement("global-secondary-indexes");
        if (globalSecondaryIndexes != null)
            for (GlobalSecondaryIndexDescription index : globalSecondaryIndexes)
                writeGlobalSecondaryIndex(index);
        endElement();

    }

    private void writeKeySchemaList(List<KeySchemaElement> keySchema)
            throws XMLStreamException {

        startElement("key-schema");
        for (KeySchemaElement key : keySchema) {
            startElement("key-schema-element");
            attribute("name", key.getAttributeName());
            attribute("type", key.getKeyType());
            endElement();
        }

    }

    private void writeLocalSecondaryIndex(LocalSecondaryIndexDescription index)
            throws XMLStreamException {

        startElement("local-secondary-index");
        attribute("index-name", index.getIndexName());
        attribute("index-size", index.getIndexSizeBytes());
        attribute("item-count", index.getItemCount());

        writeKeySchemaList(index.getKeySchema());
        writeProjection(index.getProjection());
        endElement();

    }

    private void writeLocalSecondaryIndexes(
            List<LocalSecondaryIndexDescription> localSecondaryIndexes)
                    throws XMLStreamException {
        startElement("local-secondary-indexes");
        if (localSecondaryIndexes != null)
            for (LocalSecondaryIndexDescription index : localSecondaryIndexes)
                writeLocalSecondaryIndex(index);
        endElement();

    }

    private void writeProjection(Projection projection)
            throws XMLStreamException {
        startElement("projection");
        attribute("projection-type", projection.getProjectionType());
        for (String s : projection.getNonKeyAttributes())
            textElement("non-key-attribute", s);

        endElement();

    }

    private void writeProvisionedThroughput(
            ProvisionedThroughputDescription provisionedThroughput)
                    throws XMLStreamException {
        startElement("provisioned-throughput");
        attribute("last-decrease",
                provisionedThroughput.getLastDecreaseDateTime());
        attribute("last-increase",
                provisionedThroughput.getLastIncreaseDateTime());
        attribute("decreases-today",
                provisionedThroughput.getNumberOfDecreasesToday());
        attribute("read-capacity", provisionedThroughput.getReadCapacityUnits());
        attribute("write-capacity",
                provisionedThroughput.getWriteCapacityUnits());
        endElement();

    }

    protected void writeTableDescription(TableDescription tableDescription)
            throws XMLStreamException {
        startElement("table");
        attribute("name", tableDescription.getTableName());
        attribute("status", tableDescription.getTableStatus());
        attribute("create-date",
                Util.formatXSDateTime(tableDescription.getCreationDateTime()));
        attribute("item-count", tableDescription.getItemCount());
        attribute("size", tableDescription.getTableSizeBytes());
        attribute("item-count", tableDescription.getItemCount());

        writeAttributeDefinitions(tableDescription.getAttributeDefinitions());
        writeKeySchemaList(tableDescription.getKeySchema());
        writeLocalSecondaryIndexes(tableDescription.getLocalSecondaryIndexes());
        writeGlobalSecondaryIndexes(tableDescription
                .getGlobalSecondaryIndexes());
        writeProvisionedThroughput(tableDescription.getProvisionedThroughput());

    }

    protected void writeAttrNameValue(String elem, Map<String, AttributeValue> result) throws XMLStreamException,
    IOException {
        startElement(elem);
        for (Entry<String, AttributeValue> a : result.entrySet())
            this.writeAttribute(a.getKey(), a.getValue());
        endElement();
    }

    protected void writeItem(Map<String, AttributeValue> result) throws XMLStreamException, IOException {
        writeAttrNameValue("item", result);
    }

    protected IKeyAttrValueMap parseKeyOptions(Options opts)
            throws InvalidArgumentException, UnexpectedException,
            UnimplementedException, IOException {
        IKeyAttrValueMap keys = new KeyAttrValueMap();
        if (opts.hasOpt("key"))
            for (XValue k : opts.getOptValues("key"))
                keys.putAll(DDBTypes.parseKey(k));

        if (opts.hasOpt("key-name")) {
            List<XValue> keyValues = opts.getOptValues("key-value");
            int i = 0;
            for (XValue keyName : opts.getOptValues("key-name"))
                keys.putAll(DDBTypes.parseKey(keyName, keyValues.get(i++)));
        }
        return keys;
    }
    
    protected void writeItemCollectionMetrics(ItemCollectionMetrics itemCollectionMetrics) throws XMLStreamException,
    IOException {
        startElement("item-collection-metrics");
        writeAttrNameValue("item-collection-key", itemCollectionMetrics.getItemCollectionKey());
        for (Double r : itemCollectionMetrics.getSizeEstimateRangeGB()) {
            super.startElement("size");
            attribute("value", r);
            endElement();
        }
        endElement();
    }

    protected void writeConsumedCapacity(ConsumedCapacity consumedCapacity) throws XMLStreamException
    {
        if (consumedCapacity == null)
            return;
        startElement("consumed-capacity");
        attribute("total-units", consumedCapacity.getCapacityUnits());
        startElement("table");
        writeCapacity("table-name", consumedCapacity.getTableName(), consumedCapacity.getTable());
        endElement();
        startElement("local-secondary-indexes");
        for (Entry<String, Capacity> ce : consumedCapacity.getLocalSecondaryIndexes().entrySet())
            writeCapacity("index-name", ce.getKey(), ce.getValue());
        endElement();

        startElement("global-secondary-indexes");
        for (Entry<String, Capacity> ce : consumedCapacity.getGlobalSecondaryIndexes().entrySet())
            writeCapacity("index-name", ce.getKey(), ce.getValue());
        endElement();
        endElement();

    }

    private void writeCapacity(String attr, String attrValue, Capacity cap) throws XMLStreamException {
        attribute(attr, attrValue);
        attribute("capacity-units", cap.getCapacityUnits());
    }

    private void attribute(String localName, Double d) throws XMLStreamException {
        attribute(localName, d.toString());
    }


    protected void writeMetrics(ArrayList<RequestMetrics> metrics) throws XMLStreamException, IOException {
        startElement("request-metrics");
        for (RequestMetrics m : metrics) {
            writeMetric(m);
        }
        endElement();

    }

    public void writeMetric(RequestMetrics m) throws XMLStreamException, IOException {

        startElement("request-metric");
        attribute("count", m.count);
        attribute("scanCount", m.scanCount);
        if (m.capacity != null)
            writeConsumedCapacity(m.capacity);
        if (m.itemMetrics != null)
            writeItemCollectionMetrics(m.itemMetrics);

        endElement();
    }
    protected int handleException(AmazonClientException e) throws XMLStreamException, SaxonApiException, IOException, CoreException {
        if( e instanceof ConditionalCheckFailedException )
            return handleConditionException((ConditionalCheckFailedException) e);
        return super.handleException(e);
    }
    protected GetItemSpec parseGetItemSpec(Options opts) throws InvalidArgumentException, XPathException, UnexpectedException, UnimplementedException, IOException {
        GetItemSpec spec = new GetItemSpec().withPrimaryKey( getPrimaryKey(opts) ).
                withConsistentRead( opts.hasOpt("consistant") ).withNameMap( parseAttrNameExprs(opts));
        if( opts.hasRemainingArgs() )
            spec.withProjectionExpression( 
                    Util.stringJoin(Util.toStringList( opts.getRemainingArgs()),","));
        return spec;

    }

    private  static  PrimaryKey getPrimaryKey(Options opts) throws InvalidArgumentException, XPathException, UnexpectedException, UnimplementedException, IOException {
         INameObjectMap keys = DDBTypes.parseKeyValueObjectOptions(opts);
        PrimaryKey key = new PrimaryKey();
        for(    Entry<String, Object> e : keys.entrySet()  ) 
            key.addComponent( e.getKey() , e.getValue() );
        return key ;
    }

    protected IAttrObjectExpr parseAttrValueObjectExprs(Options opts) throws InvalidArgumentException {
        return DDBTypes.parseAttrValueObjectExprs(opts);
    }
    protected IAttrNameExpr parseAttrNameExprs(Options opts) throws InvalidArgumentException, UnexpectedException,
            UnimplementedException, IOException {
        IAttrNameExpr exprs =  DDBTypes.parseAttrNameExprs(opts);
        if( exprs == null || exprs.isEmpty() )
            return exprs ;
        return DDBTypes.addNamePrefix( exprs );
    }
    protected IAttrValueExpr parseAttrValueExprs(Options opts) throws InvalidArgumentException,
            UnexpectedException, UnimplementedException, IOException {
        IAttrValueExpr exprs =  DDBTypes.parseAttrValueExprs(opts);
        if( exprs == null || exprs.isEmpty() )
            return exprs ;
        return DDBTypes.addValuePrefix(exprs);
    }
    protected int handleConditionException(ConditionalCheckFailedException ce) throws XMLStreamException,
    SaxonApiException, IOException, CoreException {
        startResult( getStderr() );
        attribute("status", "condition-failed");
        attribute("retryable", ce.isRetryable());
        startElement("exception");
        attribute("name", ce.getClass().getSimpleName());
        characters(ce.getLocalizedMessage());
        endElement();
        endResult();
        return ce.isRetryable() ? 2 : -1;

    }

    @Override
    protected String getCommonOpts() { 
        return Options.joinOptions(AWSCommand.sCOMMON_OPTS, sDDB_COMMON_OPTS );
    }


    

}

//
//
// Copyright (C) 2008-2014 David A. Lee.
//
// The contents of this file are subject to the "Simplified BSD License" (the
// "License");
// you may not use this file except in compliance with the License. You may
// obtain a copy of the
// License at http://www.opensource.org/licenses/bsd-license.php
//
// Software distributed under the License is distributed on an "AS IS" basis,
// WITHOUT WARRANTY OF ANY KIND, either express or implied.
// See the License for the specific language governing rights and limitations
// under the License.
//
// The Original Code is: all this file.
//
// The Initial Developer of the Original Code is David A. Lee
//
// Portions created by (your name) are Copyright (C) (your legal entity). All
// Rights Reserved.
//
// Contributor(s): none.
//
