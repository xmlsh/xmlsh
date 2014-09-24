package org.xmlsh.util;

public interface INameValue<T> extends INamed {

	@Override
	public  String getName();
	public T getValue();

}