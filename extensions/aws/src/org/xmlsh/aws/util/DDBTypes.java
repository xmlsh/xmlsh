package org.xmlsh.aws.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
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
import org.xmlsh.aws.util.DDBTypes.INameAttrValueMap;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.Options;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Base64;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.JsonUtils;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class DDBTypes implements INameAttrExpr {

    private static Logger mLogger = LogManager.getLogger(DDBTypes.class);

    public interface INameMap<T> extends Map<String,T> {
    }
    public static class NameMap<T> extends AbstractMap<String,T> implements INameMap<T> {
        HashMap<String,T>  map = new HashMap<String,T>();
        NameMap() {
        }
        NameMap(Map<String,T> map) {
            this.map.putAll( map );
        }
            
        @Override
        public Set<java.util.Map.Entry<String, T>> entrySet() {
            return map.entrySet();
        }
        @Override
        public T put(String key, T value) {
            // TODO Auto-generated method stub
            return map.put(key, value);
        }
    
    }

    // Interfaces to identify pre parsed values
    public interface  INameStringMap extends INameMap<String>  {
    }
    public static class NameStringMap extends NameMap<String> implements INameStringMap {
        public NameStringMap() {}

        public NameStringMap(Map<String, String> map) {
            super(map);
        }}
    
    public interface INameObjectMap extends INameMap<Object>  { }
    public static class NameObjectMap extends NameMap<Object> implements INameObjectMap {
        public NameObjectMap() {}
        
        public NameObjectMap(Map<String,Object> map) {
            super(map);
        }}

    public interface IAttrNameExpr extends INameStringMap {  }
    public static class AttrNameExpr extends NameMap<String> implements  IAttrNameExpr {
        public AttrNameExpr() {
        }
        public AttrNameExpr(Map<String,String> map) {
            super(map);
        }
        public AttrNameExpr(String name , String value ) {
            super( Collections.singletonMap(name, value));
        }
     }

    public interface INameAttrValueMap extends INameMap<AttributeValue> { }
    public static class NameAttrValueMap extends NameMap<AttributeValue> implements INameAttrValueMap {
        public NameAttrValueMap(Map<String, AttributeValue> map) {
            super(map);
        }
        public NameAttrValueMap() {
        }}

    public interface IAttrValueExpr extends INameAttrValueMap {}
    public static class AttrValueExpr extends NameMap<AttributeValue> implements IAttrValueExpr {

        public AttrValueExpr(Map<String,AttributeValue> map) {
            super(map);
        }

        public AttrValueExpr() {
        }

        public AttrValueExpr(String string, AttributeValue value) {
            super( Collections.singletonMap(string, value));
        }}

    public interface IAttrObjectExpr extends INameObjectMap {    }
    public static class AttrObjectExpr extends NameMap<Object> implements IAttrObjectExpr {
        public AttrObjectExpr(Map<String,Object> map) {
            super(map);
        }}

    public interface IKeyAttrValueMap  extends IAttrValueExpr {    }
    public static class KeyAttrValueMap  extends  NameMap<AttributeValue> implements IKeyAttrValueMap {
        public KeyAttrValueMap(Map<String,AttributeValue> map) {
            super(map);
        }
        KeyAttrValueMap() {} 
    }
    
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

        public static AttrType fromValue( AttributeValue av) {
            if (av.getS() != null) return S;
            if (av.getN() != null) return N ;
            if (av.getB() != null) return B ;
            if (av.getSS() != null) return SS;
            if (av.getNS() != null) return NS;
            if (av.getBS() != null) return BS;
            if (av.getM() != null) return M;
            if (av.getL() != null) return L;
            if (av.isNULL() != null)return NULL;
            if (av.isBOOL() != null) return BOOL;
            return S;
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
   
    
    
    public static AttributeValue parseAttrValue(XValue xv) throws UnexpectedException, UnimplementedException,
    IOException, InvalidArgumentException {
        if( xv.isInstanceOf(AttributeValue.class))
            return xv.asInstanceOf( AttributeValue.class);
        if( xv.isXdmNode() )
            return parseAttrValue( xv.asXdmNode() );
        if( xv.isEmpty() )
            return new AttributeValue().withNULL( true );
        if( xv.isAtomic()) {
            String value ;
            if( xv.isJson() )
                value = xv.asJson().toJson() ;
            else
                value = xv.toString() ;
     
            return parseAttrValue(value);
        }
        return parseAttrValue( parseAttrTypeFromValue(xv) , xv );
    }
    
    public static AttributeValue parseAttrValue(XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("XdmNode:" + node.toString());
        AttrType type = parseAttrType(node);
        return type.validate( parseAttrValue(type, node));
    }

    protected static AttrType parseAttrType(XdmNode node) {
        return AttrType.parseType(node);
    }

    public static AttributeValue parseAttrValue(AttrType type, XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
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

    private static INameAttrValueMap parseM(XdmNode node) throws UnimplementedException, UnexpectedException, InvalidArgumentException, IOException {
        assert( node != null );
        XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
        INameAttrValueMap list = new NameAttrValueMap();
        if (iter != null) {
            while (iter.hasNext()){
                XdmItem item = iter.next();
                list.putAll( parseAttrNameValue( new XValue(item) ) );
            }
        }
        return list ;
        
        
        
    }

    private static Collection<AttributeValue> parseL(XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
        assert( node != null );
        XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
        List<AttributeValue> list = new ArrayList<AttributeValue>();
        if (iter != null) {
            while (iter.hasNext())
                list.add(parseAttrValue( (XdmNode) iter.next()));
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
            _init( def.trim(), AttrType.S);
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

            _init( DDBTypes.parseName(xv) ,  parseAttrTypeFromValue(xv));
     
        }

        NameType(XdmNode node) {
            _init(node);
        }

        public NameType(NameType nt) {
            this.name = nt.name;
            this.type = nt.type;
        }
        private void _init(XdmNode node) {
            mLogger.debug("_init: " + node );
            AttrType _type = AttrType.parseType(node);
            String _name = parseName(node);
            _init(_name,_type);
        }
        public static String parseName(XdmNode node) {
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
            return _name;
        }
        public static String parseName(String string) {
            return string.trim();
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

    public static class NameTypeValue extends NameType  {
        AttributeValue value;
        
        NameTypeValue(NameType nt , AttributeValue value) {
            super(nt);
            this.value = value ;
        }
        NameTypeValue(String name, AttrType type, AttributeValue value) {
            super(name, type);
            mLogger.debug("Value: " + value);
            this.value = value;
        }

        public NameTypeValue(XdmNode node) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException {
            super(node);
            this.value = parseAttrValue(getType(), node);
            mLogger.debug("Value: " + value);

        }
        public INameAttrValueMap asNameValueMap(){
            return new NameAttrValueMap( Collections.singletonMap(getName(), value));
        }
    }
    /*
    public static AttributeValue parseAttributeValue(XdmItem item) throws UnexpectedException {
        mLogger.debug("XdmItem: " + item.toString());

        if (item instanceof XdmNode)
            return parseAttributeValue((XdmNode) item);
        return parseAttributeValue(item.getStringValue());
    }
   
    private static AttributeValue parseAttributeValue(String s) throws UnexpectedException, InvalidArgumentException {
        return parseAttributeValue(S, s);
    }
  */

    public static AttrType parseAttrType(XValue xv) throws InvalidArgumentException {
        if( xv.isInstanceOf(AttrType.class))
            return xv.asInstanceOf(AttrType.class);
        mLogger.debug("XValue: " + xv.toString());
        if (xv.isXdmNode())
            return AttrType.parseType(xv.asXdmNode());
        if (xv.isNull() || xv.isEmpty() )
            return AttrType.NULL;
        throw new InvalidArgumentException("Unexpected type for attribute: " + xv.toString() + " type: "
                + xv.getTypeName());

    }

    public static AttrType parseAttrTypeFromValue(XValue xv) throws InvalidArgumentException {
        if( xv.isInstanceOf(AttrType.class))
            return xv.asInstanceOf(AttrType.class);
        mLogger.debug("XValue: " + xv.toString());
        if (xv.isXdmNode())
            return AttrType.parseType(xv.asXdmNode());
        if (xv.isString()) 
            return guessAttrType( xv.toString() );
        if (xv.isNull() || xv.isEmpty() )
            return AttrType.NULL;
        if( xv.isJson())
            return guessAttrType( xv.asJson().toJson());
        if (xv.isAtomic()) {
            return guessAttrType( xv.toString());
        }
        if( xv.asXdmValue() != null )
        return AttrType.L;
        throw new InvalidArgumentException("Unexpected type for attribute: " + xv.toString() + " type: "
                + xv.getTypeName());

    }
    protected static AttributeValue parseAttrValue(String stype, XValue xv) throws UnexpectedException,
    UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("Type: " + stype + " XValue: " + xv.toString());
        return DDBTypes.parseAttrValue(AttrType.parseType(stype, AttrType.S), xv);
    }
    
    public static AttributeValue parseAttrValue(String s)  throws UnexpectedException, UnimplementedException, InvalidArgumentException, IOException 
    {
        return parseAttrValue( guessAttrType(s) , s);
    }
    
    // Guess a type from a string value
    private static AttrType guessAttrType(String s) {
        if( s.startsWith("{") )
            return AttrType.M;
        if( s.startsWith("["))
            return AttrType.L;
        if( JavaUtils.isNumber(s))
            return AttrType.N;
        return AttrType.S;
        
        
    }

    public static AttributeValue parseAttrValue(XValue type, XValue xv) throws UnexpectedException, UnimplementedException, InvalidArgumentException, IOException 
    {
        return parseAttrValue( parseAttrType(xv) , xv );
    }
    public static AttributeValue parseAttrValue(AttrType type, XValue xv) throws UnexpectedException,
    UnimplementedException, IOException, InvalidArgumentException {
        mLogger.debug("Type: " + type + " XValue: " + xv.toString());
        if( xv.isXdmNode())
            return parseAttrValue( type , xv.asXdmNode() );

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
    public static INameAttrValueMap parseAttrNameValue(List<XValue> args)
            throws IOException, UnexpectedException, UnimplementedException, InvalidArgumentException {
        mLogger.debug(" parseAttributeValues(List<XValue>) ");

        INameAttrValueMap attrs = new NameAttrValueMap();
        for (XValue arg : args) {
               attrs.putAll(parseAttrNameValue(arg));
        }
        return attrs;
    }
    public static INameAttrValueMap parseAttrNameValue(XValue arg) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {

        if( arg.isInstanceOf(INameAttrValueMap.class))
            return arg.asInstanceOf(INameAttrValueMap.class);
        if( arg.isXdmNode())
            return parseNameTypeValue( arg.asXdmNode() ).asNameValueMap() ;
        return parseNameTypeValue(arg).asNameValueMap() ;
    }

    // attr = name|nametype / value
    public static INameAttrValueMap parseAttrNameValue(XValue name, XValue xv)
            throws UnimplementedException, UnexpectedException, IOException, InvalidArgumentException {
        mLogger.debug("parseAttributeValues(Name: " + name + " XValue: " + xv);
        AttributeValue av = parseAttrValue(xv);
        return (new NameTypeValue( name.toString() , AttrType.fromValue(av) , av )).asNameValueMap();
    }


    // Attribute = name , type , value 
    public static INameAttrValueMap parseAttrNameValue(XValue xname, XValue xtype , XValue xv)
            throws UnimplementedException, UnexpectedException, IOException, InvalidArgumentException {

        String name = parseName( xname );
        AttrType type = parseAttrType(xtype);
        AttributeValue av = parseAttrValue(xv);
        
        return (new NameTypeValue(name , type , av )).asNameValueMap();
    }

    public static DDBTypes.NameTypeValue parseNameTypeValue(String s) throws InvalidArgumentException,
    UnexpectedException {
        mLogger.debug("parseNameTypeValue: " + s);
        // Name=value
        INameStringMap nv = DDBTypes.parseNameStringValue(s);
        assert( nv.size() == 1);
        String name = nv.keySet().iterator().next();
        DDBTypes.NameType ant = new DDBTypes.NameType(  name );
        return new NameTypeValue(name, ant.getType(), DDBTypes.parseAttrValue(ant.type, nv.get(name)));
    }

    static NameTypeValue parseNameTypeValue(XValue xv) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
        if( xv.isInstanceOf(NameTypeValue.class))
            return xv.asInstanceOf(NameTypeValue.class);
        if( xv.isXdmNode())
            return parseNameTypeValue(xv.asXdmNode());
        return parseNameTypeValue( xv.toString() );
    }

    static INameAttrValueMap parseM(XValue xv)
            throws UnimplementedException {
        throw new UnimplementedException("Map type unimplemented");

    }

    static DDBTypes.NameTypeValue parseNameTypeValue(XdmNode node) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
        mLogger.debug("parseNameTypeValue: " + node.toString());
        DDBTypes.NameType ant = new DDBTypes.NameType(node);
        AttributeValue av = parseAttrValue(ant.getType(), node);
        return new DDBTypes.NameTypeValue(ant.name, ant.getType(), av);

    }
    static Collection<AttributeValue> parseL(XValue xv)
            throws UnimplementedException, UnexpectedException, InvalidArgumentException, IOException {
        mLogger.debug("parseL: " + xv);

        if( xv.isNull() || xv.isEmpty() )
            return Collections.emptyList();

        // Lists - xv is the parent - parse children if it ( using
        // parseL(List<XValue> if wanting to treat xv as the list contens
        if (xv.isXdmNode()) {
            return parseL( xv.asXdmNode() );
        }
        List<XValue> list = xv.asList() ;
        List<AttributeValue> alist = new ArrayList<AttributeValue>(list.size() );
        for ( XValue v : list )
            alist.add( parseAttrValue(v));
        return alist ;
    }

    protected static Collection<ByteBuffer> parseBS(XValue xv) throws IOException {

        ArrayList<ByteBuffer> ret = new ArrayList<ByteBuffer>();
        for (String s : xv.asStringList())
            ret.add(DDBTypes.parseBinary(s));
        return ret;

    }

    static ByteBuffer parseBinary(XValue xv) throws IOException {
        if( xv.isInstanceOf(ByteBuffer.class))
            return xv.asInstanceOf(ByteBuffer.class);
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

    public static IKeyAttrValueMap parseKey(XValue xv) throws UnexpectedException,
    UnimplementedException, IOException, InvalidArgumentException {
        if( xv.isInstanceOf( IKeyAttrValueMap.class))
            return xv.asInstanceOf(IKeyAttrValueMap.class);
        
        IKeyAttrValueMap attrs = new KeyAttrValueMap() { };
        
        DDBTypes.NameTypeValue ntv = null;
        mLogger.debug("parseKey: " + xv);

        if (xv.isXdmNode()) {
            ntv = parseNameTypeValue(xv.asXdmNode());
        }
        else if (xv.isAtomic() || xv.isString()) {
            ntv = parseNameTypeValue(xv.toString());
        } else
            throw new InvalidArgumentException("Unexpected type for attribute: " + xv.getTypeName() + " "
                    + xv.toString());
        attrs.put(ntv.getName(), ntv.value);
        return attrs;

    }

    static AttributeValue parseAttrValue(AttrType type, String value) throws UnexpectedException, InvalidArgumentException {
        switch( type ) {
        case M : 
            return parseJsonM( value );
        case L: 
            return parseL( value );
        default :
            return type.newAtomicValue(value);

        }
    }

    private static AttributeValue parseL(String value) throws InvalidArgumentException {
        throw new InvalidArgumentException(
              "parseL not implemened : "  + value );

    }

    private static AttributeValue parseJsonM(String value) {
        return new AttributeValue().withM( com.amazonaws.services.dynamodbv2.document.internal.InternalUtils.toAttributeValues( Item.fromJSON(value) ) );
        
    }

    public static IKeyAttrValueMap parseKey(XValue name, XValue value)
            throws UnexpectedException, UnimplementedException, IOException,
            InvalidArgumentException {
        mLogger.debug("parseKey: name: " + name + "value: " + value );
        return new KeyAttrValueMap(parseAttrNameValue(name, value));
    }

    protected static INameStringMap parseNameValue(XValue xv) throws InvalidArgumentException {
        mLogger.debug("parseNameValue: " + xv );
        if( xv.isInstanceOf(INameStringMap.class))
            return xv.asInstanceOf(INameStringMap.class);
        // placeholder=literal
        if (xv.isAtomic()) {
            return parseNameStringValue(xv.toString());
        } else
            throw new InvalidArgumentException(
                    "Unexpected attribute name expression. expected 'placeholder=literal'  : " + xv.toString());
    }

    public static INameStringMap  parseNameStringValue(String s) throws InvalidArgumentException {
        mLogger.debug("parseNameValue: " + s );
        if( s.startsWith("{")){
            Map<String, Object> map = Item.fromJSON(s).asMap();
            INameStringMap smap = new NameStringMap();
            for( String key : map.keySet() )
                smap.put(key, map.get(key).toString() );
            return smap;
        }
        else if( s.contains("=") ){
            StringPair keypair = new StringPair(s,'=');
            if( keypair.hasLeft() )
                return new NameStringMap(Collections.singletonMap( keypair.getLeft() , keypair.getRight() ));
        }
        throw new InvalidArgumentException( "Unexpected attribute name expression. expected 'placeholder=literal'  : " + s.toString());
    }

    public static IAttrNameExpr parseAttrNameExprs(List<XValue> nameExprs)
            throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        mLogger.debug("parseAttrNameExprs(List<XValue>): " );

        IAttrNameExpr map = new AttrNameExpr();
        for (XValue v : nameExprs) {
              map.putAll( parseAttrNameExpr(v));
        }
        return  map;
    }
    public static IAttrNameExpr parseAttrNameExpr(XValue v) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
        if( v.isInstanceOf( IAttrNameExpr.class ) )
            return v.asInstanceOf( IAttrNameExpr.class );
        else
            return  new AttrNameExpr( parseNameValues(v));
        
    }
    
    public  static INameStringMap parseNameValues(XValue xv) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        if( xv.isInstanceOf(INameStringMap.class))
            return xv.asInstanceOf(INameStringMap.class);
        
        if( xv.isInstanceOf(INameStringMap.class))
           return xv.asInstanceOf(INameStringMap.class);
        if( xv.isAtomic() )
            return parseNameValue( xv );

        if( xv.isJson() ){ 
            return parseNameStringValue( xv.asJson().toJson() ) ;
        }
        
        if( xv.isXExpr() ) {
            INameStringMap map = new NameStringMap();
            DDBTypes.NameTypeValue ntv = DDBTypes.parseNameTypeValue(xv);
            map.put( ntv.getName() ,ntv.value.toString() );
            return map ;
        }
        throw new InvalidArgumentException("expected string , xml or json: " + xv.getTypeName() );

    }

    protected static  INameObjectMap   parseAttrValueObjectMap(XValue v) throws InvalidArgumentException {

        if( v.isInstanceOf(INameObjectMap.class))
            return v.asInstanceOf(INameObjectMap.class);
        
        if( v.isString()){
            return DDBTypes.parseAttrValueObjectMap( v.toString().trim() );
        } else 
            if( v.isJson() )
                return DDBTypes.parseAttrValueObjectMap( v.asJson().toJson() );
        throw new InvalidArgumentException("expected string or json: " + v.getTypeName() );
    }

    // Name/Values for attribute expressions in Docuemnt mode
    protected static  INameObjectMap parseAttrValueObjectMap(List<XValue> values) throws InvalidArgumentException {
        INameObjectMap map = new NameObjectMap();
        for( XValue v : values )
            map.putAll( parseAttrValueObjectMap( v ) );
        return map;
    }

    protected static INameObjectMap  parseAttrValueObjectMap( String s) throws InvalidArgumentException{
        if( s.startsWith("{"))
            return new NameObjectMap( Item.fromJSON(s).asMap());
        else if( s.contains("=") ){
            StringPair keypair = new StringPair(s,'=');
            if( keypair.hasLeft() )
                return  new NameObjectMap(Collections.singletonMap( keypair.getLeft() , AWSDDBCommand.parseToJavaValue(keypair.getRight())));
        }
        throw new InvalidArgumentException("name value expect  name=value: " + s );
    }

    protected  static  Item parseItem(List<XValue> args) throws InvalidArgumentException {
        INameObjectMap values = parseAttrValueObjectMap( args );
        return Item.fromMap(values);
    }

    public static IAttrNameExpr parseAttrNameExprs(Options opts) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        if( opts.hasOpt("attr-name-expr"))
            return parseAttrNameExprs(
                    Util.expandSequences(
                            opts.getOptValues("attr-name-expr")));
        return null;
    }

    public  static  INameObjectMap parseKeyValueObjectOptions(Options opts)
            throws InvalidArgumentException, UnexpectedException,
            UnimplementedException, IOException, XPathException {
        INameObjectMap keys = new NameObjectMap();
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

    public  static  IAttrObjectExpr parseAttrValueObjectExprs(Options opts) throws InvalidArgumentException {
        if( opts.hasOpt("attr-value-expr"))
            return new AttrObjectExpr( parseAttrValueObjectMap(Util.expandSequences(opts.getOptValues("attr-value-expr"))));
        return null;
    }

    public  static  IAttrValueExpr parseAttrValueExprs(Options opts) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        if( opts.hasOpt("attr-value-expr"))
            return new AttrValueExpr( parseAttrValueExprs(Util.expandSequences(opts.getOptValues("attr-value-expr"))));
        return null;
    }

    public  static  IAttrValueExpr parseAttrValueExprs(List<XValue> valueExprs) throws InvalidArgumentException, UnexpectedException, UnimplementedException, IOException {
        IAttrValueExpr map = new AttrValueExpr();
        for (XValue v : valueExprs) {
                map.putAll( parseAttrValueExpr(v));
        }
        return map;
    }
    
    
    public static IAttrValueExpr parseAttrValueExpr(XValue v) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
        if( v.isInstanceOf( IAttrValueExpr.class ) )
            return v.asInstanceOf( IAttrValueExpr.class );
        else
            return  new AttrValueExpr( parseAttrNameValue(v));
    }

    public  static  INameObjectMap parseItemValueObject(Options opts) throws InvalidArgumentException, UnexpectedException,
    UnimplementedException, IOException, XPathException {
        INameObjectMap item = parseKeyValueObjectOptions(opts);
        if( opts.hasRemainingArgs() )
            item.putAll(parseAttrValueObjectMap( opts.getRemainingArgs() ));
        return item;
    }

    public static Object     parseToJavaValue(XValue xv) throws XPathException {
        if( xv == null || xv.isNull() )
            return null ;
        if( xv.isEmpty() )
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

    public static  IAttrNameExpr addNamePrefix( IAttrNameExpr  exprs ) {
        IAttrNameExpr ret = null ;
        for( Entry<String, String> e :  exprs.entrySet() ) {
            if( ! e.getKey().startsWith( "#" ) )  {
                if( ret == null) ret = new AttrNameExpr();
                ret.put( "#" + e.getKey() ,  e.getValue() );
            }
        }
        return ret == null ? exprs : ret  ;
    }

    public static IAttrValueExpr addValuePrefix( IAttrValueExpr  exprs ) {
        IAttrValueExpr ret = null ;
        for( Entry<String, AttributeValue> e :  exprs.entrySet() ) {
            if( ! e.getKey().startsWith( ":" ) )  {
                if( ret == null) ret = new AttrValueExpr();
                ret.put( ":" + e.getKey() ,  e.getValue() );
            }
        }
       return ret == null ? exprs : ret  ;
    }

    public static String parseName(XValue xv) throws InvalidArgumentException {
        if (xv.isXdmNode()) {
           return NameType.parseName( xv.asXdmNode() );
        }
        else if (xv.isString())
            return NameType.parseName( xv.toString() );
        else
            throw new InvalidArgumentException(
                    "Unexpected argument type for attribute: " + xv.getTypeName());
        
    }

}
