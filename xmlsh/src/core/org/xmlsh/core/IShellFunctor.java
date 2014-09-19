package org.xmlsh.core;

public interface IShellFunctor {

	public abstract String getName();

	public abstract EvalEnv argumentEnv(EvalEnv env);

	public abstract EvalEnv returnEnv(EvalEnv env);

}