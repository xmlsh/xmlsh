package org.xmlsh.sh.core;

import java.util.List;

import org.xmlsh.core.EvalEnv;
import org.xmlsh.core.IFunctionExpr;
import org.xmlsh.core.XValue;
import org.xmlsh.sh.module.IModule;
import org.xmlsh.sh.shell.Shell;

/*
 * 
 */
final class ScriptFunctionExpr extends AbstractExpr implements IFunctionExpr {
	private String mName;
	private ICommandExpr mBody;
	private IModule mModule; // containing module

	public ScriptFunctionExpr(String name, ICommandExpr body, IModule module) {
		mName = name;
		mBody = body;
		mModule = module;
	}

	@Override
	public XValue run(Shell shell, List<XValue> args)
			throws Exception {

		return shell.runCommandFunction(mName, mBody, args);

	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public EvalEnv argumentEnv(EvalEnv parent) {
		return parent.withFlagsSet(EvalEnv.commandArgsFlags());
	}

	@Override
	public EvalEnv returnEnv(EvalEnv parent) {
		return parent.withFlagsMasked(EvalEnv.returnValueMask());
	}

	@Override
	public IModule getModule() {
		return mModule;
	}

	public void setModule(IModule module) {
		mModule = module;
	}

}