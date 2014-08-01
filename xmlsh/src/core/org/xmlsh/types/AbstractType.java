package org.xmlsh.types;

import org.xmlsh.core.XValue;

abstract class AbstractType implements IType
{
	protected XTypeKind	   mKind;
	protected TypeFamily	mFamily;
	protected final XValue	_nullValue;

	protected AbstractType(TypeFamily family, XTypeKind kind)
	{
		mFamily = family;
		mKind = kind;
		_nullValue = new XValue(family);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.types.IType#family()
	 */
	@Override
	public final TypeFamily family()
	{
		return mFamily;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.types.IType#getMethods()
	 */
	@Override
	public final IMethods getMethods()
	{
		return getMethodsInstance();
	}

	protected abstract IMethods getMethodsInstance();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.types.IType#isAtomic()
	 */
	@Override
	public boolean isAtomic()
	{
		return kind() == XTypeKind.ATOMIC;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.types.IType#isContainer()
	 */
	@Override
	public boolean isContainer()
	{
		return kind() == XTypeKind.CONTAINER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.types.IType#isNull()
	 */
	@Override
	public boolean isNull()
	{
		return kind() == XTypeKind.NULL;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xmlsh.types.IType#kind()
	 */
	@Override
	public final XTypeKind kind()
	{
		return mKind;
	}

	protected XValue newXValue(Object obj)
	{
		return new XValue(mFamily, obj);
	}

}
