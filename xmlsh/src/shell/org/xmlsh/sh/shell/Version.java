/**
 * $Id$
 * $Date$
 *
 */

package org.xmlsh.sh.shell;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.Logger;
import org.xmlsh.util.Util;

public class Version {
    private static Properties mProperties ;
    public enum Key  implements Supplier<String>{
         RELEASE("release" ,  Version::getRelease  ),
         VERSION("version" , Version::getVersion),
         SAXON_VERSION("saxon-version", Version::getSaxonVersion ),
         SAXON_EDITION("saxon-edition", Version::getSaxonEdition ),
         JAVA_VERSION("java-version" , Version::getJavaVersion ),
         JAVA_NAME("java-name" , Version::getJavaName ),
         JAVA_HOME("java-home" , Version::getJavaHome ),
         BUILD("build", Version::getBuildDate ) ; 
         String name ;
         Supplier<String> method;
         
         Key( String name , Supplier<String> f ){
             this.name  = name ; 
             method = f ;
         }
          
         @Override
        public String get() {
              return method.get();
        }
         
          public static Key getKey( String name ){
              for( Key key : values() ){
                  if( key.name.equals(name) || key.toString().endsWith(name))
                      return key;
              }
              return null;
          }

        public String getName() {
            return name;
        }
    }
    
	public static enum Op implements BiFunction<String,String,Boolean> {
        GT("gt", (l,r) -> toNum(l).compareTo( toNum(r)) > 0 ),
        LT("lt", (l,r)  -> toNum(l).compareTo( toNum(r))  < 0),
        EQ("eq" , (l,r) -> toNum(l).equals(toNum(r) )),
        GTE("gte",(l,r) ->toNum(l).compareTo( toNum(r))  >= 0 ),
        LTE("lte",(l,r) -> toNum(l).compareTo( toNum(r))  <= 0 ),
        MATCHES("matches",(l,r) -> l.matches(r) )
        ;
        private static Double toNum(String s) {
           // need to convert arbitrary strings to double
            return (parseVersion(s));
        };
        String name ;
        private final BiFunction<String,String,Boolean> matcher;
    
        Op(String name  , BiFunction<String,String,Boolean> matcher ) { 
            this.name=name ; 
            this.matcher = matcher;     
       }
        public static Op getOp( String name ){
            for( Op op : values() ){
                if( op.name.equals(name) || op.toString().endsWith(name))
                    return op;
            }
            return null;
            
        }
    
    
        @Override
        public Boolean apply(final String left, final String right) {
            return matcher.apply(left, right);
        }
    }


	static Logger mLogger = org.apache.logging.log4j.LogManager.getLogger();
	static {
		mProperties = new Properties();
		try {
			InputStream stream = Version.class.getResourceAsStream("version.properties");
			mProperties.load( stream);
			stream.close();
		} catch (IOException e) {
			mLogger.debug("Exception loading version.properties",e);
		}
	}


	public static String getProperty(String name){
	       if( mProperties == null )
	           return "";
           return mProperties.getProperty(name);

	}

	public static String getBuildDate() { return getProperty("version.build_date"); }

	public static String getRelease() { return getProperty("version.release"); }
	public static String getVersion() { return getProperty("version.version"); }

    public static String getJavaProperty(String... prop) {
        for( String p : prop ){
            String v = System.getProperty( p , null);
            if( v != null ) return v ;
        }
        return "";
    }

    public static String getJavaHome() {
        return getJavaProperty("java.home");
    }

    public static String getJavaName() {
        return getJavaProperty("java.vm.name","java.runtime.name");
    }

    public static String getJavaVersion() {
        return getJavaProperty("java.version","java.runtime.version");
    }
    
    public static double parseVersion( String version ){
            // create a N dimension . list
            String aver[] =version.split("\\.");
            int dots = aver.length;
            int pos = 0;
            double v = Util.parseInt(aver[pos++], 0);
            while( pos < dots ){
                v += Util.parseInt( aver[pos++] , 0 ) / 1000. ;
            }
            return v;
        }

    public static boolean matches( String value , String matches ,  Op op ){ 
        mLogger.entry(value, matches,op);
        assert( op != null );
        
        if( value == null )
            value = getVersion();
        return mLogger.exit(op.apply( value , matches ) );
    }

    public static String getSaxonEdition() {
        return Shell.getProcessor().getUnderlyingConfiguration().getEditionCode();
    }

    public static String getSaxonVersion() {
        return Shell.getProcessor().getSaxonProductVersion();
    }
}
