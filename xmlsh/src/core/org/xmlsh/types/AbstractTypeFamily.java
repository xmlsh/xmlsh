/**
 * $Id: $
 * $Date: $
 *
 */

package org.xmlsh.types;

import java.util.EnumMap;

abstract class AbstractTypeFamily implements ITypeFamily
{	
	protected EnumMap<XTypeKind,IType>  mTypes = new EnumMap<>(XTypeKind.class);


	/* (non-Javadoc)
	 * @see org.xmlsh.types.ITypeFamily#getMethods(java.lang.Class)
	 */
	@Override
	public IMethods getMethods(Class<?> cls)
	{
		return getType(cls).getMethods();

	}
	/* (non-Javadoc)
	 * @see org.xmlsh.types.ITypeFamily#getMethods(org.xmlsh.types.XTypeKind)
	 */
	@Override
	public IMethods getMethods(XTypeKind kind)
	{
		return getType(kind).getMethods();
	}


	/* (non-Javadoc)
	 * @see org.xmlsh.types.ITypeFamily#getNullType()
	 */
	@Override
	public IType getNullType()
	{
		return getType( XTypeKind.NULL );
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.types.ITypeFamily#getType(java.lang.Class)
	 */
	@Override
	public final IType getType(Class<?> cls)
	{
		return getType( inferKind( cls ) );
	}

	/* (non-Javadoc)
	 * @see org.xmlsh.types.ITypeFamily#getType(org.xmlsh.types.XTypeKind)
	 */
	@Override
	public final IType getType(XTypeKind kind)
	{
		IType it = mTypes.get(kind)  ;
		if(  it == null )
			mTypes.put( kind , it =  getTypeInstance(kind) );
		return it;

	}

	protected abstract  IType getTypeInstance(XTypeKind kind);

	protected abstract  XTypeKind inferKind(Class<?> cls);



}

/*
 * Copyright (C) 2008-2012 David A. Lee.
 * 
 * The contents of this file are subject to the "Simplified BSD License" (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.opensource.org/licenses/bsd-license.php 

 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and limitations under the License.
 *
 * The Original Code is: all this file.
 *
 * The Initial Developer of the Original Code is David A. Lee
 *
 * Portions created by (your name) are Copyright (C) (your legal entity). All Rights Reserved.
 *
 * Contributor(s): David A. Lee
 * 
 */