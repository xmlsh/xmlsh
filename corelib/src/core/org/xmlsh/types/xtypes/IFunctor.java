package org.xmlsh.types.xtypes;

import java.util.concurrent.Callable;

import org.xmlsh.core.EvalEnv;

public interface IFunctor extends Callable , Runnable  {

	public abstract String getName();

	public abstract EvalEnv argumentEnv(EvalEnv env);

	public abstract EvalEnv returnEnv(EvalEnv env);

}