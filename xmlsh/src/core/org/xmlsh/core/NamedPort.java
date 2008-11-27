package org.xmlsh.core;



class NamedPort<P extends IPort> {
	String		mName;		// Optional name
	boolean		mDefault;	// is default input
	P			mPort;		// port 
	
	NamedPort( String name , boolean def  , P port  )
	{
		mName = name ;
		mDefault = def ; 
		mPort = port ;
	}
	
	NamedPort( NamedPort<P> that ){
		mName = that.mName ;
		mDefault = that.mDefault ;
		mPort = that.mPort;
		if( mPort != null )
			mPort.addRef();
	}
	
}