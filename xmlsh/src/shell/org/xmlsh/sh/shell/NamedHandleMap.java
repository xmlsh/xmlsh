package org.xmlsh.sh.shell;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.AbstractCommand;
import org.xmlsh.core.IHandle;
import org.xmlsh.core.IManagedHandle;
import org.xmlsh.core.IReleasable;
import org.xmlsh.types.ITypeConverter;
import org.xmlsh.util.TypeConvertingIterator;
import org.xmlsh.util.Util;

@SuppressWarnings("serial")
public class NamedHandleMap<V extends Closeable,T extends IManagedHandle<V>> implements Cloneable, IReleasable {
	Map<String, T> mMap = new HashMap<>();
	static Logger mLogger = LogManager.getLogger();

	List<V> valueList() {
		return Util.toList(valueIterator());
	}

	public Iterable<V> valueIterable() {
		return new Iterable<V>() {

			@Override
			public Iterator<V> iterator() {
				return valueIterator();
			}
		};

	}
	
	public Iterable<T> handleIterable() {
		return mMap.values();
	}
	public Iterator<T> handleIterator() {
		return mMap.values().iterator();
	}

	public Iterator<V> valueIterator() {
		ITypeConverter<T, V> converter = new ITypeConverter<T, V>() {
			@Override
			public V convert(T h) {
				return h.get();
			}

		};

		return new TypeConvertingIterator<T, V >(mMap.values()
				.iterator(), converter);

	}

	
	
	public boolean containsValue(V v) {
		for (T handle : mMap.values()) {
			if (handle.get().equals(v))
				return true;
		}
		return false;
	}

	// Clone map and incr ref counts
	public NamedHandleMap<V,T> clone() {
		mLogger.entry();

		mLogger.trace("Cloning {} handles", mMap.size());
		NamedHandleMap<V,T> that = new NamedHandleMap<>();
		for (Entry<String, T> e : mMap.entrySet()) {
			T mh = e.getValue();
			mh.addRef();
			that.mMap.put(e.getKey(), mh );
		}
		
		return mLogger.exit(that);
	}

	public boolean release() throws IOException {
		mLogger.entry();
		for (IHandle<V> hm : mMap.values())
			hm.release();

		mMap.clear();
		mMap = null;
		return true;
	}

	public boolean put(String uri, T tv) throws IOException {

		T t = mMap.put(uri, tv );
		if( t != null ){
			mLogger.warn("Replacing old hanld with new one by same uri {} mod {}",uri,t);
			t.release();
		}
		return mLogger.exit(t!=null  );
		
	}

	public T get(String name) {
		return mMap.get(name);
	}

	public boolean containsKey(String name) {
		return mMap.containsKey(name);
	}


}
