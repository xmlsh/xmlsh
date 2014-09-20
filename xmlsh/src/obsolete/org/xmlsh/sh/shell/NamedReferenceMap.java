package org.xmlsh.sh.shell;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlsh.core.IHandle;
import org.xmlsh.core.IHandleable;
import org.xmlsh.core.IReferenceCounted;
import org.xmlsh.core.IReferencedCountedHandle;
import org.xmlsh.core.IReleasable;
import org.xmlsh.types.ITypeConverter;
import org.xmlsh.util.NameValueMap;
import org.xmlsh.util.TypeConvertingIterator;
import org.xmlsh.util.Util;

@SuppressWarnings("serial")
public class NamedReferenceMap<T extends IReferenceCounted> implements Cloneable, IReleasable {
	NameValueMap< T> mMap = new NameValueMap<>();
	static Logger mLogger = LogManager.getLogger();

	Collection<T> valueList() {
		return mMap.values();
	}

	public Iterable<T> valueIterable() {
		return mMap.values();

	}

	
	public boolean containsValue(T v) {
		return mMap.containsValue(v);
	}

	// Clone map and incr ref counts
	@Override
	public NamedReferenceMap<T> clone() {
		mLogger.entry();

		mLogger.trace("Cloning {} handles", mMap.size());
		NamedReferenceMap<T> that = new NamedReferenceMap<>();
		for (Entry<String, T> e : mMap.entrySet()) {
			T mh = e.getValue();
			try {
				that.put(e.getKey(), mh );
			} catch (IOException e1) {
				mLogger.catching(e1);
			}
		}
		return mLogger.exit(that);
	}

	@Override
	public boolean release() throws IOException {
		mLogger.entry();
		for (T hm : mMap.values())
			hm.release();

		mMap.clear();
		mMap = null;
		return true;
	}

	public boolean put(String name, T tv) throws IOException {

		tv.addRef();
		T t = mMap.put(name, tv );
		if( t != null ){
			mLogger.warn("Replacing old hanld with new one by same uri {} mod {}",name,t);
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

	public T getByValue(T v) {
		for (T handle : mMap.values()) {
			if (handle.equals(v))
				return handle;
		}
		return null;
	
	}

	public Iterator<T> valueIterator() {
		return mMap.values().iterator();
	}


}
