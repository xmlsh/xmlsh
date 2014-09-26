package org.xmlsh.sh.module;

import java.net.URL;
import java.util.List;

import org.xmlsh.sh.shell.SerializeOpts;

public class ModuleConfig {
	private String type;
	public String getType() {
		return type;
	}


	private  String mType;
	private List<URL> mClassPath;
	private List<String> mPackages;
	private String mHelpURI;
	private SerializeOpts mSerialOpts;
	private String mModuleClass;
	private ClassLoader mClassLoader ;
	
	public ClassLoader getClassLoader() {
		return mClassLoader;
	}


	public void setType(String type) {
		this.type = type;
	}


	public ModuleConfig(String type , String name, List<URL> classpath, SerializeOpts serialOpts,
			List<String> mPackages, String mHelpURI) {
		assert( serialOpts !=null);
		assert( name != null );
		assert( type != null );
		this.type =type ;
		this.mType = name;
		this.mClassPath = classpath;
		this.mSerialOpts = serialOpts;
		this.mPackages = mPackages;
		this.mHelpURI = mHelpURI;
		
	}


	public ModuleConfig(String type , String name, List<URL> classpath, 
			SerializeOpts serialOpts) {
		assert( serialOpts !=null);
		assert( name != null );
		assert( type != null );
		this.type =type ;
		this.mType = name;
		this.mClassPath = classpath;
		this.mSerialOpts = serialOpts;
	}
	
	
	public ModuleConfig(String type) {
		this.type = type ;
	}


	public String getInputTextEncoding() {
		return mSerialOpts.getInputTextEncoding();
	}


	public String getOutput_xml_encoding() {
		return mSerialOpts.getOutput_xml_encoding();
	}


	public String getName() {
		return mType;
	}
	public void setName(String name) {
		this.mType = name;
	}
	public List<URL> getClasspath() {
		return mClassPath;
	}

	public List<String> getPackages() {
		return mPackages;
	}


	public String getHelpURI() {
		return mHelpURI;
	}


	public String getModuleClass() {
		return mModuleClass;
	}


	public SerializeOpts getSerialOpts() {
		return mSerialOpts == null ?  SerializeOpts.defaultOpts : mSerialOpts;
	}


	public void setSerialOpts(SerializeOpts serialOpts) {
		mSerialOpts = serialOpts;
	}


	public void setClasspath(List<URL> classpath) {
		this.mClassPath = classpath;
	}


	public void setPackages(List<String> packages) {
		mPackages = packages;
	}


	public void setHelpURI(String helpURI) {
		mHelpURI = helpURI;
	}


	public void setModuleClass(String moduleClass) {
		mModuleClass = moduleClass;
	}


	public void setClassLoader(ClassLoader classLoader) {
		mClassLoader = classLoader ;
		
	}
	
	
	

}
