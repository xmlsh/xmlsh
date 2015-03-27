package org.xmlsh.sh.module;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;
import org.xmlsh.util.JavaUtils;
import org.xmlsh.util.Util;


public class ModuleConfig {

	private  String mType;
	private  String mName ;
	private List<URL> mClassPath;
	private List<String> mPackages;
	private List<URL> mModulePath;
	private String mHelpURI;
	private SerializeOpts mSerialOpts;
	private String mModuleClass;
	
	// Additional class names to search 
	private List<String>  mClassNames;
	
	
	public List<String> getClassNames() {
		return mClassNames;
	}


	public void setClassNames(List<String> classNames) {
		mClassNames = classNames;
	}

	static Logger mLogger = LogManager.getLogger();
	
	public ModuleConfig(String type) {
		this.mType = type ;
	}

	
	@Deprecated
	public ModuleConfig(String type , String name, List<URL> classpath, 
            SerializeOpts serialOpts) {
        this( type,name,null,classpath,classpath,serialOpts);
    }


	public ModuleConfig(String type , String name, 
	        String modclass ,
	        List<URL> classpath, 
	        List<URL> modpath,
			SerializeOpts serialOpts) {
		
		assert( serialOpts !=null);
		assert( name != null );
		assert( type != null );
		
		this.mType =type ;
		this.mName = name;
		this.mModuleClass = modclass;
		this.mClassPath = classpath;
		this.mModulePath = modpath;
		this.mSerialOpts = serialOpts;
	}


	public ModuleConfig(String type , String name, List<URL> classpath, List<URL> modpath, SerializeOpts serialOpts,
			List<String> mPackages, String mHelpURI) {

		mLogger.entry(type, name, classpath, modpath , serialOpts, mPackages, mHelpURI);
		
		
		assert( serialOpts !=null);
		assert( name != null );
		assert( type != null );
		this.mType =type ;
		this.mName = name;
		this.mClassPath  = JavaUtils.uniqueList( classpath);
		this.mModulePath = JavaUtils.uniqueList(modpath);
		this.mSerialOpts = serialOpts;
		this.mPackages = mPackages;
		this.mHelpURI = mHelpURI;
		
	}


	

	public synchronized List<URL> getClassPath() {
	    return mClassPath;
	}

    public synchronized List<URL> getModulePath() {
        return mModulePath;
    }

	public String getHelpURI() {
		return mHelpURI;
	}


	public String getInputTextEncoding() {
		return mSerialOpts.getInputTextEncoding();
	}
	public String getModuleClass() {
		return mLogger.exit( mModuleClass);
	}
	public String getName() {
		return mName;
	}

	public String getOutput_xml_encoding() {
		return mSerialOpts.getOutput_xml_encoding();
	}


	public List<String> getPackages() {
		return mPackages;
	}


	public SerializeOpts getSerialOpts() {
		return mSerialOpts == null ?  SerializeOpts.defaultOpts : mSerialOpts;
	}


	public String getType() {
		return mType;
	}




	public void setHelpURI(String helpURI) {
		mHelpURI = helpURI;
	}


	public void setModuleClass(String moduleClass) {
		
		mLogger.entry(moduleClass);
		mModuleClass = moduleClass;
	}


	public void setName(String name) {
		this.mName = name;
	}


	public void setPackages(List<String> packages) {
		mPackages = packages;
	}


	public void setSerialOpts(SerializeOpts serialOpts) {
		mSerialOpts = serialOpts;
	}


	public void setType(String type) {
		this.mType = type;
	}


	public synchronized boolean addClassPaths(List<URL> urls) {
		    if( Util.isEmpty(mClassPath)){
		    	mClassPath = JavaUtils.uniqueList(urls);
		        return ! Util.isEmpty(mClassPath);
		    }
		    
			List<URL>  newPath = new ArrayList<>( mClassPath );
			if( ! newPath.addAll( urls ) )
				return false ;
			newPath = JavaUtils.uniqueList(newPath);
			
			if( mClassPath.equals(newPath) )
				return false ;
			mClassPath = newPath ;
			return true ;
			
		
	}

	public synchronized void addClassName(String clsname) {
 
		if( mClassNames == null )
			mClassNames = new ArrayList<>();

	    mClassNames.add( clsname );
		
	}
	
	

}
