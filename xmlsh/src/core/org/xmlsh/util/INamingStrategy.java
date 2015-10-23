package org.xmlsh.util;
import java.util.Comparator;
import java.util.function.Function;
import javax.xml.namespace.QName;


public interface INamingStrategy {
	
	QName toXmlName(String name);
    String fromXmlName(QName qname);
    
    static class NamingStrategy implements INamingStrategy {
    	private Function<String,QName> toXml;
    	private  Function<QName,String> fromXml ;
    	public NamingStrategy( 
    			Function<QName,String>  from, 
    			Function<String, QName>  to ) {
    		fromXml = from  ;
    		toXml = to ;
    	}
		@Override
		public QName toXmlName(String name) {
			return toXml.apply(name);
		}

		@Override
		public String fromXmlName(QName qname) {
			return fromXml.apply(qname);
		}
    };
    
    static INamingStrategy DefaultNamingStrategy = 
    		new NamingStrategy( 
    				 Util::decodeFromQName , 
    				 Util::encodeForQName ); 
    
    static INamingStrategy SimpleNamingStrategy = 
    		new NamingStrategy( 
    				 Util::decodeFromQNameSimple , 
    				 Util::encodeForQNameSimple );  
    static INamingStrategy LocalNamingStrategy = 
    		new NamingStrategy( 
    		     q -> q.getLocalPart()	,
    		        QName::new
    		) ;
    
}
