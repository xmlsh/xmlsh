package org.xmlsh.sh.module;

import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.sh.shell.SerializeOpts;

public class ModuleConfig {

static Logger mLogger = LogManager.getLogger();
	private  String mType;
	private  String mName ;
	private List<URL> mClassPath;
	private List<String> mPackages;
	private String mHelpURI;
	private SerializeOpts mSerialOpts;
	private String mModuleClass;
	private ClassLoader mClassLoader ;
	
	public ModuleConfig(String type) {
		this.mType = type ;
	}


	public ModuleConfig(String type , String name, List<URL> classpath, 
			SerializeOpts serialOpts) {
		
		mLogger.entry(type, name, classpath, serialOpts);
		assert( serialOpts !=null);
		assert( name != null );
		assert( type != null );
		this.mType =type ;
		this.mName = name;
		this.mClassPath = classpath;
		this.mSerialOpts = serialOpts;
	}


	public ModuleConfig(String type , String name, List<URL> classpath, SerializeOpts serialOpts,
			List<String> mPackages, String mHelpURI) {
		
		mLogger.entry(type, name, classpath, serialOpts, mPackages, mHelpURI);
		assert( serialOpts !=null);
		assert( name != null );
		assert( type != null );
		this.mType =type ;
		this.mName = name;
		this.mClassPath = classpath;
		this.mSerialOpts = serialOpts;
		this.mPackages = mPackages;
		this.mHelpURI = mHelpURI;
		
	}


	public ClassLoader getClassLoader() {
		return  mLogger.exit(mClassLoader);
	}
	
	
	public List<URL> getClasspath() {
		return mClassPath;
	}


	public List<URL> getClassPath() {
		return mClassPath;
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


	public void setClassLoader(ClassLoader classLoader) {
		
		mLogger.entry(classLoader);
		mClassLoader = classLoader ;
		
	}


	public void setClassPath(List<URL> classPath) {
		mLogger.entry(classPath);
		mClassPath = classPath;
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
	
	
	

}
