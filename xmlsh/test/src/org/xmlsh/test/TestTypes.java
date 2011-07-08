/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.test;

public class TestTypes {
	
	private		Object	mValue1;
	private		Object  mValue2;
	private		String	mMethod;
	private		String 	mConstructor;

	/*
	 * Constructors
	 */
	public TestTypes()
	{
		mConstructor = "TestTypes()";
		
	}
	
	public TestTypes(String s)
	{
		mConstructor = "TestTypes(String)";
		mValue1 = s ;
		
	}
	
	public TestTypes(Object o)
	{
		mConstructor = "TestTypes(Object)";
		mValue1 = o ;
	}

	public TestTypes(int x)
	{
		mConstructor = "TestTypes(int)";
		mValue1 = Integer.valueOf(x) ;
	}

	public TestTypes(Integer x)
	{
		mConstructor = "TestTypes(Integer)";
		mValue1 = x;
	}
	
	public TestTypes(int x , String y)
	{
		mConstructor = "TestTypes(int,String)";
		mValue1 = Integer.valueOf(x);
		mValue2 = y ;
	}

	
	public TestTypes(long x , String y)
	{
		mConstructor = "TestTypes(long,String)";
		mValue1 = Long.valueOf(x);
		mValue2 = y ;
	}
	public TestTypes(Long x , String y)
	{
		mConstructor = "TestTypes(Long,String)";
		mValue1 = x ;
		mValue2 = y ;
	}

	public TestTypes(Integer x , String y)
	{
		mConstructor = "TestTypes(Integer,String)";
		mValue1 = x ;
		mValue2 = y ;
	}
	
	public Object getValue1() { 
		return mValue1;
	}
	public String getMethod()
	{
		return mMethod ;
	}
	public String getConstructor()
	{
		return mConstructor;
	}


	public static	String staticAsString( )
	{
		return "String";
	}
	
	public Object asNull()
	{
		return null;
	}
	
}



//
//
//Copyright (C) 2008,2009,2010,2011 David A. Lee.
//
//The contents of this file are subject to the "Simplified BSD License" (the "License");
//you may not use this file except in compliance with the License. You may obtain a copy of the
//License at http://www.opensource.org/licenses/bsd-license.php 
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied.
//See the License for the specific language governing rights and limitations under the License.
//
//The Original Code is: all this file.
//
//The Initial Developer of the Original Code is David A. Lee
//
//Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
//
//Contributor(s): none.
//
