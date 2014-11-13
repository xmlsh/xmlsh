/**
 * 
 */
package org.xmlsh.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.shell.Shell;
import org.xmlsh.util.StringPair;
import org.xmlsh.xpath.ThreadLocalShell;

/**
 * @author DLEE
 *
 */
public abstract class XStringLookup extends StrLookup<String> {

    private Map<String,XStringLookup> mSchemes ;
    private static Map<String,XStringLookup> sDefaultSchemes = new HashMap<>();

    private XStringLookup mParent = null ;
    
    static Logger mLogger = LogManager.getLogger();
    
    public XStringLookup() {
        this( null , getDefaultSchemes() );
    }


    public XStringLookup(XStringLookup parent , Map<String,XStringLookup>  schemes  ) 
    {
        super();
        mSchemes = schemes ;
        mParent = parent ;
    }
    

    public static Map<String, XStringLookup> getDefaultSchemes() {
        synchronized( sDefaultSchemes){
            if( sDefaultSchemes.isEmpty()){
                sDefaultSchemes.put("env", new XStringLookup(null,null){

                    @Override
                    protected XValue lookupXValue(String name) {
                        Shell shell = ThreadLocalShell.get();
                        if( shell != null )
                            return shell.getEnv().getVarValue(name);
                        return null;
                        
                    }} );
                
            }
        }
        
        return sDefaultSchemes;
    }
    
    
    /**
     * @param defaultSchemes
     */
    public XStringLookup(Map<String, XStringLookup> schemes) {
       this(null, schemes);
    }


    /**
     * @param parent
     */
    public XStringLookup(XStringLookup parent) {
        this( parent, getDefaultSchemes());
    }


    protected  XStringLookup getScheme( String scheme ){
        if( mSchemes == null )
            return null;
        return mSchemes.get(scheme);
    }
    
    
    protected abstract XValue lookupXValue( String value ) throws InvalidArgumentException;
    
    protected XValue lookupXValue( String scheme , String name ) throws InvalidArgumentException{
        XValue value = null;
        
         // Scheme specific lookup 
        if( scheme != null ){
            XStringLookup schemeLookup = getScheme(scheme);
            if( schemeLookup != null )
                value =  schemeLookup.lookupXValue( name );
            if( value == null && mParent != null )
               value = mParent.lookupXValue(scheme,name);
            return value ;
        } 
        
        // no scheme
        value = lookupXValue( name );
        if( value == null && mParent != null )
            value = mParent.lookupXValue(name);
        return value ;
    }
    
    @Override
    public final String lookup(String key) 
    {
    
        mLogger.entry(key);
        StringPair pair = new StringPair(key, ':');
        String scheme = pair.getLeft();
        String name = pair.getRight();
        XValue value=null;
        try {
            value = lookupXValue( scheme , name );
        } catch (InvalidArgumentException e) {
            // TODO Auto-generated catch block
            mLogger.catching(e);
        }
        if( value == null )
            return mLogger.exit(null);
    
        // TODO: Better determination of if to convert to string
        return mLogger.exit(value.toString());
    
    }

}