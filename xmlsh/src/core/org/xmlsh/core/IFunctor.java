package org.xmlsh.core;

import java.util.concurrent.Callable;

public interface IFunctor extends Callable , Runnable  {

	public abstract String getName();

	public abstract EvalEnv argumentEnv(EvalEnv env);

	public abstract EvalEnv returnEnv(EvalEnv env);

}