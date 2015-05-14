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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.xmlsh.aws.util.DDBTypes.NameType;
import org.xmlsh.aws.util.DDBTypes.NameTypeValue;
import org.xmlsh.core.InvalidArgumentException;
import org.xmlsh.core.UnexpectedException;
import org.xmlsh.core.UnimplementedException;
import org.xmlsh.core.XValue;
import org.xmlsh.util.Base64;
import org.xmlsh.util.Base64.InputStream;
import org.xmlsh.util.StringPair;
import org.xmlsh.util.Util;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public  class DDBTypes {

  public enum AttrType {
    S,N,B,SS,NS,BS,M,L,NULL,BOOL ;
    public String toString() { return this.name() ; }
    public static AttrType parseType( String name ) {
        return parseType(name,null);
    }
    public static AttrType parseType( String name , AttrType def ) {
        if( name == null)
            return def ;
        for( AttrType n : values() )
            if( n.name().equals(name) ) 
                return n;
        return def;
    }
    public static AttrType parseType( XdmNode node , AttrType def ) {
        if( node == null )
            return def ;
        switch( node.getNodeKind() ){
        case ATTRIBUTE :
           // if( node.getNodeName().getLocalName().equals("type"))
              return parseType( node.getStringValue() ,def );
        case ELEMENT :
          String stype = node.getAttributeValue(new QName("type"));
          return parseType( stype  , def);
        case TEXT :
        default :
         return parseType( node.getStringValue() , def );
        }
    }
    public static AttrType parseType( XdmNode node ) {
        return parseType(node,S);
    }

    
    AttributeValue newValue() {
        return new AttributeValue();
    }
    public AttributeValue newAtomicValue(String s) throws UnexpectedException {
        switch( this ){
        case N : 
            return new AttributeValue().withN(s);
        case S:
            return new AttributeValue().withS(s);
        case BOOL:
            return new AttributeValue().withBOOL(Util.parseBoolean(s));
        case NULL:
            return new AttributeValue().withNULL(Util.parseBoolean(s));
       default: 
           throw new UnexpectedException("Unexpected type for newAtomicValue: " + this.name() );
        }
    }
}
  public static AttributeValue parseAttributeValue(XdmNode node) throws UnexpectedException {
      AttrType type = AttrType.parseType(node);
      return parseAttributeValue( type , node );
  }
public static AttributeValue parseAttributeValue(AttrType type, XdmNode node) throws UnexpectedException {
    switch( node.getNodeKind()){
    case ATTRIBUTE : 
    case ELEMENT : 
        String value =  node.getAttributeValue(new QName("value"));
        if( value == null )
            value = node.getStringValue();
        return type.newAtomicValue( value );
    default:
    case TEXT :
        return type.newAtomicValue( node.getStringValue() );
    }
}
        
        public static class NameValue<T> {
            public String name;
            public T value; 
            NameValue( String name , T value ){
                this.name = name ;
                this.value = value ;
            } 
            Entry<String,T> asEntry() { 
                return new SimpleImmutableEntry<String,T>(name,value);
            }
        }

        /*
         * Attribute Name/Type 
         *   Type is optional and defaults to "S" and stored in Left
         *   Name is stored in right
         */
        public static class NameType {
            String name;
            AttrType type;
            private void _init( String def ){
                /*
                StringPair pair = new StringPair( def , ':');
                name = pair.getRight();
                type = pair.hasLeft() ? pair.getLeft() : "S";
                 */
                name = def.trim(); 
                type = S;
            }
            NameType( String def ) {
                _init(def);
            }
            NameType( String name ,AttrType type ){
                this.name = name ;
                this.type = type ;
            }
            NameType( String name ,String type ){
                this.name = name ;
                this.type = AttrType.parseType(type);
            }
            NameType( XValue xv ) throws InvalidArgumentException {
                _init(xv);
            }
        
            private void _init( XValue xv ) throws InvalidArgumentException{
                if( xv.isXdmNode() ){
                    _init( xv.asXdmNode());
                } 
                else
                    if( xv.isString() )
                        _init( xv.toString() );
                    else
                        throw new InvalidArgumentException(
                                "Unexpected argument type for attribute: " + xv.getTypeName());
            }
            NameType( XdmNode node ){
                _init( node );
            }
            private void _init( XdmNode node ) {
                type = AttrType.parseType( node  );
                switch( node.getNodeKind()){
                case ELEMENT : 
                  name = node.getAttributeValue(new QName("name"));
                  if( name == null )
                      name = node.getNodeName().getLocalName();
                  break ;
                case TEXT :
                case ATTRIBUTE :
                default :
                 name = node.getStringValue();
                
                }
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
            AttributeValue value ;
            NameTypeValue(String name , AttrType  type ,AttributeValue value  ){
                super( name , type );
                this.value = value;
            }
            public NameTypeValue(XdmNode node) throws UnexpectedException {
                super(node);
                this.value = parseAttributeValue(getType(), node);
            }
        }

        public static AttributeValue parseAttributeValue(XdmItem item) throws UnexpectedException {
            if( item instanceof XdmNode )
                return parseAttributeValue( (XdmNode) item );
            return parseAttributeValue(item.getStringValue());
        }
        private static AttributeValue parseAttributeValue(String s) throws UnexpectedException {
            return parseAttributeValue(S, s);
        }
        public static AttributeValue parseAttributeValue(XValue xv) throws UnexpectedException, InvalidArgumentException, UnimplementedException, IOException {
            return parseAttributeValue(parseAttribteType(xv), xv);
        }
        public static AttrType parseAttribteType(XValue xv) throws InvalidArgumentException {
            if( xv.isXdmNode() )
                return AttrType.parseType(xv.asXdmNode());
            if( xv.isString() )
                return S;
            if( xv.isNull())
                return AttrType.NULL ;
            if( xv.isAtomic()){
                return  S ;
            }
            throw new InvalidArgumentException("Unexpected type for attribute: " + xv.toString() +" type: "+ xv.getTypeName() );
        
        }
        protected static AttributeValue parseAttributeValue( String  stype , XValue xv ) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException{
            return DDBTypes.parseAttributeValue(AttrType.parseType(stype,AttrType.S),xv);
        }
        public static AttributeValue parseAttributeValue( AttrType type , XValue xv ) throws UnexpectedException, UnimplementedException, IOException, InvalidArgumentException{
            switch( type ){
            case N : 
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
            default :
                throw new UnexpectedException("Unknown type: " + type.toString());
            }
        
        } 
        /*
         * Parse a set of attributes 
         *   [type:]Name Value ... 
         *               -> any value of type 
         *   
         *   <attribute name="name" type="type">
         *          value
         *   <attribute>
         *      
         *   
         */
        public static Map<String, AttributeValue> parseAttributeValues(List<XValue> args)
                throws IOException, UnexpectedException, UnimplementedException,
                InvalidArgumentException {
            Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
            for( XValue arg : args ){
                DDBTypes.NameTypeValue ntv = DDBTypes.parseNameTypeValue(arg);
                attrs.put( ntv.getName() , ntv.value );
            }
            return attrs;
        }
        public static Map<String, AttributeValue> parseAttributeNameValue( XValue name, XValue xv ) throws UnimplementedException, UnexpectedException, IOException, InvalidArgumentException {
            Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
            AttributeValue av  = parseAttributeValue(parseAttribteType(xv) , xv );
            attrs.put(name.toString(), av);
            return attrs;
        }
        public static DDBTypes.NameTypeValue parseNameTypeValue( String s ) throws InvalidArgumentException, UnexpectedException{
            //Name=value
            NameValue<String> nv = DDBTypes.parseNameValue( s); 
            DDBTypes.NameType ant = new DDBTypes.NameType( nv.name );
            return new DDBTypes.NameTypeValue(  nv.name , ant.getType() , DDBTypes.parseAttributeValue( ant.type , nv.value ) );
        }
        static DDBTypes.NameTypeValue parseNameTypeValue( XValue a ) throws UnexpectedException, InvalidArgumentException{
            // <attribute element>
            if( a.isXdmNode() ) {
                return new DDBTypes.NameTypeValue(  a.asXdmNode() );
            }
            else {
                // [type:]NameValue
                if( a.isAtomic() || a.isString() )
                    return parseNameTypeValue( a.toString() );
                else
                    throw new InvalidArgumentException("Unexpected type for attribute: " + a.getTypeName() );
            }
        }
        static Map<String, AttributeValue> parseM(XValue xv)
                throws UnimplementedException {
            throw new UnimplementedException("Map type unimplemented");
        
        }
        static DDBTypes.NameTypeValue parseNameTypeValue( XdmNode node  ) throws UnexpectedException, InvalidArgumentException{
            DDBTypes.NameType ant = new DDBTypes.NameType(node);
            AttributeValue av  = parseAttributeValue(ant.getType(), node );
            return new DDBTypes.NameTypeValue(  ant.name , ant.getType() , av );
        
        }
        static Collection<AttributeValue> parseL(XValue xv)
                throws UnimplementedException, UnexpectedException, InvalidArgumentException, IOException {
            XdmSequenceIterator iter = xv.asXdmSequenceIterator();
            List<AttributeValue> list = new ArrayList<AttributeValue>( );
        
            if( iter != null ){
                while( iter.hasNext())
                    list.add( parseAttributeValue( (XdmNode) iter.next() ));
                return list ;
            }
            return Collections.singletonList( parseAttributeValue( xv ));
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
            return xv.asStringList();
        }
        public static Map<String, AttributeValue> parseKey(XValue key) throws UnexpectedException,
        UnimplementedException, IOException, InvalidArgumentException {
            Map<String, AttributeValue> attrs = new HashMap<String, AttributeValue>();
            DDBTypes.NameTypeValue ntv = null;
            if( key.isXdmNode() ){
                ntv = parseNameTypeValue( key.asXdmNode() );
            }
            else
                if( key.isAtomic() || key.isString() ){
                    ntv = parseNameTypeValue( key.toString() );
                } else 
                    throw new InvalidArgumentException("Unexpected type for attribute: " + key.getTypeName() + " " + key.toString() );
            attrs.put( ntv.getName() , ntv.value );
            return attrs;
        
        }
        static AttributeValue parseAttributeValue(AttrType type, String value) throws UnexpectedException {
            return  type.newAtomicValue(value);
        }
        public static Map<String, AttributeValue> parseKey(XValue name, XValue value)
                throws UnexpectedException, UnimplementedException, IOException,
                InvalidArgumentException {
            return  parseAttributeNameValue(name, value);
        }
        protected static NameValue<String> parseNameValue(XValue nv) throws InvalidArgumentException{
            // placeholder=literal
            if( nv.isAtomic()  ){
                return DDBTypes.parseNameValue( nv.toString() );
            } else
                throw new InvalidArgumentException("Unexpected attribute name expression. expected 'placeholder=literal'  : " + nv.toString()  );
        }
        protected static NameValue<String>  parseNameValue(String nv) throws InvalidArgumentException{
            // placeholder=literal
                StringPair pair=new StringPair( nv, '=' );
                if( ! pair.hasLeft() )
                    throw new InvalidArgumentException("Unexpected attribute name expression. expected 'placeholder=literal'  : " + nv.toString()  );
                return new NameValue<String>( pair.getLeft() , pair.getRight() );
        }
        public static Map<String, String> parseAttrNameExprs(List<XValue> nameExprs)
                throws InvalidArgumentException {
            Map<String, String> map =  new HashMap<String, String>();
            for( XValue v : nameExprs ){
                NameValue<String> nv = parseNameValue(v);
                map.put(  nv.name  , nv.value );
            }
            return map;
        }
    
}
