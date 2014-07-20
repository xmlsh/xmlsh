package org.xmlsh.core;

import org.xmlsh.util.NameValue;



public class NamedPort<P extends IPort> extends NameValue<P> {
	
	boolean    mSystem;    // System port from original env
	
	public NamedPort( String name ,  P port   ) 
	{
		this(name,port,false);
	}
	
	public NamedPort( String name ,  P port , boolean system  ) 
	{
		super(name,port);
		mSystem = system ;
	}
	
	public NamedPort( NamedPort<P> that ){
		super( that.getName() , that.getValue() );
		// do NOT copy system 
		mSystem = false ;
		if( getValue() != null )
			getValue().addRef();
	}
	
	public P getPort(){

		return getValue();
	}

	public boolean getSystem()
	{
		return mSystem;
	}
	
	
}