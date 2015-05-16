package org.xmlsh.aws.util;

import static org.xmlsh.aws.util.DDBTypes.AttrType.S;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.trans.XPathException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xmlsh.aws.util.DDBTypes.NameType;
import org.xmlsh.aws.util.DDBTypes.NameTypeValue;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Base64;
import org.xmlsh.util.Base64.InputStream;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.JsonUtils;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class DDBTypes {

    private static Logger mLogger = LogManager.getLogger(DDBTypes.class);

    public enum AttrType {

        S, N, B, SS, NS, BS, M, L, NULL, BOOL;

        public String toString() {
            return this.name();
        }

        public static AttrType parseType(String name) {
            mLogger.debug("parseType: " + name);

            return parseType(name, null);
        }

        public static AttrType parseType(String name, AttrType def) {
            if (name == null)
                return def;
            for (AttrType n : values())
                if (n.name().equals(name))
                    return n;
            return def;
        }

        public static AttrType parseType(XdmNode node, AttrType def) {
            if (node == null)
                return def;
            mLogger.debug("parseType: " + node + "def: " + def );

            switch (node.getNodeKind()) {
            case ATTRIBUTE:
                // if( node.getNodeName().getLocalName().equals("type"))
                return parseType(node.getStringValue(), def);
            case ELEMENT:
                String stype = node.getAttributeValue(new QName("type"));
                return parseType(stype, def);
            case TEXT:
            default:
                return parseType(node.getStringValue(), def);
            }
        }

        public static AttrType parseType(XdmNode node) {
            mLogger.debug("parseType: " + node );
            return parseType(node, S);
        }

        public AttributeValue newAtomicValue( String s) throws UnexpectedException, InvalidArgumentException {
            assert( s != null);
            if( s.isEmpty() )
                throw  new InvalidArgumentException("Atomic attribute values may not be empty: type: " + name() );
            switch (this) {
            case N:
                return new AttributeValue().withN(s);
            case S:
                return new AttributeValue().withS(s);
            case BOOL:
                return new AttributeValue().withBOOL(Util.parseBoolean(s));
            case NULL:
                return new AttributeValue().withNULL(Util.parseBoolean(s));
            default:
                throw new UnexpectedException("Unexpected type for newAtomicValue: " + this.name());
            }
        }

        public AttributeValue validate( AttributeValue value ) throws InvalidArgumentException, UnexpectedException{

            switch( this ){
            case B: 
                notNull( value.getB() ); break ;
            case BOOL:
                notNull( value.getBOOL() ); break ;
            case BS:
                notEmpty( value.getBS() ); break ;
            case L:
                notNull( value.getL()); break ;
            case M:
                notNull( value.getM() ); break ;
            case N:
                notEmpty( value.getN() ); break ;
            case NS:
                notEmpty( value.getNS() ); break ;
            case NULL:
                notNull( value.getNULL() ); break ;
            case S:
                notEmpty( value.getS() ); break ;
            case SS:
                notEmpty( value.getSS() ) ;
                break ;
            default:
                throw new UnexpectedException("Unexpected type for newAtomicValue: " + this.name());
            }

            return value ;
        }
        private void notEmpty(String s) throws InvalidArgumentException {
            notNull(s);
            if( s.isEmpty() )
                throw new InvalidArgumentException("Attribute type is not allowed to be empty. Type: " + name() );
        }
        private <T> void notEmpty(Collection<T> c) throws InvalidArgumentException {
            notNull(c);
            if( c.isEmpty() )
                throw new InvalidArgumentException("Attribute type is not allowed to be empty. Type: " + name() );
        }
        private <T> void notNull(T o) throws InvalidArgumentException {
            if( o == null )
                throw new InvalidArgumentException("Attribute type is not allowed to be null. Type: " + name() );

        }
    }

    public static AttributeValue parseAttributeValue(XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("XdmNode:" + node.toString());
        AttrType type = AttrType.parseType(node);
        return type.validate( parseAttributeValue(type, node));
    }

    public static AttributeValue parseAttributeValue(AttrType type, XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("AttrType: " + type.toString() + " XdmNode: " + node.toString());

        switch (type) {
        case N:
        case S:
        case BOOL:
        case NULL:
            return type.newAtomicValue( DDBTypes.parseStringValue( node ) );
        case NS:
            return type.validate( new AttributeValue().withNS( DDBTypes.parseNS(node) ));
        case SS:
            return  type.validate( new AttributeValue().withSS(DDBTypes.parseSS(node)));
        case B:
            return  type.validate( new AttributeValue().withB(DDBTypes.parseBinary(node)));
        case BS:
            return  type.validate( new AttributeValue().withBS(DDBTypes.parseBS(node)));
        case L:
            return  type.validate( new AttributeValue().withL(DDBTypes.parseL(node)));
        case M:
            return  type.validate(new AttributeValue().withM(DDBTypes.parseM(node)));
        default:
            throw new UnexpectedException("Unknown type: " + type.toString());
        }

    }

    private static Map<String, AttributeValue> parseM(XdmNode node) throws UnimplementedException {
        throw new UnimplementedException("parseM(XdmMode)");
    }

    private static Collection<AttributeValue> parseL(XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
        assert( node != null );
        XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
        List<AttributeValue> list = new ArrayList<AttributeValue>();
        if (iter != null) {
            while (iter.hasNext())
                list.add(parseAttributeValue( (XdmNode) iter.next()));
        }
        return list ;
    }

    private static Collection<ByteBuffer> parseBS(XdmNode node) throws IOException {
        return parseBS( new XValue( node )) ;
    }

    private static ByteBuffer parseBinary(XdmNode node) throws IOException {
        return DDBTypes.parseBinary( node.getStringValue() );
    }

    private static Collection<String>  parseSS(XdmNode node) {
        assert( node != null );

        Set<String> set = new HashSet<String>();
        XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
        if (iter != null) {
            while (iter.hasNext())
                set.add( iter.next().getStringValue() );
        }
        return set;   
    }

    private static Collection<String>   parseNS(XdmNode node) {
        return parseSS(node);
    }

    private static String parseStringValue(XdmNode node) {
        String value = null;
        switch( node.getNodeKind() ){
        case ATTRIBUTE:
        case ELEMENT:
            value = node.getAttributeValue(new QName("value"));
        default:
        case TEXT:
        }
        if( value == null )
            value = node.getStringValue();
        return value ;

    }




    /*
     * Attribute Name/Type Type is optional and defaults to "S" and stored in
     * Left Name is stored in right
     */
    public static class NameType {
        String name;
        AttrType type;

        private void _init( String name , AttrType type ){
            mLogger.debug("_init: name: " + name + "type: " + type);
            this.name = name ;
            this.type =  type ;
        }
        private void _init(String def) {
            /*
             * StringPair pair = new StringPair( def , ':'); name =
             * pair.getRight(); type = pair.hasLeft() ? pair.getLeft() : "S";
             */
            _init( def.trim(),S);
        }

        NameType(String def) {
            _init(def);
        }

        NameType(String name, AttrType type) {
            _init(name,type);
        }

        NameType(String name, String type) {
            _init(name,AttrType.parseType(type));
        }

        NameType(XValue xv) throws InvalidArgumentException {
            _init(xv);
        }

        private void _init(XValue xv) throws InvalidArgumentException {
            mLogger.debug("_init: " + xv );

            if (xv.isXdmNode()) {
                _init(xv.asXdmNode());
            }
            else if (xv.isString())
                _init(xv.toString());
            else
                throw new InvalidArgumentException(
                        "Unexpected argument type for attribute: " + xv.getTypeName());
        }

        NameType(XdmNode node) {
            _init(node);
        }

        private void _init(XdmNode node) {
            mLogger.debug("_init: " + node );
            AttrType _type = AttrType.parseType(node);
            String _name;
            switch (node.getNodeKind()) {
            case ELEMENT:
                _name = node.getAttributeValue(new QName("name"));
                if (_name == null)
                    _name = node.getNodeName().getLocalName();
                break;
            case TEXT:
            case ATTRIBUTE:
            default:
                _name = node.getStringValue();
            }
            _init(_name,_type);
        }

        String getName() {
            return name;
        }

        AttrType getType() {
            return type;
        }

        String getTypeName() {
            return type.toString();
        }

        public String toString() {
            return "{" + type.toString() + "}" + name.toString();
        }

    }

    public static class NameTypeValue extends NameType {
        AttributeValue value;

        NameTypeValue(String name, AttrType type, AttributeValue value) {
            super(name, type);
            mLogger.debug("Value: " + value);
            this.value = value;
        }

        public NameTypeValue(XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
            super(node);
            this.value = parseAttributeValue(getType(), node);
            mLogger.debug("Value: " + value);

        }
    }
    /*
    public static AttributeValue parseAttributeValue(XdmItem item) throws UnexpectedException {
        mLogger.debug("XdmItem: " + item.toString());

        if (item instanceof XdmNode)
            return parseAttributeValue((XdmNode) item);
        return parseAttributeValue(item.getStringValue());
    }
     */
    private static AttributeValue parseAttributeValue(String s) throws UnexpectedException, InvalidArgumentException {
        return parseAttributeValue(S, s);
    }

    public static AttributeValue parseAttributeValue(XValue xv) throws UnexpectedException, InvalidArgumentException,
    UnimplementedException, IOException {
        mLogger.debug("XValue: " + xv.toString());

        return parseAttributeValue(parseAttribteType(xv), xv);
    }

    public static AttrType parseAttribteType(XValue xv) throws InvalidArgumentException {
        mLogger.debug("XValue: " + xv.toString());
        if (xv.isXdmNode())
            return AttrType.parseType(xv.asXdmNode());
        if (xv.isString())
            return S;
        if (xv.isNull())
            return AttrType.NULL;
        if (xv.isAtomic()) {
            return S;
        }
        throw new InvalidArgumentException("Unexpected type for attribute: " + xv.toString() + " type: "
                + xv.getTypeName());

    }

    protected static AttributeValue parseAttributeValue(String stype, XValue xv) throws UnexpectedException,
    UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("Type: " + stype + " XValue: " + xv.toString());
        return DDBTypes.parseAttributeValue(AttrType.parseType(stype, AttrType.S), xv);
    }

    public static AttributeValue parseAttributeValue(AttrType type, XValue xv) throws UnexpectedException,
    UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("Type: " + type + " XValue: " + xv.toString());
        if( xv.isXdmNode())
            return parseAttributeValue( type , xv.asXdmNode() );

        switch (type) {
        case N:
        case S:
        case BOOL:
        case NULL:
            return type.newAtomicValue(xv.toString());
        case NS:
            return new AttributeValue().withNS(DDBTypes.parseNS(xv));
        case SS:
            return new AttributeValue().withSS(DDBTypes.parseSS(xv));
        case B:
            return new AttributeValue().withB(DDBTypes.parseBinary(xv));
        case BS:
            return new AttributeValue().withBS(DDBTypes.parseBS(xv));
        case L:
            return new AttributeValue().withL(DDBTypes.parseL(xv));
        case M:
            return new AttributeValue().withM(DDBTypes.parseM(xv));
        default:
            throw new UnexpectedException("Unknown type: " + type.toString());
        }

    }

    /*
     * Parse a set of attributes [type:]Name Value ... -> any value of type
     * 
     * <attribute name="name" type="type"> value <attribute>
     */
    public static Map<String, AttributeValue> parseAttributeValues(List<XValue> args)
            throws IOException, UnexpectedException, UnimplementedException, InvalidArgumentException {
        mLogger.debug(" parseAttributeValues(List<XValue>) ");

        Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
        for (XValue arg : args) {
            DDBTypes.NameTypeValue ntv = DDBTypes.parseNameTypeValue(arg);
            attrs.put(ntv.getName(), ntv.value);
        }
        return attrs;
    }

    public static Map<String, AttributeValue> parseAttributeNameValue(XValue name, XValue xv)
            throws UnimplementedException, UnexpectedException, IOException, InvalidArgumentException {
        mLogger.debug("parseAttributeValues(Name: " + name + " XValue: " + xv);

        Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
        AttributeValue av = parseAttributeValue(parseAttribteType(xv), xv);
        attrs.put(name.toString(), av);
        return attrs;
    }

    public static DDBTypes.NameTypeValue parseNameTypeValue(String s) throws InvalidArgumentException,
    UnexpectedException {
        mLogger.debug("parseNameTypeValue: " + s);
        // Name=value
        Map<String,String> nv = DDBTypes.parseNameStringValue(s);
        assert( nv.size() == 1);
        String name = nv.keySet().iterator().next();
        DDBTypes.NameType ant = new DDBTypes.NameType(  name );
        return new DDBTypes.NameTypeValue(name, ant.getType(), DDBTypes.parseAttributeValue(ant.type, nv.get(name)));
    }

    static DDBTypes.NameTypeValue parseNameTypeValue(XValue xv) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
        mLogger.debug("parseNameTypeValue: " + xv);
        NameType ant = new NameType(xv);
        AttributeValue av = parseAttributeValue(ant.getType(), xv);
        return new NameTypeValue( ant.getName() , ant.getType() , av );
    }

    static Map<String, AttributeValue> parseM(XValue xv)
            throws UnimplementedException {
        throw new UnimplementedException("Map type unimplemented");

    }

    static DDBTypes.NameTypeValue parseNameTypeValue(XdmNode node) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
        mLogger.debug("parseNameTypeValue: " + node.toString());

        DDBTypes.NameType ant = new DDBTypes.NameType(node);
        AttributeValue av = parseAttributeValue(ant.getType(), node);
        return new DDBTypes.NameTypeValue(ant.name, ant.getType(), av);

    }
    static Collection<AttributeValue> parseL(XValue xv)
            throws UnimplementedException, UnexpectedException, InvalidArgumentException, IOException {
        mLogger.debug("parseL: " + xv);

        if( xv.isNull() || xv.isEmpty() )
            return Collections.EMPTY_LIST;

        // Lists - xv is the parent - parse children if it ( using
        // parseL(List<XValue> if wanting to treat xv as the list contens
        if (xv.isXdmNode()) {
            return parseL( xv.asXdmNode() );
        }
        // Othwerwise a list of one itself
        return Collections.singletonList(AttrType.S.newAtomicValue(xv.toString()));
    }

    protected static Collection<ByteBuffer> parseBS(XValue xv) throws IOException {

        ArrayList<ByteBuffer> ret = new ArrayList<ByteBuffer>();
        for (String s : xv.asStringList())
            ret.add(DDBTypes.parseBinary(s));
        return ret;

    }

    static ByteBuffer parseBinary(XValue xv) throws IOException {
        return DDBTypes.parseBinary(xv.toString());

    }

    protected static ByteBuffer parseBinary(String s) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Base64.InputStream b64 = new Base64.InputStream(
                new ByteArrayInputStream(s.getBytes("UTF8")), Base64.DECODE);
        Util.copyStream(b64, bos);
        b64.close();
        return ByteBuffer.wrap(bos.toByteArray());

    }

    public static Collection<String> parseNS(XValue xv) {
        return DDBTypes.parseSS(xv); // numbers
    }

    protected static Collection<String> parseSS(XValue xv) {
        mLogger.debug("parseSS: " + xv);

        return xv.asStringList();
    }

    public static Map<String, AttributeValue> parseKey(XValue key) throws UnexpectedException,
    UnimplementedException, IOException, InvalidArgumentException {
        Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
        DDBTypes.NameTypeValue ntv = null;
        mLogger.debug("parseKey: " + key);

        if (key.isXdmNode()) {
            ntv = parseNameTypeValue(key.asXdmNode());
        }
        else if (key.isAtomic() || key.isString()) {
            ntv = parseNameTypeValue(key.toString());
        } else
            throw new InvalidArgumentException("Unexpected type for attribute: " + key.getTypeName() + " "
                    + key.toString());
        attrs.put(ntv.getName(), ntv.value);
        return attrs;

    }

    static AttributeValue parseAttributeValue(AttrType type, String value) throws UnexpectedException, InvalidArgumentException {
        return type.newAtomicValue(value);
    }

    public static Map<String, AttributeValue> parseKey(XValue name, XValue value)
            throws UnexpectedException, UnimplementedException, IOException,
            InvalidArgumentException {
        mLogger.debug("parseKey: name: " + name + "value: " + value );
        return parseAttributeNameValue(name, value);
    }

    protected static Map<String,String> parseNameValue(XValue nv) throws InvalidArgumentException {
        mLogger.debug("parseNameValue: " + nv );

        // placeholder=literal
        if (nv.isAtomic()) {
            return parseNameStringValue(nv.toString());
        } else
            throw new InvalidArgumentException(
                    "Unexpected attribute name expression. expected 'placeholder=literal'  : " + nv.toString());
    }

    protected static Map<String,String>  parseNameStringValue(String s) throws InvalidArgumentException {
        mLogger.debug("parseNameValue: " + s );
        if( s.startsWith("{")){
            Map<String, Object> map = Item.fromJSON(s).asMap();
            Map<String,String> smap = new HashMap<String,String>(map.size());
            for( String key : map.keySet() )
                smap.put(key, map.get(key).toString() );
            return smap;
        }
        else if( s.contains("=") ){
            StringPair keypair = new StringPair(s,'=');
            if( keypair.hasLeft() )
                return Collections.singletonMap( keypair.getLeft() , keypair.getRight() );
        }
        throw new InvalidArgumentException( "Unexpected attribute name expression. expected 'placeholder=literal'  : " + s.toString());
    }

    public static Map<String, String> parseAttrNameExprs(List<XValue> nameExprs)
            throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        mLogger.debug("parseAttrNameExprs(List<XValue>): " );

        Map<String, String> map = new HashMap<String, String>();
        for (XValue v : nameExprs) {
            map.putAll( parseNameValues(v));
        }
        return map;
    }

    private static Map<String,String> parseNameValues(XValue v) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        if( v.isAtomic() )
            return parseNameValue( v );

        if( v.isJson() ){ 
            return parseNameStringValue( v.asJson().toJson() ) ;
        }
        
        if( v.isXExpr() ) {
            Map<String,String> map = new HashMap<String,String>();
            DDBTypes.NameTypeValue ntv = DDBTypes.parseNameTypeValue(v);
            map.put( ntv.getName() ,ntv.value.toString() );
            return map ;
        }
        throw new InvalidArgumentException("expected string , xml or json: " + v.getTypeName() );

    }

    protected static  Map<String,Object>   parseAttrValueObjectMap(XValue v) throws InvalidArgumentException {
        if( v.isString()){
            return DDBTypes.parseAttrValueObjectMap( v.toString().trim() );
        } else 
            if( v.isJson() )
                return DDBTypes.parseAttrValueObjectMap( v.asJson().toJson() );
        throw new InvalidArgumentException("expected string or json: " + v.getTypeName() );
    }

    // Name/Values for attribute expressions in Docuemnt mode
    protected static Map<String, AttributeValue> parseAttrValueExprs(List<XValue> optValues)
            throws UnexpectedException, UnimplementedException,
            InvalidArgumentException, IOException {
        return parseAttributeValues(optValues);
    }

    // Name/Values for attribute expressions in Docuemnt mode
    protected static  Map<String, Object> parseAttrValueObjectMap(List<XValue> values) throws InvalidArgumentException {
        Map<String,Object> map = new HashMap<String,Object>();
        for( XValue v : values )
            map.putAll( parseAttrValueObjectMap( v ) );
        return map;
    }

    protected static Map<String,Object>  parseAttrValueObjectMap( String s) throws InvalidArgumentException{
        if( s.startsWith("{"))
            return Item.fromJSON(s).asMap();
        else if( s.contains("=") ){
            StringPair keypair = new StringPair(s,'=');
            if( keypair.hasLeft() )
                return Collections.singletonMap( keypair.getLeft() , AWSDDBCommand.parseToJavaValue(keypair.getRight()));
        }
        throw new InvalidArgumentException("name value expect  name=value: " + s );
    }

    protected  static  Item parseItem(List<XValue> args) throws InvalidArgumentException {
        Map<String, Object> values = parseAttrValueObjectMap( args );
        return Item.fromMap(values);
    }

    public static Map<String, String> parseAttrNameExprs(Options opts) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        if( opts.hasOpt("attr-name-expr"))
            return parseAttrNameExprs(
                    Util.expandSequences(
                            opts.getOptValues("attr-name-expr")));
        return null;
    }

    public  static  Map<String, Object> parseKeyValueObjectOptions(Options opts)
            throws InvalidArgumentException, UnexpectedException,
            UnimplementedException, IOException, XPathException {
        Map<String, Object> keys = new HashMap<String, Object>();
        if (opts.hasOpt("key"))
            keys.putAll( parseAttrValueObjectMap( opts.getOptValue("key") ));

        if (opts.hasOpt("key-name")) {
            List<XValue> keyValues = opts.getOptValues("key-value");
            int i = 0;
            for (XValue keyName : opts.getOptValues("key-name"))
                keys.put( keyName.toString() , parseToJavaValue( keyValues.get(i++) ));
        }
        return keys;
    }

    public  static  Map<String, Object> parseAttrValueObjectExprs(Options opts) throws InvalidArgumentException {
        if( opts.hasOpt("attr-value-expr"))
            return parseAttrValueObjectMap(Util.expandSequences(opts.getOptValues("attr-value-expr")));
        return null;
    }

    public  static  Map<String, AttributeValue> parseAttrValueExprs(Options opts) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        if( opts.hasOpt("attr-value-expr"))
            return parseAttrValueExprs(  Util.expandSequences(opts.getOptValues("attr-value-expr")));
        return null;
    }

    public  static  Map<String, Object> parseItemValueObject(Options opts) throws InvalidArgumentException, UnexpectedException,
    UnimplementedException, IOException, XPathException {
        Map<String, Object> item = parseKeyValueObjectOptions(opts);
        if( opts.hasRemainingArgs() )
            item.putAll(parseAttrValueObjectMap( opts.getRemainingArgs() ));
        return item;
    }

    public static Object     parseToJavaValue(XValue xv) throws XPathException {
        if( xv == null || xv.isNull() )
            return null ;
        if( xv.isEmptySequence() )
            return null ;
        if( xv.isJson() )
            return JsonUtils.toJsonType(xv);
        if( xv.isString())
            return AWSDDBCommand.parseToJavaValue( xv.toString());
        Object obj =  xv.getJavaNative();
        if( obj != null && JavaUtils.isStringClass(obj.getClass()))
            return AWSDDBCommand.parseToJavaValue( obj.toString() );
        return obj;

    }

}
