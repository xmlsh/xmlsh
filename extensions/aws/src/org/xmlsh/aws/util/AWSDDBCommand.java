/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.aws.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Base64;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

public abstract class AWSDDBCommand extends AWSCommand {

    protected AmazonDynamoDB mAmazon;

    public AWSDDBCommand() {
        super();
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

    protected void getDDBClient(Options opts) throws UnexpectedException,
            InvalidArgumentException {

        mAmazon = new AmazonDynamoDBClient(
                new AWSCommandCredentialsProviderChain(mShell, opts)

        );

        setEndpoint(opts);
        setRegion(opts);
    }

    protected AttributeDefinition parseKeyAttribute(XValue xv)
            throws InvalidArgumentException, UnexpectedException {
        if (xv.isString()) {
            StringPair sp = new StringPair(xv.toString(), '=');
            return new AttributeDefinition().withAttributeName(sp.getRight())
                    .withAttributeType(sp.getLeft());
        }
        if (xv.isXdmNode()) {
            return new AttributeDefinition().withAttributeName(
                    xpath(xv, "string(./(@name|name)").toString())
                    .withAttributeType(
                            xpath(xv, "string(./(@type|type)").toString());

        }
        throw new InvalidArgumentException(
                "Unexpected argument type for attribute: " + xv.getTypeName());
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

    protected KeySchemaElement parseKeySchemaElement(XValue xv) {

        StringPair sp = new StringPair(xv.toString(), ':');

        KeySchemaElement keyElement = new KeySchemaElement().withAttributeName(
                sp.getRight()).withKeyType(
                KeyType.valueOf(sp.getLeft().toUpperCase()));
        return keyElement;
    }

    private Collection<KeySchemaElement> parseKeySchemaList(XValue xv)
            throws UnexpectedException {
        Collection<KeySchemaElement> list = new ArrayList<KeySchemaElement>();
        if (xv.isXdmNode()) {
            // value->sequence->item
            for (XdmItem item : xpath(xv, "key-schema/key-element-name")
                    .asXdmValue()) {
                if (item instanceof XdmNode)
                    list.add(new KeySchemaElement(((XdmNode) item)
                            .getAttributeValue(new QName("name")),
                            ((XdmNode) item)
                                    .getAttributeValue(new QName("type"))));
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmlsh.aws.util.AWSCommand#setRegion(java.lang.String)
     */
    @Override
    public void setRegion(String region) {
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
            attribute("type", "S");
            characters(avalue.getS());
        }
        else
        if (avalue.getN() != null) {
            attribute("type", "N");
            characters(avalue.getN());
        }
        else

        if (avalue.getB() != null) {
            attribute("type", "B");
            binary(avalue.getB().array());
        }
        else
        if (avalue.getSS() != null) {
            attribute("type", "SS");
            for (String s : avalue.getSS()) {
                startElement("value");
                characters(s);
                endElement();
            }

        }
        else
        if (avalue.getNS() != null) {
            attribute("type", "NS");
            for (String s : avalue.getNS()) {
                startElement("value");
                characters(s);
                endElement();
            }
        }
        else
        if (avalue.getBS() != null) {
            attribute("type", "BS");
            for (ByteBuffer s : avalue.getBS()) {
                startElement("value");
                binary(s.array());
                endElement();
            }
        }
        else
        if (avalue.getL() != null) {
            attribute("type", "L");
            for (AttributeValue av : avalue.getL()) {
                startElement("value");
                writeAttributeValue(av);
                endElement();
            }
        } else if (avalue.getM() != null) {
            attribute("type", "M");
            for (Entry<String, AttributeValue> e : avalue.getM().entrySet()) {
                writeAttribute(e.getKey(), e.getValue());

            }
        } else if (avalue.isBOOL()) {
            attribute("type", "BOOL");
            characters(avalue.getBOOL() ? "true" : "false");
        } else if (avalue.isNULL()) {
            attribute("type", "NULL");
            characters(avalue.getNULL() ? "true" : "false");

        }

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

    protected Map<String, AttributeValue> parseAttributeValues(List<XValue> args)
            throws IOException, UnexpectedException, UnimplementedException,
            InvalidArgumentException {
        Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
        while (!args.isEmpty()) {
            if (args.size() < 2)
                throw new InvalidArgumentException(
                        "Expecting name attribute [name value]");

            StringPair sp = new StringPair(args.remove(0).toString(), ':');
            String type = sp.hasLeft() ? sp.getLeft() : "S";
            AttributeValue av = new AttributeValue();

            XValue xv = args.remove(0);

            if (type.equals( "N"))
                av.setN(xv.toString());
            else if (type.equals( "NS"))
                av.setNS(parseNS(xv));
            else if (type.equals( "S"))
                av.setS(xv.toString());
            else if (type.equals( "SS"))
                av.setSS(parseSS(xv));
            else if (type.equals( "B"))
                av.setB(parseBinary(xv));
            else if (type.equals( "BS"))
                av.setBS(parseBS(xv));
            else if (type.equals( "BOOL"))
                av.setBOOL(Util.parseBoolean(xv.toString()));
            else if (type.equals( "NULL"))
                av.setNULL(Util.parseBoolean(xv.toString()));
            else if (type.equals( "L"))
                av.setL(parseL(xv));
            else if (type.equals( "M"))
                av.setM(parseM(xv));

            else
                throw new UnexpectedException("Unknown type: " + type);


            attrs.put(sp.getRight(), av);

        }
        return attrs;
    }

    private Map<String, AttributeValue> parseM(XValue xv)
            throws UnimplementedException {
        throw new UnimplementedException("Map type unimplemented");

    }

    private Collection<AttributeValue> parseL(XValue xv)
            throws UnimplementedException {

        throw new UnimplementedException("List<Attribute> type unimplemented");

    }

    protected Collection<ByteBuffer> parseBS(XValue xv) throws IOException {

        ArrayList<ByteBuffer> ret = new ArrayList<ByteBuffer>();
        for (String s : xv.asStringList())
            ret.add(parseBinary(s));

        return ret;

    }

    private ByteBuffer parseBinary(XValue xv) throws IOException {
        return parseBinary(xv.toString());

    }

    protected ByteBuffer parseBinary(String s) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Base64.InputStream b64 = new Base64.InputStream(
                new ByteArrayInputStream(s.getBytes("UTF8")), Base64.DECODE);
        Util.copyStream(b64, bos);
        b64.close();
        return ByteBuffer.wrap(bos.toByteArray());

    }

    protected Collection<String> parseSS(XValue xv) {
        return xv.asStringList();
    }

    protected Collection<String> parseNS(XValue xv) {
        return parseSS(xv); // numbers
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
