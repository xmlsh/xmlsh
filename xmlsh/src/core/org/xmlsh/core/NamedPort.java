package org.xmlsh.core;



public class NamedPort<P extends IPort> {
	String		mName;		// Optional name
	boolean		mDefault;	// is default input
	P			mPort;		// port 
	
	public NamedPort( String name , boolean def  , P port  )
	{
		mName = name ;
		mDefault = def ; 
		mPort = port ;
	}
	
	public NamedPort( NamedPort<P> that ){
		mName = that.mName ;
		mDefault = that.mDefault ;
		mPort = that.mPort;
		if( mPort != null )
			mPort.addRef();
	}
	public String getName() {
		return mName ;
	}
	public P getPort(){
		return mPort ;
	}
}