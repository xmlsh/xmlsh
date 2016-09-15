package org.xmlsh.types.xtypes;

import java.util.List;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.shell.Shell;

public interface IXCallable
{
	public XValue   invoke( Shell shell , List<XValue> args ) throws Exception;
	public abstract String getName();
	public abstract EvalEnv argumentEnv(EvalEnv env);
	public abstract EvalEnv returnEnv(EvalEnv env);

}