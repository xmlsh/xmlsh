package org.xmlsh.util;

import javax.xml.namespace.QName;



public interface INamingStrategy {
    QName  toXmlName( String name );
    String  fromXmlName( QName name );
    
    public static class EncodedNamingStrategy implements INamingStrategy {
        @Override
        public QName toXmlName(String name) {
            return Util.encodeForQName(name);
        }

        @Override
        public String fromXmlName(QName name) {
            return Util.decodeFromQName(name);
        }
    }
    public static INamingStrategy DefaultNamingStrategy = new EncodedNamingStrategy();
}
