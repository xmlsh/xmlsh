package org.xmlsh.sh.module;

import java.net.URL;
import java.util.List;

import org.xmlsh.sh.shell.SerializeOpts;

public class ModuleConfig {
	private  String name;
	private List<URL> classpath;
	private List<String> mPackages;
	private String mHelpURI;
	private SerializeOpts mSerialOpts;
	private String mModuleClass;
	
	public ModuleConfig(String name, List<URL> classpath, SerializeOpts serialOpts,
			List<String> mPackages, String mHelpURI) {
		assert( serialOpts !=null);
		assert( name != null );
		
		this.name = name;
		this.classpath = classpath;
		this.mSerialOpts = serialOpts;
		this.mPackages = mPackages;
		this.mHelpURI = mHelpURI;
		
	}


	public ModuleConfig(String name, List<URL> classpath, 
			SerializeOpts serialOpts) {
		assert( serialOpts !=null);
		assert( name != null );
		this.name = name;
		this.classpath = classpath;
		this.mSerialOpts = serialOpts;
	}
	
	
	public ModuleConfig() {
	}


	public String getInputTextEncoding() {
		return mSerialOpts.getInputTextEncoding();
	}


	public String getOutput_xml_encoding() {
		return mSerialOpts.getOutput_xml_encoding();
	}


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<URL> getClasspath() {
		return classpath;
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
		this.classpath = classpath;
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
	
	
	

}
