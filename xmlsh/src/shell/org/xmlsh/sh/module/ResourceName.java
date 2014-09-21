package org.xmlsh.sh.module;

public class ResourceName {
	private String mName;
	  
	public ResourceName(String name) {
		super();
		mName = name;
	}

	public boolean equals(Object anObject) {
		return mName.equals(anObject);
	}

	public String getName() {
		return mName;
	}

	public int hashCode() {
		return mName.hashCode();
	}

	public boolean isEmpty() {
		return mName.isEmpty();
	}

	public void setName(String name) {
		mName = name;
	}

	public String toString() {
		return mName.toString();
	}
	

}
