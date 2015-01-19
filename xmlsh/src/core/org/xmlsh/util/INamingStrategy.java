package org.xmlsh.util;



public interface INamingStrategy {
    String  toXmlName( String name );
    String  fromXmlName( String name );
    
    public static class EncodedNamingStrategy implements INamingStrategy {
        @Override
        public String toXmlName(String name) {
            return Util.encodeForNCName(name);
        }

        @Override
        public String fromXmlName(String name) {
            return Util.decodeFromNCName(name);
        }
    }
    public static INamingStrategy DefaultNamingStrategy = new EncodedNamingStrategy();
}
